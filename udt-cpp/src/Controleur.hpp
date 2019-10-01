#include "Carte.hpp"
#include "Compte.hpp"

#include <dab/ControleurComponent.hpp>

namespace udt {

   class Controleur : public dab::ControleurComponent {
   public:

      Controleur( const std::string & name ) :
         dab::ControleurComponent( name ),
         _valeurCaisse( 0.0 )
      {}

   public:

      virtual void maintenance( bool maintenance ) {
         if( maintenance ) {
            _automaton.process( dab::Evenement::MAINTENANCE_ON );
         }
         else {
            _automaton.process( dab::Evenement::MAINTENANCE_OFF );
         }
      }

      virtual void rechargerLaCaisse( const double & montant ) {
         _valeurCaisse += montant;
         _iHM->setSoldeCaisse( _valeurCaisse );
         if( _valeurCaisse < 1000 ) {
            _automaton.process( dab::Evenement::SOLDE_CAISSE_INSUFFISANT );
         }
      }

      virtual void anomalie( bool anomalie ) {
         if( anomalie ) {
            _automaton.process( dab::Evenement::ANOMALIE_ON );
         }
         else {
            _automaton.process( dab::Evenement::ANOMALIE_OFF );
         }
      }

      virtual void carteInseree( const std::string & id ) {
         _carte .invalidate();
         _compte.invalidate();
         _automaton.process( dab::Evenement::CARTE_INSEREE );
         _siteCentral->getInformations( id );
      }

      virtual void getInformations( const dab::Carte & carte, const dab::Compte & compte ) {
         _carte .set( carte );
         _compte.set( compte );
         if( _carte.isValid() && _compte.isValid()) {
            if( _carte.getNbEssais() == 0 ) {
               _automaton.process( dab::Evenement::CARTE_LUE_0 );
            }
            else if( _carte.getNbEssais() == 1 ) {
               _automaton.process( dab::Evenement::CARTE_LUE_1 );
            }
            else if( _carte.getNbEssais() == 2 ) {
               _automaton.process( dab::Evenement::CARTE_LUE_2 );
            }
            else {
               _automaton.process( dab::Evenement::CARTE_CONFISQUEE );
               _iHM->confisquerLaCarte();
            }
         }
         else {
            ::fprintf( stderr, "Carte et/ou compte invalide\n" );
            _automaton.process( dab::Evenement::CARTE_INVALIDE );
         }
      }

      virtual void codeSaisi( const std::string & code ) {
         if( ! _carte.isValid()) {
            _automaton.process( dab::Evenement::CARTE_INVALIDE );
         }
         else if( _carte.compareCode( code )) {
            _automaton.process( dab::Evenement::BON_CODE );
         }
         else {
            _siteCentral->incrNbEssais( _carte.getId());
            _carte.incrementeNbEssais();
            if( _carte.getNbEssais() == 1 ) {
               _automaton.process( dab::Evenement::MAUVAIS_CODE_1 );
            }
            else if( _carte.getNbEssais() == 2 ) {
               _automaton.process( dab::Evenement::MAUVAIS_CODE_2 );
            }
            else if( _carte.getNbEssais() == 3 ) {
               _automaton.process( dab::Evenement::MAUVAIS_CODE_3 );
               _iHM->confisquerLaCarte();
            }
         }
      }

      virtual void montantSaisi( const double & montant ) {
         if( montant > _valeurCaisse ) {
            _iHM->ejecterLaCarte();
            _automaton.process( dab::Evenement::SOLDE_CAISSE_INSUFFISANT );
         }
         else if( montant > _compte.getSolde()) {
            _iHM->ejecterLaCarte();
            _automaton.process( dab::Evenement::SOLDE_COMPTE_INSUFFISANT );
         }
         else {
            _valeurCaisse -= montant;
            _iHM->setSoldeCaisse( _valeurCaisse );
            _siteCentral->retrait( _carte.getId(), montant );
            _automaton.process( dab::Evenement::MONTANT_OK );
         }
      }

      virtual void carteRetiree( void ) {
         _automaton.process( dab::Evenement::CARTE_RETIREE );
      }
      virtual void billetsRetires( void ) {
         _automaton.process( dab::Evenement::BILLETS_RETIRES );
      }

      virtual void shutdown( void ) {
         _iHM->shutdown();
         _siteCentral->shutdown();
         _automaton.process( dab::Evenement::TERMINATE );
         terminate();
      }

      virtual void afterDispatch( void ) {
         _iHM->setStatus( _automaton.getCurrentState());
      }

   private:

      Carte  _carte;
      Compte _compte;
      double _valeurCaisse;
   };
}
