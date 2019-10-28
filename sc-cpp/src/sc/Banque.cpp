#include <sc/Banque.hpp>

#include <os/Thread.hpp>
#include <os/sleep.hpp>
#include <util/Time.hpp>
#include <util/Log.hpp>

#include <stdio.h>

using namespace sc;

static void * launchUI( void * arg ) {
   BanqueUI * ui = (BanqueUI *)arg;
   ui->run();
   return nullptr;
}

Banque::Banque( const char * name ) :
   BanqueComponent( name ),
   _ui( *this )
{
   UTIL_LOG_MSG( "launching UI" );
   os::Thread( launchUI, &_ui );
}

void Banque::getInformations( const char * carteID, dabtypes::SiteCentralGetInformationsResponse & response ) {
   UTIL_LOG_ARGS( "carteID = %s", carteID );
   dabtypes::Carte * carte = _repository.getCarte( carteID );
   if( carte ) {
      dabtypes::Compte * compte = _repository.getCompte( carteID );
      if( compte ) {
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
   os::sleep( 3000 );
   UTIL_LOG_DONE();
}

void Banque::incrNbEssais( const char * carteID ) {
   UTIL_LOG_ARGS( "carteID = %s\n", carteID );
   dabtypes::Carte * carte = _repository.getCarte( carteID );
   if( carte ) {
      carte->nbEssais++;
   }
   else {
      UTIL_LOG_MSG( "unknown!" );
   }
   _ui.refresh();
}

void Banque::retrait( const char * carteID, const double & montant ) {
   UTIL_LOG_ARGS( "carteID = %s, montant = %7.2f", carteID, montant );
   dabtypes::Compte * compte = _repository.getCompte( carteID );
   if( compte ) {
      compte->solde -= montant;
   }
   else {
      UTIL_LOG_MSG( "unknown!" );
   }
   _ui.refresh();
}

void Banque::shutdown( void ) {
   UTIL_LOG_HERE();
   terminate();
}
