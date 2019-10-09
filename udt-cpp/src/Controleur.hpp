#include "Carte.hpp"
#include "Compte.hpp"

#include <dab/ControleurComponent.hpp>
#include <util/Timeout.hpp>

namespace udt {

   class Controleur : public dab::ControleurComponent {
   public:

      Controleur( const char * name ) :
         dab::ControleurComponent( name ),
         _timeoutSaisirCode    ( *this, DELAI_DE_SAISIE_DU_CODE, &Controleur::confisquerLaCarte ),
         _timeoutSaisirMontant ( *this, DELAI_DE_SAISIE_DU_CODE, &Controleur::confisquerLaCarte ),
         _timeoutPrendreCarte  ( *this, DELAI_DE_SAISIE_DU_CODE, &Controleur::confisquerLaCarte ),
         _timeoutPrendreBillets( *this, DELAI_DE_SAISIE_DU_CODE, &Controleur::confisquerLaCarte )
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
         _iHM.etatDuDab().soldeCaisse += montant;
         if( _iHM.etatDuDab().soldeCaisse < 1000 ) {
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

      virtual void carteInseree( const char * id ) {
         _carte .invalidate();
         _compte.invalidate();
         _automaton.process( dab::Evenement::CARTE_INSEREE );
         _siteCentral.getInformations( id );
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
               _iHM.confisquerLaCarte();
               return;
            }
         }
         else {
            ::fprintf( stderr, "Carte et/ou compte invalide\n" );
            _automaton.process( dab::Evenement::CARTE_INVALIDE );
         }
      }

      virtual void codeSaisi( const char * code ) {
         if( ! _carte.isValid()) {
            _automaton.process( dab::Evenement::CARTE_INVALIDE );
         }
         else if( _carte.compareCode( code )) {
            _automaton.process( dab::Evenement::BON_CODE );
         }
         else {
            _siteCentral.incrNbEssais( _carte.getId());
            _carte.incrementeNbEssais();
            if( _carte.getNbEssais() == 1 ) {
               _automaton.process( dab::Evenement::MAUVAIS_CODE_1 );
            }
            else if( _carte.getNbEssais() == 2 ) {
               _automaton.process( dab::Evenement::MAUVAIS_CODE_2 );
            }
            else if( _carte.getNbEssais() == 3 ) {
               _iHM.confisquerLaCarte();
               _automaton.process( dab::Evenement::MAUVAIS_CODE_3 );
            }
         }
      }

      virtual void montantSaisi( const double & montant ) {
         if( montant > _iHM.etatDuDab().soldeCaisse ) {
            _iHM.ejecterLaCarte();
            _automaton.process( dab::Evenement::SOLDE_CAISSE_INSUFFISANT );
         }
         else if( montant > _compte.getSolde()) {
            _iHM.ejecterLaCarte();
            _automaton.process( dab::Evenement::SOLDE_COMPTE_INSUFFISANT );
         }
         else {
            _iHM.etatDuDab().soldeCaisse -= montant;
            _siteCentral.retrait( _carte.getId(), montant );
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
         _iHM.shutdown();
         _siteCentral.shutdown();
         _automaton.process( dab::Evenement::TERMINATE );
         terminate();
      }

      /**
       * Méthode appelée après réception et traitement d'un événement ou d'une requête.
       */
      virtual void afterDispatch( void ) {
         _iHM.etatDuDab().etat = _automaton.getCurrentState();
         _iHM.publishEtatDuDab();
      }

   private:

      /**
       * Déclenchée par timeout, cette méthode ne peut attendre qu'afterDispatch() publie l'état du DAB
       */
      void confisquerLaCarte( void ) {
         _iHM.confisquerLaCarte();
         _automaton.process( dab::Evenement::DELAI_EXPIRE );
         _iHM.etatDuDab().etat = _automaton.getCurrentState();
         _iHM.publishEtatDuDab();
      }

   public:

      virtual void armerLeTimeoutDeSaisieDuCode( void ) {
         _timeoutSaisirCode.start();
      }

      virtual void armerLeTimeoutDeSaisieDuMontant( void ) {
         _timeoutSaisirCode   .cancel();
         _timeoutSaisirMontant.start();
      }

      virtual void armerLeTimeoutDeRetraitDeLaCarte( void ) {
         _timeoutSaisirMontant .cancel();
         _timeoutPrendreCarte.start();
      }

      virtual void armerLeTimeoutDeRetraitDesBillets( void ) {
         _timeoutPrendreCarte   .cancel();
         _timeoutPrendreBillets.start();
      }

      virtual void annulerLeTimeoutDeRetraitDesBillets( void ) {
         _timeoutPrendreBillets.cancel();
      }

   private:

      static const unsigned DELAI_DE_SAISIE_DU_CODE        = 30*1000;// millisecondes
      static const unsigned DELAI_DE_SAISIE_DU_MONTANT     = 30*1000;// millisecondes
      static const unsigned DELAI_POUR_PRENDRE_LA_CARTE    = 10*1000;// millisecondes
      static const unsigned DELAI_POUR_PRENDRE_LES_BILLETS = 10*1000;// millisecondes

      Carte                             _carte;
      Compte                            _compte;
      util::TimeoutCallBack<Controleur> _timeoutSaisirCode;
      util::TimeoutCallBack<Controleur> _timeoutSaisirMontant;
      util::TimeoutCallBack<Controleur> _timeoutPrendreCarte;
      util::TimeoutCallBack<Controleur> _timeoutPrendreBillets;
   };
}
