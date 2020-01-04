package hpms.udt;

import java.io.IOException;

import hpms.dabtypes.Etat;
import hpms.dabtypes.Evenement;

public final class Controleur extends ControleurComponent {

   private static final double RETRAIT_MAX = 1000.0;

   private final Carte  _carte  = new Carte();
   private final Compte _compte = new Compte();
   private /* */ double _montantDeLatransactionEnCours = 0.0;

   public Controleur( byte instanceID, da.IMainLoop mainLoop ) {
      super( instanceID, mainLoop );
   }

   @Override
   public void init() {
      _etatDuDab.etat        = _automaton.getCurrentState();
      _etatDuDab.soldeCaisse = 0.0;
   }

   public Etat getEtat() {
      return _automaton.getCurrentState();
   }

   public double getSoldeCaisse() {
      return _etatDuDab.soldeCaisse;
   }

   @Override
   public void maintenance( boolean maintenance ) {
      if( maintenance ) {
         _automaton.process( hpms.dabtypes.Evenement.MAINTENANCE_ON );
      }
      else {
         _automaton.process( hpms.dabtypes.Evenement.MAINTENANCE_OFF );
      }
   }

   @Override
   public void rechargerLaCaisse( double montant ) throws IOException {
      _etatDuDab.soldeCaisse += montant;              // 'montant' peut être négatif
      if( _etatDuDab.soldeCaisse < RETRAIT_MAX ) {    // _valeurCaisse peut donc passer en dessous du seuil
         _automaton.process( Evenement.SOLDE_CAISSE_INSUFFISANT );
      }
   }

   @Override
   public void anomalie( boolean anomalie ) {
      if( anomalie ) {
         _automaton.process( Evenement.ANOMALIE_ON );
      }
      else {
         _automaton.process( Evenement.ANOMALIE_OFF );
      }
   }

   @Override
   public void carteInseree( String id ) throws IOException {
      System.err.printf( "%s.carteInseree|id = %s\n", getClass().getName(), id );
      _carte .invalidate();
      _compte.invalidate();
      _siteCentral.informations( id );
      _automaton.process( Evenement.CARTE_INSEREE );
   }

   @Override
   public void informationsResponse( hpms.dabtypes.Information information ) throws IOException {
      final hpms.dabtypes.Carte  carte  = information.carte;
      final hpms.dabtypes.Compte compte = information.compte;
      System.err.printf( "%s.informationsResponse|carte = %s, solde = %7.2f\n", getClass().getName(), carte.id, compte.solde );
      _carte .set( carte );
      _compte.set( compte );
      if( _carte.isValid() && _compte.isValid()) {
         if( _carte.getNbEssais() == 0 ) {
            _automaton.process( Evenement.CARTE_LUE_0 );
         }
         else if( _carte.getNbEssais() == 1 ) {
            _automaton.process( Evenement.CARTE_LUE_1 );
         }
         else if( _carte.getNbEssais() == 2 ) {
            _automaton.process( Evenement.CARTE_LUE_2 );
         }
         else {
            _automaton.process( Evenement.CARTE_CONFISQUEE );
            _iHM.confisquerLaCarte();
            return;
         }
      }
      else {
         System.err.printf( "Carte et/ou compte invalide\n" );
         _automaton.process( Evenement.CARTE_INVALIDE );
      }
   }

   @Override
   public void codeSaisi( String code ) throws IOException {
      System.err.printf( "%s.codeSaisi|code = %s\n", getClass().getName(), code );
      if( ! _carte._isValid ) {
         _automaton.process( Evenement.CARTE_INVALIDE );
      }
      else if( _carte.compareCode( code )) {
         _automaton.process( Evenement.BON_CODE );
      }
      else {
         _siteCentral.incrNbEssais( _carte.getId());
         _carte.incrementeNbEssais();
         if( _carte.getNbEssais() == 1 ) {
            _automaton.process( Evenement.MAUVAIS_CODE_1 );
         }
         else if( _carte.getNbEssais() == 2 ) {
            _automaton.process( Evenement.MAUVAIS_CODE_2 );
         }
         else if( _carte.getNbEssais() == 3 ) {
            _automaton.process( Evenement.MAUVAIS_CODE_3 );
            _iHM.confisquerLaCarte();
         }
      }
   }

   @Override
   public void montantSaisi( double montant ) throws IOException {
      System.err.printf( "%s.montantSaisi|montant = %7.2f\n", getClass().getName(), montant );
      _iHM.ejecterLaCarte();
      if( montant > _etatDuDab.soldeCaisse ) {
         _automaton.process( Evenement.SOLDE_CAISSE_INSUFFISANT );
      }
      else if( montant > _compte.getSolde()) {
         _automaton.process( Evenement.SOLDE_COMPTE_INSUFFISANT );
      }
      else {
         _montantDeLatransactionEnCours = montant;
         _automaton.process( Evenement.MONTANT_OK );
      }
   }

   @Override
   public void carteRetiree() throws IOException {
      System.err.printf( "%s.carteRetiree\n", getClass().getName());
      _siteCentral.retrait( _carte.getId(), _montantDeLatransactionEnCours );
      _iHM.ejecterLesBillets( _montantDeLatransactionEnCours );
      _etatDuDab.soldeCaisse -= _montantDeLatransactionEnCours;
      _montantDeLatransactionEnCours = 0.0;
      _automaton.process( Evenement.CARTE_RETIREE );
   }

   @Override
   public void billetsRetires() {
      System.err.printf( "%s.billetsRetires\n", getClass().getName() );
      _automaton.process( Evenement.BILLETS_RETIRES );
   }

   @Override
   public void annulationDemandeeParLeClient() throws IOException {
      System.err.printf( "%s.annulationDemandeeParLeClient\n", getClass().getName() );
      _montantDeLatransactionEnCours = 0.0;
      _iHM.ejecterLaCarte();
      _automaton.process( Evenement.ANNULATION_CLIENT );
   }

   @Override
   public void arret() throws IOException {
      _iHM.arret();
      _siteCentral.arret();
      _automaton.process( Evenement.TERMINATE );
      System.exit( 0 );
   }

   @Override
   protected void afterDispatch() throws IOException {
      _etatDuDab.etat = _automaton.getCurrentState();
      _uniteDeTraitementPublisher.publishEtatDuDab( _etatDuDab );
   }

   private void confisquerLaCarte() {
      try {
         _iHM.confisquerLaCarte();
         _automaton.process( Evenement.DELAI_EXPIRE );
      }
      catch( final IOException t ) {
         t.printStackTrace();
      }
   }

   @Override
   public void saisieDuCodeElapsed() throws IOException {
      confisquerLaCarte();
   }

   @Override
   public void saisieDuMontantElapsed() throws IOException {
      confisquerLaCarte();
   }

   @Override
   public void retraitDeLaCarteElapsed() throws IOException {
      confisquerLaCarte();
   }

   @Override
   public void retraitDesBilletsElapsed() throws IOException {
      _iHM.placerLesBilletsDansLaCorbeille();
      _automaton.process( Evenement.DELAI_EXPIRE );
   }

   @Override protected void armerLeTimeoutDeSaisieDuCode       () {                              _saisieDuCode     .start(); }
   @Override protected void armerLeTimeoutDeSaisieDuMontant    () { _saisieDuCode     .cancel(); _saisieDuMontant  .start(); }
   @Override protected void armerLeTimeoutDeRetraitDeLaCarte   () { _saisieDuMontant  .cancel(); _retraitDeLaCarte .start(); }
   @Override protected void armerLeTimeoutDeRetraitDesBillets  () { _retraitDeLaCarte .cancel(); _retraitDesBillets.start(); }
   @Override protected void annulerLeTimeoutDeRetraitDesBillets() { _retraitDesBillets.cancel(); }
}
