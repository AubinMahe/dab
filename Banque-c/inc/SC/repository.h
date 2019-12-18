#pragma once

#include <DBT/carte.h>
#include <DBT/compte.h>

typedef struct SC_cartes_compte_s {
   DBT_carte *  carte;
   DBT_compte * compte;
} SC_cartes_compte;

typedef struct SC_repository_s {

   DBT_carte        cartes [5];
   DBT_compte       comptes[4];
   SC_cartes_compte cartes_compte[5];
   bool             has_changed;
} SC_repository;

util_error SC_repository_init      ( SC_repository * This );
util_error SC_repository_get_carte ( SC_repository * This, const char * carte_id, DBT_carte **  target );
util_error SC_repository_get_compte( SC_repository * This, const char * carte_id, DBT_compte ** target );
