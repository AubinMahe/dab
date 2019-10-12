package udt;

import java.io.IOException;
import java.time.Duration;

import dab.ControleurComponent;
import dab.Evenement;
import util.Timeout;

public final class Controleur extends ControleurComponent {

   private static final double   RETRAIT_MAX                    = 1000.0;
   private static final Duration DELAI_DE_SAISIE_DU_CODE        = Duration.ofSeconds( 30 );
   private static final Duration DELAI_DE_SAISIE_DU_MONTANT     = Duration.ofSeconds( 30 );
   private static final Duration DELAI_POUR_PRENDRE_LA_CARTE    = Duration.ofSeconds( 10 );
   private static final Duration DELAI_POUR_PRENDRE_LES_BILLETS = Duration.ofSeconds( 10 );

   private final Carte   _carte                 = new Carte();
   private final Compte  _compte                = new Compte();
   private final Timeout _timeoutSaisirCode     = new Timeout( DELAI_DE_SAISIE_DU_CODE       , this::confisquerLaCarte );
   private final Timeout _timeoutSaisirMontant  = new Timeout( DELAI_DE_SAISIE_DU_MONTANT    , this::confisquerLaCarte );
   private final Timeout _timeoutPrendreCarte   = new Timeout( DELAI_POUR_PRENDRE_LA_CARTE   , this::confisquerLaCarte );
   private final Timeout _timeoutPrendreBillets = new Timeout( DELAI_POUR_PRENDRE_LES_BILLETS, this::placerLesBilletsDansLaCorbeille );

   public Controleur( String name ) throws IOException {
      super( name );
      _iHM.etatDuDab.etat        = _automaton.getCurrentState();
      _iHM.etatDuDab.soldeCaisse = 0.0;
   }

   @Override
   public void maintenance( boolean maintenance ) {
      if( maintenance ) {
         _automaton.process( Evenement.MAINTENANCE_ON );
      }
      else {
         _automaton.process( Evenement.MAINTENANCE_OFF );
      }
   }

   @Override
   public void rechargerLaCaisse( double montant ) throws IOException {
      _iHM.etatDuDab.soldeCaisse += montant;              // 'montant' peut être négatif
      if( _iHM.etatDuDab.soldeCaisse < RETRAIT_MAX ) {    // _valeurCaisse peut donc passer en dessous du seuil
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
      _carte .invalidate();
      _compte.invalidate();
      _siteCentral.getInformations( id );
      _automaton.process( Evenement.CARTE_INSEREE );
   }

   @Override
   public void getInformations( dab.Carte carte, dab.Compte compte ) throws IOException {
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
      if( montant > _iHM.etatDuDab.soldeCaisse ) {
         _iHM.ejecterLaCarte();
         _automaton.process( Evenement.SOLDE_CAISSE_INSUFFISANT );
      }
      else if( montant > _compte.getSolde()) {
         _iHM.ejecterLaCarte();
         _automaton.process( Evenement.SOLDE_COMPTE_INSUFFISANT );
      }
      else {
         _iHM.etatDuDab.soldeCaisse -= montant;
         _siteCentral.retrait( _carte.getId(), montant );
         _automaton.process( Evenement.MONTANT_OK );
      }
   }

   @Override
   public void carteRetiree() {
      _automaton.process( Evenement.CARTE_RETIREE );
   }

   @Override
   public void billetsRetires() {
      _automaton.process( Evenement.BILLETS_RETIRES );
   }

   @Override
   public void annulationDemandeeParLeClient() throws IOException {
      _iHM.ejecterLaCarte();
      _automaton.process( Evenement.ANNULATION_CLIENT );
   }

   @Override
   public void shutdown() throws IOException {
      _iHM.shutdown();
      _siteCentral.shutdown();
      _automaton.process( Evenement.TERMINATE );
      terminate();
   }

   private void publishEtatDuDab() throws IOException {
      _iHM.etatDuDab.etat = _automaton.getCurrentState();
      _iHM.publishEtatDuDab();
   }

   @Override
   protected void afterDispatch() throws IOException {
      publishEtatDuDab();
   }

   private void confisquerLaCarte() {
      try {
         _iHM.confisquerLaCarte();
         _automaton.process( Evenement.DELAI_EXPIRE );
         publishEtatDuDab();
      }
      catch( final IOException t ) {
         t.printStackTrace();
      }
   }

   private void placerLesBilletsDansLaCorbeille() {
      try {
         _iHM.placerLesBilletsDansLaCorbeille();
         _automaton.process( Evenement.DELAI_EXPIRE );
         publishEtatDuDab();
      }
      catch( final IOException t ) {
         t.printStackTrace();
      }
   }

   @Override
   protected void armerLeTimeoutDeSaisieDuCode() {
      _timeoutSaisirCode.start();
   }

   @Override
   protected void armerLeTimeoutDeSaisieDuMontant() {
      _timeoutSaisirCode   .cancel();
      _timeoutSaisirMontant.start();
   }

   @Override
   protected void armerLeTimeoutDeRetraitDeLaCarte() {
      _timeoutSaisirMontant .cancel();
      _timeoutPrendreCarte.start();
   }

   @Override
   protected void armerLeTimeoutDeRetraitDesBillets() {
      _timeoutPrendreCarte   .cancel();
      _timeoutPrendreBillets.start();
   }

   @Override
   protected void annulerLeTimeoutDeRetraitDesBillets() {
      _timeoutPrendreBillets.cancel();
   }
}
