#pragma once

#include <string.h>
#include <types.h>

#include <dab/carte.h>

#include <dab/compte.h>

struct dab_unite_de_traitement_s;
typedef struct dab_unite_de_traitement_s dab_unite_de_traitement;

util_error dab_unite_de_traitement_maintenance( struct dab_unite_de_traitement_s * This, bool maintenance );
util_error dab_unite_de_traitement_recharger_la_caisse( struct dab_unite_de_traitement_s * This, double montant );
util_error dab_unite_de_traitement_anomalie( struct dab_unite_de_traitement_s * This, bool anomalie );
util_error dab_unite_de_traitement_lire_la_carte( struct dab_unite_de_traitement_s * This, const char * carteID );
util_error dab_unite_de_traitement_code_saisi( struct dab_unite_de_traitement_s * This, const char * code );
util_error dab_unite_de_traitement_montant_saisi( struct dab_unite_de_traitement_s * This, double montant );
util_error dab_unite_de_traitement_carte_retiree( struct dab_unite_de_traitement_s * This );
util_error dab_unite_de_traitement_billets_retires( struct dab_unite_de_traitement_s * This );
util_error dab_unite_de_traitement_shutdown( struct dab_unite_de_traitement_s * This );
util_error dab_unite_de_traitement_carte_lue( struct dab_unite_de_traitement_s * This, const dab_carte * carte, const dab_compte * compte );
