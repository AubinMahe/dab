#include <udt/ControleurComponent.hpp>

#include "Carte.hpp"
#include "Compte.hpp"

#include <util/Timeout.hpp>
#include <util/Time.hpp>
#include <util/Log.hpp>

namespace dab {

   class Controleur : public udt::ControleurComponent {
   public:

      Controleur( const char * name ) :
         udt::ControleurComponent( name )
      {
         UTIL_LOG_HERE();
         _automaton.setDebug( true );
      }

   public:

      virtual void maintenance( bool maintenance ) {
         UTIL_LOG_ARGS( "maintenance = %s", maintenance ? "true" : "false" );
         if( maintenance ) {
            _automaton.process( dabtypes::Evenement::MAINTENANCE_ON );
         }
         else {
            _automaton.process( dabtypes::Evenement::MAINTENANCE_OFF );
         }
      }

      virtual void rechargerLaCaisse( const double & montant ) {
         UTIL_LOG_ARGS( "montant = %7.2f €", montant );
         _uniteDeTraitement._etatDuDab.soldeCaisse += montant;
         if( _uniteDeTraitement._etatDuDab.soldeCaisse < 1000 ) {
            _automaton.process( dabtypes::Evenement::SOLDE_CAISSE_INSUFFISANT );
         }
      }

      virtual void anomalie( bool anomalie ) {
         UTIL_LOG_ARGS( "anomalie = %s", anomalie ? "true" : "false" );
         if( anomalie ) {
            _automaton.process( dabtypes::Evenement::ANOMALIE_ON );
         }
         else {
            _automaton.process( dabtypes::Evenement::ANOMALIE_OFF );
         }
      }

      virtual void carteInseree( const char * id ) {
         UTIL_LOG_ARGS( "id = %s", id );
         _carte .invalidate();
         _compte.invalidate();
         _automaton.process( dabtypes::Evenement::CARTE_INSEREE );
         _siteCentral.getInformations( id );
      }

      virtual void getInformations( const dabtypes::Carte & carte, const dabtypes::Compte & compte ) {
         UTIL_LOG_HERE();
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
            UTIL_LOG_MSG( "Carte et/ou compte invalide" );
            _carte .dump();
            _compte.dump();
            _automaton.process( dabtypes::Evenement::CARTE_INVALIDE );
         }
      }

      virtual void codeSaisi( const char * code ) {
         UTIL_LOG_ARGS( "code = %s", code );
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
         UTIL_LOG_ARGS( "montant = %7.2f €", montant );
         _iHM.ejecterLaCarte();
         if( montant > _uniteDeTraitement._etatDuDab.soldeCaisse ) {
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
         UTIL_LOG_HERE();
         _siteCentral.retrait( _carte.getId(), _montantDeLatransactionEnCours );
         _iHM.ejecterLesBillets( _montantDeLatransactionEnCours );
         _uniteDeTraitement._etatDuDab.soldeCaisse -= _montantDeLatransactionEnCours;
         _montantDeLatransactionEnCours = 0.0;
         _automaton.process( dabtypes::Evenement::CARTE_RETIREE );
      }

      virtual void billetsRetires( void ) {
         UTIL_LOG_HERE();
         _automaton.process( dabtypes::Evenement::BILLETS_RETIRES );
      }

      virtual void annulationDemandeeParLeClient() {
         UTIL_LOG_HERE();
         _montantDeLatransactionEnCours = 0.0;
         _iHM.ejecterLaCarte();
         _automaton.process( dabtypes::Evenement::ANNULATION_CLIENT );
      }

      virtual void shutdown( void ) {
         UTIL_LOG_HERE();
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
         _uniteDeTraitement._etatDuDab.etat = _automaton.getCurrentState();
         UTIL_LOG_ARGS( "etat = %s", dabtypes::toString( _uniteDeTraitement._etatDuDab.etat ));
         _uniteDeTraitement.publishEtatDuDab();
      }

   private:

      void confisquerLaCarte( void ) {
         UTIL_LOG_HERE();
         _iHM.confisquerLaCarte();
         _automaton.process( dabtypes::Evenement::DELAI_EXPIRE );
      }

      void placerLesBilletsDansLaCorbeille( void ) {
         UTIL_LOG_HERE();
         _iHM.placerLesBilletsDansLaCorbeille();
         _automaton.process( dabtypes::Evenement::DELAI_EXPIRE );
      }

   public:

      virtual void armerLeTimeoutDeSaisieDuCode       ( void ) { UTIL_LOG_HERE();                              _saisieDuCode     .start(); }
      virtual void armerLeTimeoutDeSaisieDuMontant    ( void ) { UTIL_LOG_HERE(); _saisieDuCode     .cancel(); _saisieDuMontant  .start(); }
      virtual void armerLeTimeoutDeRetraitDeLaCarte   ( void ) { UTIL_LOG_HERE(); _saisieDuMontant  .cancel(); _retraitDeLaCarte .start(); }
      virtual void armerLeTimeoutDeRetraitDesBillets  ( void ) { UTIL_LOG_HERE(); _retraitDeLaCarte .cancel(); _retraitDesBillets.start(); }
      virtual void annulerLeTimeoutDeRetraitDesBillets( void ) { UTIL_LOG_HERE(); _retraitDesBillets.cancel(); }

      virtual void saisieDuCodeElapsed     ( void ) { UTIL_LOG_HERE(); confisquerLaCarte(); }
      virtual void saisieDuMontantElapsed  ( void ) { UTIL_LOG_HERE(); confisquerLaCarte(); }
      virtual void retraitDeLaCarteElapsed ( void ) { UTIL_LOG_HERE(); confisquerLaCarte(); }
      virtual void retraitDesBilletsElapsed( void ) { UTIL_LOG_HERE(); placerLesBilletsDansLaCorbeille(); }

   private:

      Carte  _carte;
      Compte _compte;
      double _montantDeLatransactionEnCours;
   };
}
