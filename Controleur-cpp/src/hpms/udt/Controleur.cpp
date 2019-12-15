#include <hpms/udt/Controleur.hpp>

#include <util/Log.hpp>
#include <util/Timeout.hpp>
#include <util/Time.hpp>

using namespace hpms::udt;

Controleur::Controleur( void ) {
   UTIL_LOG_HERE();
   _automaton.setDebug( true );
}

void Controleur::maintenance( bool maintenance ) {
   UTIL_LOG_ARGS( "maintenance = %s", maintenance ? "true" : "false" );
   if( maintenance ) {
      _automaton.process( hpms::dabtypes::Evenement::MAINTENANCE_ON );
   }
   else {
      _automaton.process( hpms::dabtypes::Evenement::MAINTENANCE_OFF );
   }
}

void Controleur::rechargerLaCaisse( const double & montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f €", montant );
   getUniteDeTraitementData()._etatDuDab.soldeCaisse += montant;
   if( getUniteDeTraitementData()._etatDuDab.soldeCaisse < 1000 ) {
      _automaton.process( hpms::dabtypes::Evenement::SOLDE_CAISSE_INSUFFISANT );
   }
}

void Controleur::anomalie( bool anomalie ) {
   UTIL_LOG_ARGS( "anomalie = %s", anomalie ? "true" : "false" );
   if( anomalie ) {
      _automaton.process( hpms::dabtypes::Evenement::ANOMALIE_ON );
   }
   else {
      _automaton.process( hpms::dabtypes::Evenement::ANOMALIE_OFF );
   }
}

void Controleur::carteInseree( const char * id ) {
   UTIL_LOG_ARGS( "id = %s", id );
   _carte .invalidate();
   _compte.invalidate();
   _automaton.process( hpms::dabtypes::Evenement::CARTE_INSEREE );
   _siteCentral->informations( id );
}

void Controleur::informationsResponse( const hpms::dabtypes::Information & information ) {
   UTIL_LOG_HERE();
   _carte .set( information.carte );
   _compte.set( information.compte );
   if( _carte.isValid() && _compte.isValid()) {
      if( _carte.getNbEssais() == 0 ) {
         _automaton.process( hpms::dabtypes::Evenement::CARTE_LUE_0 );
      }
      else if( _carte.getNbEssais() == 1 ) {
         _automaton.process( hpms::dabtypes::Evenement::CARTE_LUE_1 );
      }
      else if( _carte.getNbEssais() == 2 ) {
         _automaton.process( hpms::dabtypes::Evenement::CARTE_LUE_2 );
      }
      else {
         _automaton.process( hpms::dabtypes::Evenement::CARTE_CONFISQUEE );
         _iHM->confisquerLaCarte();
         return;
      }
   }
   else {
      UTIL_LOG_MSG( "Carte et/ou compte invalide" );
      _carte .dump();
      _compte.dump();
      _automaton.process( hpms::dabtypes::Evenement::CARTE_INVALIDE );
   }
}

void Controleur::codeSaisi( const char * code ) {
   UTIL_LOG_ARGS( "code = %s", code );
   if( ! _carte.isValid()) {
      _automaton.process( hpms::dabtypes::Evenement::CARTE_INVALIDE );
   }
   else if( _carte.compareCode( code )) {
      _automaton.process( hpms::dabtypes::Evenement::BON_CODE );
   }
   else {
      _siteCentral->incrNbEssais( _carte.getId());
      _carte.incrementeNbEssais();
      if( _carte.getNbEssais() == 1 ) {
         _automaton.process( hpms::dabtypes::Evenement::MAUVAIS_CODE_1 );
      }
      else if( _carte.getNbEssais() == 2 ) {
         _automaton.process( hpms::dabtypes::Evenement::MAUVAIS_CODE_2 );
      }
      else if( _carte.getNbEssais() == 3 ) {
         _iHM->confisquerLaCarte();
         _automaton.process( hpms::dabtypes::Evenement::MAUVAIS_CODE_3 );
      }
   }
}

void Controleur::montantSaisi( const double & montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f €", montant );
   _iHM->ejecterLaCarte();
   if( montant > getUniteDeTraitementData()._etatDuDab.soldeCaisse ) {
      _automaton.process( hpms::dabtypes::Evenement::SOLDE_CAISSE_INSUFFISANT );
   }
   else if( montant > _compte.getSolde()) {
      _automaton.process( hpms::dabtypes::Evenement::SOLDE_COMPTE_INSUFFISANT );
   }
   else {
      _montantDeLatransactionEnCours = montant;
      _automaton.process( hpms::dabtypes::Evenement::MONTANT_OK );
   }
}

void Controleur::carteRetiree( void ) {
   UTIL_LOG_HERE();
   _siteCentral->retrait( _carte.getId(), _montantDeLatransactionEnCours );
   _iHM->ejecterLesBillets( _montantDeLatransactionEnCours );
   getUniteDeTraitementData()._etatDuDab.soldeCaisse -= _montantDeLatransactionEnCours;
   _montantDeLatransactionEnCours = 0.0;
   _automaton.process( hpms::dabtypes::Evenement::CARTE_RETIREE );
}

void Controleur::billetsRetires( void ) {
   UTIL_LOG_HERE();
   _automaton.process( hpms::dabtypes::Evenement::BILLETS_RETIRES );
}

void Controleur::annulationDemandeeParLeClient() {
   UTIL_LOG_HERE();
   _montantDeLatransactionEnCours = 0.0;
   _iHM->ejecterLaCarte();
   _automaton.process( hpms::dabtypes::Evenement::ANNULATION_CLIENT );
}

void Controleur::shutdown( void ) {
   UTIL_LOG_HERE();
   _iHM->shutdown();
   _siteCentral->shutdown();
   _automaton.process( hpms::dabtypes::Evenement::TERMINATE );
   _dispatcher->terminate();
}

/**
 * Méthode appelée après réception et traitement d'un événement ou d'une requête.
 * L'état de l'automate à sans doute été mis à jour, il faut donc le publier.
 */
void Controleur::afterDispatch( bool dispatched ) {
   getUniteDeTraitementData()._etatDuDab.etat = _automaton.getCurrentState();
   UTIL_LOG_ARGS( "etat = %s", hpms::dabtypes::toString( getUniteDeTraitementData()._etatDuDab.etat ));
   getUniteDeTraitementData().publishEtatDuDab();
   (void)dispatched;
}

void Controleur::confisquerLaCarte( void ) {
   UTIL_LOG_HERE();
   _iHM->confisquerLaCarte();
   _automaton.process( hpms::dabtypes::Evenement::DELAI_EXPIRE );
}

void Controleur::placerLesBilletsDansLaCorbeille( void ) {
   UTIL_LOG_HERE();
   _iHM->placerLesBilletsDansLaCorbeille();
   _automaton.process( hpms::dabtypes::Evenement::DELAI_EXPIRE );
}

void Controleur::armerLeTimeoutDeSaisieDuCode( void ) {
   UTIL_LOG_HERE();
   _saisieDuCode.start();
}

void Controleur::armerLeTimeoutDeSaisieDuMontant( void ) { UTIL_LOG_HERE(); _saisieDuCode     .cancel(); _saisieDuMontant  .start(); }
void Controleur::armerLeTimeoutDeRetraitDeLaCarte( void ) { UTIL_LOG_HERE(); _saisieDuMontant  .cancel(); _retraitDeLaCarte .start(); }
void Controleur::armerLeTimeoutDeRetraitDesBillets( void ) { UTIL_LOG_HERE(); _retraitDeLaCarte .cancel(); _retraitDesBillets.start(); }
void Controleur::annulerLeTimeoutDeRetraitDesBillets( void ) { UTIL_LOG_HERE(); _retraitDesBillets.cancel(); }

void Controleur::saisieDuCodeElapsed     ( void ) { UTIL_LOG_HERE(); confisquerLaCarte(); }
void Controleur::saisieDuMontantElapsed  ( void ) { UTIL_LOG_HERE(); confisquerLaCarte(); }
void Controleur::retraitDeLaCarteElapsed ( void ) { UTIL_LOG_HERE(); confisquerLaCarte(); }
void Controleur::retraitDesBilletsElapsed( void ) { UTIL_LOG_HERE(); placerLesBilletsDansLaCorbeille(); }
