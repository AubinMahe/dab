#pragma once

#include <sc/banque.h>

typedef struct sc_cartes_compte_s {
   dabtypes_carte *  carte;
   dabtypes_compte * compte;
} sc_cartes_compte;

typedef struct sc_repository_s {

   dabtypes_carte   cartes [5];
   dabtypes_compte  comptes[4];
   sc_cartes_compte cartes_compte[5];
} sc_repository;

util_error sc_repository_init      ( sc_repository * This );
util_error sc_repository_get_carte ( sc_repository * This, const char * carte_id, dabtypes_carte **  target );
util_error sc_repository_get_compte( sc_repository * This, const char * carte_id, dabtypes_compte ** target );
