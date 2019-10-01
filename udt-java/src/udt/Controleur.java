package udt;

import java.io.IOException;

import dab.ControleurComponent;
import dab.Evenement;

public final class Controleur extends ControleurComponent {

   private static final double RETRAIT_MAX = 1000.0;

   private final Carte  _carte  = new Carte();
   private final Compte _compte = new Compte();
   private /* */ double _valeurCaisse;

   public Controleur( String name ) throws IOException {
      super( name );
      _valeurCaisse = 0.0;
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
      _valeurCaisse += montant;              // 'montant' peut être négatif
      _iHM.setSoldeCaisse( _valeurCaisse );
      if( _valeurCaisse < RETRAIT_MAX ) {    // _valeurCaisse peut donc passer en dessous du seuil
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
      _automaton.process( Evenement.CARTE_INSEREE );
      _siteCentral.getInformations( id );
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
      if( montant > _valeurCaisse ) {
         _iHM.ejecterLaCarte();
         _automaton.process( Evenement.SOLDE_CAISSE_INSUFFISANT );
      }
      else if( montant > _compte.getSolde()) {
         _iHM.ejecterLaCarte();
         _automaton.process( Evenement.SOLDE_COMPTE_INSUFFISANT );
      }
      else {
         _valeurCaisse -= montant;
         _iHM.setSoldeCaisse( _valeurCaisse );
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
   public void shutdown() throws IOException {
      _iHM.shutdown();
      _siteCentral.shutdown();
      _automaton.process( Evenement.TERMINATE );
      terminate();
   }

   @Override
   protected void afterDispatch() throws IOException {
      _iHM.setStatus( _automaton.getCurrentState());
   }
}
