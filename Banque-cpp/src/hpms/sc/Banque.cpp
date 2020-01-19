#include <hpms/sc/Banque.hpp>

#include <os/Thread.hpp>
#include <os/sleep.hpp>
#include <util/Time.hpp>
#include <util/Log.hpp>

#include <stdio.h>

using namespace hpms::sc;

void Banque::informations( const char * carteID, hpms::dabtypes::Information & response ) {
   UTIL_LOG_ARGS( "carteID = %s", carteID );
   const hpms::dabtypes::Carte * carte = _repository.getCarte( carteID );
   if( carte ) {
      const hpms::dabtypes::Compte * compte = _repository.getCompte( carteID );
      if( compte ) {
         _repository.printStatusOf( carteID );
         os::sleep( 3000 );
         response.carte  = *carte;
         response.compte = *compte;
      }
      else {
         UTIL_LOG_MSG( "unknown!" );
      }
   }
   else {
      UTIL_LOG_MSG( "unknown!" );
   }
   UTIL_LOG_DONE();
}

void Banque::incrNbEssais( const char * carteID ) {
   UTIL_LOG_ARGS( "carteID = %s\n", carteID );
   hpms::dabtypes::Carte * carte = (hpms::dabtypes::Carte *)_repository.getCarte( carteID );
   if( carte ) {
      carte->nbEssais++;
      _repository.printStatusOf( carteID );
   }
   else {
      UTIL_LOG_MSG( "unknown!" );
   }
}

void Banque::retrait( const char * carteID, const double & montant ) {
   UTIL_LOG_ARGS( "carteID = %s, montant = %7.2f", carteID, montant );
   hpms::dabtypes::Compte * compte = (hpms::dabtypes::Compte *)_repository.getCompte( carteID );
   if( compte ) {
      compte->solde -= montant;
      _repository.printStatusOf( carteID );
   }
   else {
      UTIL_LOG_MSG( "unknown!" );
   }
}

void Banque::arret( void ) {
   UTIL_LOG_HERE();
   _dispatcher->terminate();
}

void Banque::afterDispatch( void ) {
   _ui->refresh();
}
