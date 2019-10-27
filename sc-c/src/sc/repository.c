#include <sc/repository.h>

#include <string.h>

util_error sc_repository_init( sc_repository * This ) {
   strncpy( This->cartes[0].id    , "A123", 4 ); This->cartes[0].id    [4] = '\0';
   strncpy( This->cartes[0].code  , "1230", 4 ); This->cartes[0].code  [4] = '\0';
   This->cartes[0].month  = 6;
   This->cartes[0].year   = 2022;

   strncpy( This->cartes[1].id    , "B456", 4 ); This->cartes[1].id    [4] = '\0';
   strncpy( This->cartes[1].code  , "4560", 4 ); This->cartes[1].code  [4] = '\0';
   This->cartes[1].month  = 1;
   This->cartes[1].year   = 2021;

   strncpy( This->cartes[2].id    ,"C789", 4 ); This->cartes[2].id    [4] = '\0';
   strncpy( This->cartes[2].code  ,"7890", 4 ); This->cartes[2].code  [4] = '\0';
   This->cartes[2].month  = 2;
   This->cartes[2].year   = 2022;

   strncpy( This->cartes[3].id    , "D123", 4 ); This->cartes[3].id    [4] = '\0';
   strncpy( This->cartes[3].code  , "1230", 4 ); This->cartes[3].code  [4] = '\0';
   This->cartes[3].month  = 3;
   This->cartes[3].year   = 2021;

   strncpy( This->cartes[4].id    , "E456", 4 ); This->cartes[4].id    [4] = '\0';
   strncpy( This->cartes[4].code  , "4560", 4 ); This->cartes[4].code  [4] = '\0';
   This->cartes[4].month  = 4;
   This->cartes[4].year   = 2022;

   strncpy( This->comptes[0].id, "#123", 4 ); This->comptes[0].id[4] = '\0';
   This->comptes[0].solde    = 5000.0;
   This->comptes[0].autorise = true;

   strncpy( This->comptes[1].id, "#456", 4 ); This->comptes[1].id[4] = '\0';
   This->comptes[1].solde    = 1000.0;
   This->comptes[1].autorise = true;

   strncpy( This->comptes[2].id, "#789", 4 ); This->comptes[2].id[4] = '\0';
   This->comptes[2].solde    = 400.0;
   This->comptes[2].autorise = true;

   This->cartes_compte[0].carte  = &(This->cartes [0]);
   This->cartes_compte[0].compte = &(This->comptes[0]);

   This->cartes_compte[1].carte  = &(This->cartes [1]);
   This->cartes_compte[1].compte = &(This->comptes[1]);

   This->cartes_compte[2].carte  = &(This->cartes [2]);
   This->cartes_compte[2].compte = &(This->comptes[2]);

   This->cartes_compte[3].carte  = &(This->cartes [3]);
   This->cartes_compte[3].compte = &(This->comptes[0]);

   This->cartes_compte[4].carte  = &(This->cartes [4]);
   This->cartes_compte[4].compte = &(This->comptes[1]);

   return UTIL_NOT_FOUND;
}

util_error sc_repository_get_carte( sc_repository * This, const char * carte_id, dabtypes_carte ** target ) {
   fprintf( stderr, "%s|id = %s\n", __func__, carte_id );
   for( unsigned row = 0, count = sizeof( This->cartes )/sizeof( This->cartes[0] ); row < count; ++row ) {
      if( 0 == strncmp( This->cartes[row].id, carte_id, 4 )) {
         *target = This->cartes + row;
         return UTIL_NO_ERROR;
      }
   }
   return UTIL_NOT_FOUND;
}

util_error sc_repository_get_compte( sc_repository * This, const char * carte_id, dabtypes_compte ** target ) {
   fprintf( stderr, "%s|id = %s\n", __func__, carte_id );
   for( unsigned row = 0, count = sizeof( This->cartes_compte )/sizeof( This->cartes_compte[0] ); row < count; ++row ) {
      if( 0 == strncmp( This->cartes_compte[row].carte->id, carte_id, 4 )) {
         *target = This->cartes_compte[row].compte;
         return UTIL_NO_ERROR;
      }
   }
   return UTIL_NOT_FOUND;
}
