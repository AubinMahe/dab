#include <hpms/sc/Repository.hpp>

#include <util/Log.hpp>

#include <stdio.h>
#include <string.h>

using namespace hpms::sc;

static void init( hpms::dabtypes::Carte & carte, const char * id, const char * code, byte month, unsigned short year ) {
   strncpy( carte.id  , id  , 4 ); carte.id  [4] = '\0';
   strncpy( carte.code, code, 4 ); carte.code[4] = '\0';
   carte.month    = month;
   carte.year     = year;
   carte.nbEssais = 0;
}

static void init( hpms::dabtypes::Compte & compte, const char * id, double solde ) {
   strncpy( compte.id  , id  , 4 ); compte.id[4] = '\0';
   compte.solde    = solde;
   compte.autorise = true;
}

Repository::Repository( void ) {
   init( _cartes[0], "A123", "1230", 6, 2022 );
   init( _cartes[1], "B456", "4560", 1, 2021 );
   init( _cartes[2], "C789", "7890", 2, 2022 );
   init( _cartes[3], "D123", "1230", 3, 2021 );
   init( _cartes[4], "E456", "4560", 4, 2022 );

   init( _comptes[0], "#123", 5000.0 );
   init( _comptes[1], "#456", 1000.0 );
   init( _comptes[2], "#789",  400.0 );

   _cartes_compte[0].carte  = &(_cartes [0]);
   _cartes_compte[0].compte = &(_comptes[0]);

   _cartes_compte[1].carte  = &(_cartes [1]);
   _cartes_compte[1].compte = &(_comptes[1]);

   _cartes_compte[2].carte  = &(_cartes [2]);
   _cartes_compte[2].compte = &(_comptes[2]);

   _cartes_compte[3].carte  = &(_cartes [3]);
   _cartes_compte[3].compte = &(_comptes[0]);

   _cartes_compte[4].carte  = &(_cartes [4]);
   _cartes_compte[4].compte = &(_comptes[1]);
}

const hpms::dabtypes::Carte * Repository::getCarte( const char * carteID ) const {
   UTIL_LOG_ARGS( "id = %s", carteID );
   for( unsigned row = 0, count = sizeof( _cartes )/sizeof( _cartes[0] ); row < count; ++row ) {
      if( 0 == strncmp( _cartes[row].id, carteID, 4 )) {
         return _cartes + row;
      }
   }
   UTIL_LOG_ARGS( "id = %s not found", carteID );
   return nullptr;
}

const hpms::dabtypes::Compte * Repository::getCompte( const char * carteID ) const {
   UTIL_LOG_ARGS( "id = %s", carteID );
   for( unsigned row = 0, count = sizeof( _cartes_compte )/sizeof( _cartes_compte[0] ); row < count; ++row ) {
      if( 0 == strncmp( _cartes_compte[row].carte->id, carteID, 4 )) {
         return _cartes_compte[row].compte;
      }
   }
   UTIL_LOG_ARGS( "id = %s not found", carteID );
   return nullptr;
}

const hpms::dabtypes::Carte * Repository::getCartes( unsigned & count ) const {
   count = sizeof( _cartes ) / sizeof( _cartes[0] );
   return _cartes;
}

const hpms::dabtypes::Compte * Repository::getComptes( unsigned & count ) const {
   count = sizeof( _comptes ) / sizeof( _comptes[0] );
   return _comptes;
}

void Repository::printStatusOf( const char * carteID ) const {
   const hpms::dabtypes::Carte  * carte  = getCarte( carteID );
   const hpms::dabtypes::Compte * compte = getCompte( carteID );
   fprintf( stderr, "TEST|nbEssais = %d\n"   , carte ->nbEssais );
   fprintf( stderr, "TEST|solde    = %7.2f\n", compte->solde );
   fflush( stderr );
}
