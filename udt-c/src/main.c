#include <dab/controleur.h>
#include <dab/evenement.h>

#include <util/args.h>
#include <util/timeout.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
/*
src/io/datagram_socket.c:94:io_datagram_socket_send_to:send:Bad file descriptor
src-gen/dab/controleur_dispatcher.c:212:dab_controleur_dispatcher_loopback:io_datagram_socket_send_to( This->socket, &out, &This->listener->localAddress ):os error
*/
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

static const double DAB_RETRAIT_MAX = 1000.0;

typedef struct business_logic_data_s {
   carte  carte;
   compte compte;
} business_logic_data;

util_error dab_controleur_maintenance( dab_controleur * This, bool maintenance ) {
   if( maintenance ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAINTENANCE_ON ));
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAINTENANCE_OFF ));
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_recharger_la_caisse( dab_controleur * This, double montant ) {
   This->ihm.etat_du_dab.solde_caisse += montant;
   if( This->ihm.etat_du_dab.solde_caisse < DAB_RETRAIT_MAX ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ));
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_anomalie( dab_controleur * This, bool anomalie ) {
   if( anomalie ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_ANOMALIE_ON ));
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_ANOMALIE_OFF ));
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_carte_inseree( dab_controleur * This, const char * id ) {
   business_logic_data * d = This->user_context;
   d->carte.is_valid = false;
   d->compte.is_valid = false;
   UTIL_ERROR_CHECK( dab_site_central_get_informations( &This->site_central, id ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_INSEREE ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_get_informations( dab_controleur * This, const dab_carte * carte, const dab_compte * compte ) {
   business_logic_data * d = This->user_context;
   carte_set( &d->carte, carte );
   compte_set( &d->compte, compte );
   if( d->carte.is_valid && d->compte.is_valid ) {
      if( d->carte.nb_essais == 0 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_0 ));
      }
      else if( d->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_1 ));
      }
      else if( d->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_2 ));
      }
      else {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_CONFISQUEE ));
         UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->ihm ));
         return UTIL_NO_ERROR;
      }
   }
   else {
      fprintf( stderr, "Carte et/ou compte invalide\n" );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_INVALIDE ));
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_code_saisi( dab_controleur * This, const char * code ) {
   business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->saisie_du_code ));
   if( carte_compare_code( &d->carte, code )) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_BON_CODE ));
   }
   else {
      UTIL_ERROR_CHECK( dab_site_central_incr_nb_essais( &This->site_central, d->carte.id ));
      ++( d->carte.nb_essais );
      if( d->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_1 ));
      }
      else if( d->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_2 ));
      }
      else if( d->carte.nb_essais == 3 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_3 ));
         UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->ihm ));
      }
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_montant_saisi( dab_controleur * This, double montant ) {
   business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->saisie_du_montant ));
   if( montant > This->ihm.etat_du_dab.solde_caisse ) {
      UTIL_ERROR_CHECK( dab_ihm_ejecter_la_carte( &This->ihm ));
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ));
   }
   else if( montant > d->compte.solde ) {
      UTIL_ERROR_CHECK( dab_ihm_ejecter_la_carte( &This->ihm ));
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_SOLDE_COMPTE_INSUFFISANT ));
   }
   else {
      This->ihm.etat_du_dab.solde_caisse -= montant;
      UTIL_ERROR_CHECK( dab_site_central_retrait( &This->site_central, d->carte.id, montant ));
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MONTANT_OK ));
   }
   return UTIL_NO_ERROR;
}

util_error dab_controleur_carte_retiree( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->retrait_de_la_carte ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_RETIREE ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_billets_retires( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_BILLETS_RETIRES ));
   return UTIL_NO_ERROR;
}

/**
 * Appelée par l'automate dès qu'on quite l'état DAB_STATE_RETRAIT_BILLETS
 */
util_error dab_controleur_annuler_le_timeout_de_retrait_des_billets( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->retrait_des_billets ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_annulation_demandee_par_le_client( dab_controleur * This ) {
   UTIL_ERROR_CHECK( dab_ihm_ejecter_la_carte( &This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_ANNULATION_CLIENT ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_after_dispatch( dab_controleur * This ) {
   fprintf( stderr, "%s|state = %s, solde caisse : %7.2f\n",
      __func__, dab_etat_to_string( This->automaton.current ), This->ihm.etat_du_dab.solde_caisse );
   This->ihm.etat_du_dab.etat = This->automaton.current;
   UTIL_ERROR_CHECK( dab_ihm_etat_du_dab_publish( &This->ihm ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_confisquer_la_carte( dab_controleur * This ) {
   fprintf( stderr, "%s\n", __func__ );
   UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_DELAI_EXPIRE ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_placer_les_billets_dans_la_corbeille( dab_controleur * This ) {
   fprintf( stderr, "%s\n", __func__ );
   UTIL_ERROR_CHECK( dab_ihm_placer_les_billets_dans_la_corbeille( &This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_DELAI_EXPIRE ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_saisie_du_code_elapsed( dab_controleur * This ) {
   UTIL_ERROR_CHECK( dab_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_saisie_du_montant_elapsed( dab_controleur * This ) {
   UTIL_ERROR_CHECK( dab_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_retrait_de_la_carte_elapsed( dab_controleur * This ) {
   UTIL_ERROR_CHECK( dab_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_retrait_des_billets_elapsed( dab_controleur * This ) {
   UTIL_ERROR_CHECK( dab_controleur_placer_les_billets_dans_la_corbeille( This ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_armer_le_timeout_de_saisie_du_code( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_timeout_start( &This->saisie_du_code ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_armer_le_timeout_de_saisie_du_montant( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_timeout_start( &This->saisie_du_montant ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_armer_le_timeout_de_retrait_de_la_carte( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_timeout_start( &This->retrait_de_la_carte ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_armer_le_timeout_de_retrait_des_billets( dab_controleur * This ) {
   UTIL_ERROR_CHECK( util_timeout_start( &This->retrait_des_billets ));
   return UTIL_NO_ERROR;
}

util_error dab_controleur_shutdown( dab_controleur * This ) {
   This->running = false;
   UTIL_ERROR_CHECK( dab_ihm_shutdown( &This->ihm ));
   UTIL_ERROR_CHECK( dab_site_central_shutdown( &This->site_central ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_TERMINATE ));
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
   io_winsock_init();
   business_logic_data d;
   memset( &d, 0, sizeof( d ));
   dab_controleur controleur;
   fprintf( stderr, "dab_controleur_init\n" );
   util_error err = dab_controleur_init( &controleur, name, &d );
   if( UTIL_NO_ERROR == err ) {
      fprintf( stderr, "dab_controleur_run\n" );
      controleur.automaton.debug = true;
      err = dab_controleur_run( &controleur );
   }
   if( UTIL_OS_ERROR == err ) {
      perror( util_error_messages[err] );
   }
   else if( UTIL_NO_ERROR != err ) {
      fprintf( stderr, "%s\n", util_error_messages[err] );
   }
   dab_controleur_shutdown( &controleur );
   fprintf( stderr, "dab_controleur_shutdown\n" );
   return 0;
}
