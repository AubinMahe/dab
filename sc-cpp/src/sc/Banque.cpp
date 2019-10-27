#include <sc/Banque.hpp>

#include <os/Thread.hpp>
#include <util/Time.hpp>

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
   fprintf( stderr, "%s:%s|launching UI\n", util::Time::now(), HPMS_FUNCNAME );
   os::Thread( launchUI, &_ui );
}

void Banque::getInformations( const char * carteID, dabtypes::SiteCentralGetInformationsResponse & response ) {
   fprintf( stderr, "%s:%s|carteID = %s\n", util::Time::now(), HPMS_FUNCNAME, carteID );
   dabtypes::Carte * carte = _repository.getCarte( carteID );
   if( carte ) {
      dabtypes::Compte * compte = _repository.getCompte( carteID );
      if( compte ) {
         response.carte  = *carte;
         response.compte = *compte;
      }
      else {
         fprintf( stderr, "%s:%s|unknown!\n", util::Time::now(), HPMS_FUNCNAME );
      }
   }
   else {
      fprintf( stderr, "%s:%s|unknown!\n", util::Time::now(), HPMS_FUNCNAME );
   }
}

void Banque::incrNbEssais( const char * carteID ) {
   fprintf( stderr, "%s:%s|carteID = %s\n", util::Time::now(), HPMS_FUNCNAME, carteID );
   dabtypes::Carte * carte = _repository.getCarte( carteID );
   if( carte ) {
      carte->nbEssais++;
   }
   else {
      fprintf( stderr, "%s:%s|unknown!\n", util::Time::now(), HPMS_FUNCNAME );
   }
   _ui.refresh();
}

void Banque::retrait( const char * carteID, const double & montant ) {
   fprintf( stderr, "%s:%s|carteID = %s, montant = %7.2f\n", util::Time::now(), HPMS_FUNCNAME, carteID, montant );
   dabtypes::Compte * compte = _repository.getCompte( carteID );
   if( compte ) {
      compte->solde -= montant;
   }
   else {
      fprintf( stderr, "%s:%s|unknown!\n", util::Time::now(), HPMS_FUNCNAME );
   }
   _ui.refresh();
}

void Banque::shutdown( void ) {
   fprintf( stderr, "%s:%s\n", util::Time::now(), HPMS_FUNCNAME );
   terminate();
}
