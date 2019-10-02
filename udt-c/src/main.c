#include <dab/controleur.h>
#include <dab/evenement.h>

#include <util/args.h>
#include <util/timeout.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct date_s {
   byte   month;
   ushort year;
} date;

typedef struct carte_s {
   bool is_valid;
   char id[5];
   char code[5];
   char compte[5];
   date peremption;
   byte nb_essais;
} carte;

typedef struct compte_s {
   bool   is_valid;
   char   id[5];
   double solde;
   bool   autorise;
} compte;

typedef struct business_logic_data_s {
   carte  carte;
   compte compte;
   double valeur_caisse;
   void * timeout_id_delai_de_saisie_du_code;
} business_logic_data;

static void date_set( date * This, byte month, ushort year ) {
   This->month= month;
   This->year = year;
}

static bool date_is_valid( date * This ) {
   return( This->month > 0 )&&( This->year > 0 );
}

static void carte_set( carte * This, const dab_carte * source ) {
   strncpy( This->id  , source->id  , sizeof( This->id ));
   strncpy( This->code, source->code, sizeof( This->code ));
   This->nb_essais = source->nb_essais;
   date_set( &This->peremption, source->month, source->year );
   This->is_valid  =
      (   strlen( This->id   ) > 0 )
      &&( strlen( This->code ) > 0 )
      &&( This->nb_essais < 4 )
      && date_is_valid( &This->peremption );
}

static bool carte_compare_code( const carte * carte, const char * code ) {
   return 0 == strncmp( carte->code, code, sizeof( carte->code ));
}

void compte_set( compte * This, const dab_compte * source ) {
   strncpy( This->id, source->id, sizeof( This->id ));
   This->solde    = source->solde;
   This->autorise = source->autorise;
   This->is_valid = ( strlen( This->id ) > 0 )&&( This->solde > 0.0 );
}

void compte_invalidate( compte * This ) {
   fprintf( stderr, "compte_invalidate\n" );
   This->is_valid = false;
}

static const double   DAB_RETRAIT_MAX        = 1000.0;
static const unsigned DELAI_DE_SAISIE_DU_CODE = 30*1000;// millisecondes

util_error dab_controleur_maintenance( dab_controleur * This, bool maintenance ) {
   if( maintenance ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAINTENANCE_ON ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAINTENANCE_OFF ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_recharger_la_caisse( dab_controleur * This, double montant ) {
   business_logic_data * d = This->user_context;
   d->valeur_caisse += montant;
   UTIL_ERROR_CHECK( dab_ihm_set_solde_caisse( &This->ihm, d->valeur_caisse ), __FILE__, __LINE__ );
   if( d->valeur_caisse < DAB_RETRAIT_MAX ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_anomalie( dab_controleur * This, bool anomalie ) {
   if( anomalie ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_ANOMALIE_ON ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_ANOMALIE_OFF ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_carte_inseree( dab_controleur * This, const char * id ) {
   business_logic_data * d = This->user_context;
   d->carte.is_valid = false;
   d->compte.is_valid = false;
   UTIL_ERROR_CHECK( dab_site_central_get_informations( &This->site_central, id ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_INSEREE ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

static util_error expiration_du_delai_de_saisie_du_code( void * arg ) {
   dab_controleur * This = arg;
   UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->ihm ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_DELAI_EXPIRE ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_controleur_get_informations( dab_controleur * This, const dab_carte * carte, const dab_compte * compte ) {
   business_logic_data * d = This->user_context;
   carte_set( &d->carte, carte );
   compte_set( &d->compte, compte );
   if( d->carte.is_valid && d->compte.is_valid ) {
      if( d->carte.nb_essais == 0 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_0 ), __FILE__, __LINE__ );
      }
      else if( d->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_1 ), __FILE__, __LINE__ );
      }
      else if( d->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_2 ), __FILE__, __LINE__ );
      }
      else {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_CONFISQUEE ), __FILE__, __LINE__ );
         UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->ihm ), __FILE__, __LINE__ );
         return UTIL_NO_ERROR;
      }
      UTIL_ERROR_CHECK(
         util_timeout_start(
            DELAI_DE_SAISIE_DU_CODE, expiration_du_delai_de_saisie_du_code, This, &d->timeout_id_delai_de_saisie_du_code ),
         __FILE__, __LINE__ );
   }
   else {
      fprintf( stderr, "Carte et/ou compte invalide\n" );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_INVALIDE ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_code_saisi( dab_controleur * This, const char * code ) {
   business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( util_timeout_cancel( d->timeout_id_delai_de_saisie_du_code ), __FILE__, __LINE__ );
   if( carte_compare_code( &d->carte, code )) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_BON_CODE ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( dab_site_central_incr_nb_essais( &This->site_central, d->carte.id ), __FILE__, __LINE__ );
      ++( d->carte.nb_essais );
      if( d->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK(
            util_timeout_start(
               DELAI_DE_SAISIE_DU_CODE, expiration_du_delai_de_saisie_du_code, This, &d->timeout_id_delai_de_saisie_du_code ),
            __FILE__, __LINE__ );
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_1 ), __FILE__, __LINE__ );
      }
      else if( d->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK(
            util_timeout_start(
               DELAI_DE_SAISIE_DU_CODE, expiration_du_delai_de_saisie_du_code, This, &d->timeout_id_delai_de_saisie_du_code ),
            __FILE__, __LINE__ );
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_2 ), __FILE__, __LINE__ );
      }
      else if( d->carte.nb_essais == 3 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_3 ), __FILE__, __LINE__ );
         UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->ihm ), __FILE__, __LINE__ );
      }
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_montant_saisi( dab_controleur * This, double montant ) {
   business_logic_data * d = This->user_context;
   if( montant > d->valeur_caisse ) {
      UTIL_ERROR_CHECK( dab_ihm_ejecter_la_carte( &This->ihm ), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ), __FILE__, __LINE__ );
   }
   else if( montant > d->compte.solde ) {
      UTIL_ERROR_CHECK( dab_ihm_ejecter_la_carte( &This->ihm ), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_SOLDE_COMPTE_INSUFFISANT ), __FILE__, __LINE__ );
   }
   else {
      d->valeur_caisse -= montant;
      UTIL_ERROR_CHECK( dab_ihm_set_solde_caisse( &This->ihm, d->valeur_caisse ), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( dab_site_central_retrait( &This->site_central, d->carte.id, montant ), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MONTANT_OK ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_carte_retiree( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_RETIREE ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_controleur_billets_retires( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_BILLETS_RETIRES ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_controleur_after_dispatch( dab_controleur * This ) {
   UTIL_ERROR_CHECK( dab_ihm_set_status( &This->ihm, This->automaton.current ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_controleur_shutdown( dab_controleur * This ) {
   This->running = false;
   UTIL_ERROR_CHECK( dab_ihm_shutdown( &This->ihm ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_site_central_shutdown( &This->site_central ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_TERMINATE ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in XML application file>\n\n", exename );
   return 1;
}

int main( int argc, char * argv[] ) {
   util_pair    pairs[argc];
   util_map     map;
   const char * name = NULL;
   util_args_parse( &map, (size_t)argc, pairs, argc, argv );
   if( UTIL_NO_ERROR != util_args_get_string( &map, "name", &name )) {
      return usage( argv[0] );
   }
   dab_controleur controleur;
   business_logic_data d;
   memset( &d, 0, sizeof( d ));
   util_error err = dab_controleur_init( &controleur, name, &d );
   if( UTIL_NO_ERROR == err ) {
      err = dab_controleur_run( &controleur );
   }
   if( UTIL_OS_ERROR == err ) {
      perror( util_error_messages[err] );
   }
   else if( UTIL_NO_ERROR != err ) {
      fprintf( stderr, "%s\n", util_error_messages[err] );
   }
   dab_controleur_shutdown( &controleur );
   return 0;
}
