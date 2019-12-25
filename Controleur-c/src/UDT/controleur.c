#include <UDT/controleur.h>
#include <DBT/evenement.h>

#include <util/timeout.h>
#include <util/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static const double UDT_RETRAIT_MAX = 1000.0;

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

typedef struct UDT_business_logic_data_s {

   carte   carte;
   compte  compte;
   double  montantDeLatransactionEnCours;

} UDT_business_logic_data;

static void date_set( date * This, byte month, ushort year ) {
   This->month= month;
   This->year = year;
}

static bool date_is_valid( date * This ) {
   return( This->month > 0 )&&( This->year > 0 );
}

static void carte_set( carte * This, const DBT_carte * source ) {
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

static void compte_set( compte * This, const DBT_compte * source ) {
   strncpy( This->id, source->id, sizeof( This->id ));
   This->solde    = source->solde;
   This->autorise = source->autorise;
   This->is_valid = ( strlen( This->id ) > 0 )&&( This->solde > 0.0 );
}

util_error UDT_controleur_init( UDT_controleur * This ) {
   memset( This, 0, sizeof( UDT_controleur ));
   This->user_context = malloc( sizeof( UDT_business_logic_data ));
   memset( This->user_context, 0, sizeof( UDT_business_logic_data ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_maintenance( UDT_controleur * This, bool maintenance ) {
   UTIL_LOG_ARGS( "maintenance = %s", maintenance ? "true" : "false" );
   if( maintenance ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_MAINTENANCE_ON ));
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_MAINTENANCE_OFF ));
   }
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_recharger_la_caisse( UDT_controleur * This, double montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f", montant );
   UTIL_CHECK_NON_NULL( This );
   UTIL_CHECK_NON_NULL( This->unite_de_traitement );
   This->etat_du_dab.solde_caisse += montant;
   if( This->etat_du_dab.solde_caisse < UDT_RETRAIT_MAX ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ));
   }
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_anomalie( UDT_controleur * This, bool anomalie ) {
   UTIL_LOG_ARGS( "anomalie = %s", anomalie ? "true" : "false" );
   if( anomalie ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_ANOMALIE_ON ));
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_ANOMALIE_OFF ));
   }
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_carte_inseree( UDT_controleur * This, const char * id ) {
   UTIL_LOG_ARGS( "id = %s", id );
   UDT_business_logic_data * d = This->user_context;
   d->carte.is_valid = false;
   d->compte.is_valid = false;
   UTIL_ERROR_CHECK( UDT_site_central_informations( This->site_central, id ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_CARTE_INSEREE ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_site_central_response_informations( UDT_controleur * This, DBT_information * information ) {
   UTIL_LOG_ARGS( "carte.id = %s, carte.nb_essais = %d, compte.autorise = %s, compte.solde = %7.2f",
      information->carte.id, information->carte.nb_essais,
      information->compte.autorise ? "true" : "false", information->compte.solde );
   UDT_business_logic_data * d = This->user_context;
   carte_set ( &d->carte , &information->carte );
   compte_set( &d->compte, &information->compte );
   if( d->carte.is_valid && d->compte.is_valid ) {
      if( d->carte.nb_essais == 0 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_CARTE_LUE_0 ));
      }
      else if( d->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_CARTE_LUE_1 ));
      }
      else if( d->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_CARTE_LUE_2 ));
      }
      else {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_CARTE_CONFISQUEE ));
         UTIL_ERROR_CHECK( UDT_ihm_confisquer_la_carte( This->ihm ));
         return UTIL_NO_ERROR;
      }
   }
   else {
      UTIL_LOG_MSG( "Carte et/ou compte invalide" );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_CARTE_INVALIDE ));
   }
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_code_saisi( UDT_controleur * This, const char * code ) {
   UTIL_LOG_ARGS( "code = %s", code );
   UDT_business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->saisie_du_code ));
   if( carte_compare_code( &d->carte, code )) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_BON_CODE ));
   }
   else {
      UTIL_ERROR_CHECK( UDT_site_central_incr_nb_essais( This->site_central, d->carte.id ));
      ++( d->carte.nb_essais );
      if( d->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_MAUVAIS_CODE_1 ));
      }
      else if( d->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_MAUVAIS_CODE_2 ));
      }
      else if( d->carte.nb_essais == 3 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_MAUVAIS_CODE_3 ));
         UTIL_ERROR_CHECK( UDT_ihm_confisquer_la_carte( This->ihm ));
      }
   }
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_montant_saisi( UDT_controleur * This, double montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f", montant );
   UDT_business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( UDT_ihm_ejecter_la_carte( This->ihm ));
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->saisie_du_montant ));
   if( montant > This->etat_du_dab.solde_caisse ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ));
   }
   else if( montant > d->compte.solde ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_SOLDE_COMPTE_INSUFFISANT ));
   }
   else {
      d->montantDeLatransactionEnCours = montant;
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_MONTANT_OK ));
   }
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_carte_retiree( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UDT_business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( UDT_site_central_retrait( This->site_central, d->carte.id, d->montantDeLatransactionEnCours ));
   UTIL_ERROR_CHECK( UDT_ihm_ejecter_les_billets( This->ihm, d->montantDeLatransactionEnCours ));
   This->etat_du_dab.solde_caisse -= d->montantDeLatransactionEnCours;
   d->montantDeLatransactionEnCours = 0.0;
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->retrait_de_la_carte ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_CARTE_RETIREE ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_billets_retires( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_BILLETS_RETIRES ));
   return UTIL_NO_ERROR;
}

/**
 * Appelée par l'automate dès qu'on quite l'état UDT_STATE_RETRAIT_BILLETS
 */
util_error UDT_controleur_annuler_le_timeout_de_retrait_des_billets( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->retrait_des_billets ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_annulation_demandee_par_le_client( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UDT_business_logic_data * d = This->user_context;
   d->montantDeLatransactionEnCours = 0.0;
   UTIL_ERROR_CHECK( UDT_ihm_ejecter_la_carte( This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_ANNULATION_CLIENT ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_before_dispatch( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   return UTIL_NO_ERROR;
   (void)This;
}

util_error UDT_controleur_after_dispatch( UDT_controleur * This, bool hasDispatched ) {
   UTIL_CHECK_NON_NULL( This->unite_de_traitement );
   UTIL_LOG_ARGS( "state = %s, solde caisse : %7.2f",
      DBT_etat_to_string((DBT_etat)This->automaton.current ), This->etat_du_dab.solde_caisse );
   This->etat_du_dab.etat = (DBT_etat)This->automaton.current;
   if( hasDispatched ) {
      UTIL_ERROR_CHECK( UDT_unite_de_traitement_data_publish_etat_du_dab( This->unite_de_traitement, &This->etat_du_dab ));
   }
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_confisquer_la_carte( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( UDT_ihm_confisquer_la_carte( This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_DELAI_EXPIRE ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_placer_les_billets_dans_la_corbeille( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( UDT_ihm_placer_les_billets_dans_la_corbeille( This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_DELAI_EXPIRE ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_saisie_du_code_elapsed( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( UDT_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_saisie_du_montant_elapsed( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( UDT_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_retrait_de_la_carte_elapsed( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( UDT_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_retrait_des_billets_elapsed( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( UDT_controleur_placer_les_billets_dans_la_corbeille( This ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_armer_le_timeout_de_saisie_du_code( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_start( &This->saisie_du_code ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_armer_le_timeout_de_saisie_du_montant( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_start( &This->saisie_du_montant ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_armer_le_timeout_de_retrait_de_la_carte( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_start( &This->retrait_de_la_carte ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_armer_le_timeout_de_retrait_des_billets( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_start( &This->retrait_des_billets ));
   return UTIL_NO_ERROR;
}

util_error UDT_controleur_arret( UDT_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( UDT_ihm_arret( This->ihm ));
   UTIL_ERROR_CHECK( UDT_site_central_arret( This->site_central ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DBT_EVENEMENT_TERMINATE ));
   free( This->user_context );
   This->user_context = NULL;
   UTIL_ERROR_CHECK( UDT_controleur_dispatcher_terminate( This->dispatcher ));
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}
