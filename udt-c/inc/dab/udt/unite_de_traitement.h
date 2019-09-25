#pragma once

#include <dab/unite_de_traitement.h>
#include <dab/ihm.h>
#include <dab/site_central.h>
#include <dab/unite_de_traitement_dispatcher.h>

#include <util/automaton.h>

typedef struct dab_udt_date_s {
   byte   month;
   ushort year;
} dab_udt_date;

typedef struct dab_udt_carte_s {
   bool         is_valid;
   char         id[5];
   char         code[5];
   char         compte[5];
   dab_udt_date peremption;
   byte         nb_essais;
} dab_udt_carte;

typedef struct dab_udt_compte_s {
   bool   is_valid;
   char   id[5];
   double solde;
   bool   autorise;
} dab_udt_compte;

typedef struct dab_unite_de_traitement_s {

   SOCKET                             socket;
   dab_ihm                            ui;
   struct sockaddr_in                 ui_target;
   dab_site_central                   sc;
   struct sockaddr_in                 sc_target;
   dab_unite_de_traitement_dispatcher dispatcher;
   bool                               running;
   util_automaton                     automaton;
   dab_udt_carte                      carte;
   dab_udt_compte                     compte;
   double                             valeur_caisse;

} dab_unite_de_traitement;

util_error dab_unite_de_traitement_init(
   dab_unite_de_traitement * This,
   const char *              intrfc,
   unsigned short            udtPort,
   const char *              uiAddress,
   unsigned short            uiPort,
   const char *              scAddress,
   unsigned short            scPort     );

util_error dab_unite_de_traitement_run( dab_unite_de_traitement * This );
