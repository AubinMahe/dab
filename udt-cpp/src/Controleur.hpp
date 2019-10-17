#include <udt/ControleurComponent.hpp>

#include "Carte.hpp"
#include "Compte.hpp"

#include <util/Timeout.hpp>

namespace dab {

   class Controleur : public udt::ControleurComponent {
   public:

      Controleur( const char * name ) :
         udt::ControleurComponent( name )
      {
         _automaton.setDebug( true );
      }

   public:

      virtual void maintenance( bool maintenance ) {
         fprintf( stderr, "%s, maintenance = %s\n", HPMS_FUNCNAME, maintenance ? "true" : "false" );
         if( maintenance ) {
            _automaton.process( dabtypes::Evenement::MAINTENANCE_ON );
         }
         else {
            _automaton.process( dabtypes::Evenement::MAINTENANCE_OFF );
         }
      }

      virtual void rechargerLaCaisse( const double & montant ) {
         fprintf( stderr, "%s, montant = %7.2f €\n", HPMS_FUNCNAME, montant );
         _iHM.etatDuDab().soldeCaisse += montant;
         if( _iHM.etatDuDab().soldeCaisse < 1000 ) {
            _automaton.process( dabtypes::Evenement::SOLDE_CAISSE_INSUFFISANT );
         }
      }

      virtual void anomalie( bool anomalie ) {
         fprintf( stderr, "%s, anomalie = %s\n", HPMS_FUNCNAME, anomalie ? "true" : "false" );
         if( anomalie ) {
            _automaton.process( dabtypes::Evenement::ANOMALIE_ON );
         }
         else {
            _automaton.process( dabtypes::Evenement::ANOMALIE_OFF );
         }
      }

      virtual void carteInseree( const char * id ) {
         fprintf( stderr, "%s, id = %s\n", HPMS_FUNCNAME, id );
         _carte .invalidate();
         _compte.invalidate();
         _automaton.process( dabtypes::Evenement::CARTE_INSEREE );
         _siteCentral.getInformations( id );
      }

      virtual void getInformations( const dabtypes::Carte & carte, const dabtypes::Compte & compte ) {
         fprintf( stderr, "%s\n", HPMS_FUNCNAME );
         _carte .set( carte );
         _compte.set( compte );
         if( _carte.isValid() && _compte.isValid()) {
            if( _carte.getNbEssais() == 0 ) {
               _automaton.process( dabtypes::Evenement::CARTE_LUE_0 );
            }
            else if( _carte.getNbEssais() == 1 ) {
               _automaton.process( dabtypes::Evenement::CARTE_LUE_1 );
            }
            else if( _carte.getNbEssais() == 2 ) {
               _automaton.process( dabtypes::Evenement::CARTE_LUE_2 );
            }
            else {
               _automaton.process( dabtypes::Evenement::CARTE_CONFISQUEE );
               _iHM.confisquerLaCarte();
               return;
            }
         }
         else {
            ::fprintf( stderr, "%s: Carte et/ou compte invalide\n", HPMS_FUNCNAME );
            _automaton.process( dabtypes::Evenement::CARTE_INVALIDE );
         }
      }

      virtual void codeSaisi( const char * code ) {
         fprintf( stderr, "%s, code = %s\n", HPMS_FUNCNAME, code );
         if( ! _carte.isValid()) {
            _automaton.process( dabtypes::Evenement::CARTE_INVALIDE );
         }
         else if( _carte.compareCode( code )) {
            _automaton.process( dabtypes::Evenement::BON_CODE );
         }
         else {
            _siteCentral.incrNbEssais( _carte.getId());
            _carte.incrementeNbEssais();
            if( _carte.getNbEssais() == 1 ) {
               _automaton.process( dabtypes::Evenement::MAUVAIS_CODE_1 );
            }
            else if( _carte.getNbEssais() == 2 ) {
               _automaton.process( dabtypes::Evenement::MAUVAIS_CODE_2 );
            }
            else if( _carte.getNbEssais() == 3 ) {
               _iHM.confisquerLaCarte();
               _automaton.process( dabtypes::Evenement::MAUVAIS_CODE_3 );
            }
         }
      }

      virtual void montantSaisi( const double & montant ) {
         fprintf( stderr, "%s, montant = %7.2f €\n", HPMS_FUNCNAME, montant );
         _iHM.ejecterLaCarte();
         if( montant > _iHM.etatDuDab().soldeCaisse ) {
            _automaton.process( dabtypes::Evenement::SOLDE_CAISSE_INSUFFISANT );
         }
         else if( montant > _compte.getSolde()) {
            _automaton.process( dabtypes::Evenement::SOLDE_COMPTE_INSUFFISANT );
         }
         else {
            _montantDeLatransactionEnCours = montant;
            _automaton.process( dabtypes::Evenement::MONTANT_OK );
         }
      }

      virtual void carteRetiree( void ) {
         fprintf( stderr, "%s\n", HPMS_FUNCNAME );
         _siteCentral.retrait( _carte.getId(), _montantDeLatransactionEnCours );
         _iHM.ejecterLesBillets( _montantDeLatransactionEnCours );
         _iHM.etatDuDab().soldeCaisse -= _montantDeLatransactionEnCours;
         _montantDeLatransactionEnCours = 0.0;
         _automaton.process( dabtypes::Evenement::CARTE_RETIREE );
      }

      virtual void billetsRetires( void ) {
         fprintf( stderr, "%s\n", HPMS_FUNCNAME );
         _automaton.process( dabtypes::Evenement::BILLETS_RETIRES );
      }

      virtual void annulationDemandeeParLeClient() {
         fprintf( stderr, "%s\n", HPMS_FUNCNAME );
         _montantDeLatransactionEnCours = 0.0;
         _iHM.ejecterLaCarte();
         _automaton.process( dabtypes::Evenement::ANNULATION_CLIENT );
      }

      virtual void shutdown( void ) {
         fprintf( stderr, "%s\n", HPMS_FUNCNAME );
         _iHM.shutdown();
         _siteCentral.shutdown();
         _automaton.process( dabtypes::Evenement::TERMINATE );
         terminate();
      }

   public:

      /**
       * Méthode appelée après réception et traitement d'un événement ou d'une requête.
       * L'état de l'automate à sans doute été mis à jour, il faut donc le publier.
       */
      virtual void afterDispatch( void ) {
         _iHM.etatDuDab().etat = _automaton.getCurrentState();
         fprintf( stderr, "%s, etat = %s\n", HPMS_FUNCNAME, dabtypes::toString( _iHM.etatDuDab().etat ));
         _iHM.publishEtatDuDab();
      }

   private:

      void confisquerLaCarte( void ) {
         fprintf( stderr, "%s\n", HPMS_FUNCNAME );
         _iHM.confisquerLaCarte();
         _automaton.process( dabtypes::Evenement::DELAI_EXPIRE );
      }

      void placerLesBilletsDansLaCorbeille( void ) {
         fprintf( stderr, "%s\n", HPMS_FUNCNAME );
         _iHM.placerLesBilletsDansLaCorbeille();
         _automaton.process( dabtypes::Evenement::DELAI_EXPIRE );
      }

   public:

      virtual void armerLeTimeoutDeSaisieDuCode       ( void ) {                              _saisieDuCode     .start(); }
      virtual void armerLeTimeoutDeSaisieDuMontant    ( void ) { _saisieDuCode     .cancel(); _saisieDuMontant  .start(); }
      virtual void armerLeTimeoutDeRetraitDeLaCarte   ( void ) { _saisieDuMontant  .cancel(); _retraitDeLaCarte .start(); }
      virtual void armerLeTimeoutDeRetraitDesBillets  ( void ) { _retraitDeLaCarte .cancel(); _retraitDesBillets.start(); }
      virtual void annulerLeTimeoutDeRetraitDesBillets( void ) { _retraitDesBillets.cancel(); }

      virtual void saisieDuCodeElapsed     ( void ) { confisquerLaCarte(); }
      virtual void saisieDuMontantElapsed  ( void ) { confisquerLaCarte(); }
      virtual void retraitDeLaCarteElapsed ( void ) { confisquerLaCarte(); }
      virtual void retraitDesBilletsElapsed( void ) { placerLesBilletsDansLaCorbeille(); }

   private:

      Carte  _carte;
      Compte _compte;
      double _montantDeLatransactionEnCours;
   };
}
