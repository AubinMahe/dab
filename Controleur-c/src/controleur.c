#include <udt/controleur.h>
#include <dabtypes/evenement.h>

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

typedef struct business_logic_data_s {
   carte  carte;
   compte compte;
   double montantDeLatransactionEnCours;
} business_logic_data;

static void date_set( date * This, byte month, ushort year ) {
   This->month= month;
   This->year = year;
}

static bool date_is_valid( date * This ) {
   return( This->month > 0 )&&( This->year > 0 );
}

static void carte_set( carte * This, const dabtypes_carte * source ) {
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

void compte_set( compte * This, const dabtypes_compte * source ) {
   strncpy( This->id, source->id, sizeof( This->id ));
   This->solde    = source->solde;
   This->autorise = source->autorise;
   This->is_valid = ( strlen( This->id ) > 0 )&&( This->solde > 0.0 );
}

void compte_invalidate( compte * This ) {
   This->is_valid = false;
}

util_error udt_controleur_maintenance( udt_controleur * This, bool maintenance ) {
   UTIL_LOG_ARGS( "maintenance = %s", maintenance ? "true" : "false" );
   if( maintenance ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_MAINTENANCE_ON ));
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_MAINTENANCE_OFF ));
   }
   return UTIL_NO_ERROR;
}

util_error udt_controleur_recharger_la_caisse( udt_controleur * This, double montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f", montant );
   This->unite_de_traitement.etat_du_dab.solde_caisse += montant;
   if( This->unite_de_traitement.etat_du_dab.solde_caisse < UDT_RETRAIT_MAX ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ));
   }
   return UTIL_NO_ERROR;
}

util_error udt_controleur_anomalie( udt_controleur * This, bool anomalie ) {
   UTIL_LOG_ARGS( "anomalie = %s", anomalie ? "true" : "false" );
   if( anomalie ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_ANOMALIE_ON ));
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_ANOMALIE_OFF ));
   }
   return UTIL_NO_ERROR;
}

util_error udt_controleur_carte_inseree( udt_controleur * This, const char * id ) {
   UTIL_LOG_ARGS( "id = %s", id );
   business_logic_data * d = This->user_context;
   d->carte.is_valid = false;
   d->compte.is_valid = false;
   UTIL_ERROR_CHECK( udt_site_central_get_informations( &This->site_central, id ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_CARTE_INSEREE ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_site_central_response_get_informations( udt_controleur * This, const dabtypes_carte * carte, const dabtypes_compte * compte ) {
   UTIL_LOG_ARGS( "carte.id = %s, carte.nb_essais = %d, compte.autorise = %s, compte.solde = %7.2f",
      carte->id, carte->nb_essais, compte->autorise ? "true" : "false", compte->solde );
   business_logic_data * d = This->user_context;
   carte_set( &d->carte, carte );
   compte_set( &d->compte, compte );
   if( d->carte.is_valid && d->compte.is_valid ) {
      if( d->carte.nb_essais == 0 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_CARTE_LUE_0 ));
      }
      else if( d->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_CARTE_LUE_1 ));
      }
      else if( d->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_CARTE_LUE_2 ));
      }
      else {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_CARTE_CONFISQUEE ));
         UTIL_ERROR_CHECK( udt_ihm_confisquer_la_carte( &This->ihm ));
         return UTIL_NO_ERROR;
      }
   }
   else {
      UTIL_LOG_MSG( "Carte et/ou compte invalide" );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_CARTE_INVALIDE ));
   }
   return UTIL_NO_ERROR;
}

util_error udt_controleur_code_saisi( udt_controleur * This, const char * code ) {
   UTIL_LOG_ARGS( "code = %s", code );
   business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->saisie_du_code ));
   if( carte_compare_code( &d->carte, code )) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_BON_CODE ));
   }
   else {
      UTIL_ERROR_CHECK( udt_site_central_incr_nb_essais( &This->site_central, d->carte.id ));
      ++( d->carte.nb_essais );
      if( d->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_MAUVAIS_CODE_1 ));
      }
      else if( d->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_MAUVAIS_CODE_2 ));
      }
      else if( d->carte.nb_essais == 3 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_MAUVAIS_CODE_3 ));
         UTIL_ERROR_CHECK( udt_ihm_confisquer_la_carte( &This->ihm ));
      }
   }
   return UTIL_NO_ERROR;
}

util_error udt_controleur_montant_saisi( udt_controleur * This, double montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f", montant );
   business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( udt_ihm_ejecter_la_carte( &This->ihm ));
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->saisie_du_montant ));
   if( montant > This->unite_de_traitement.etat_du_dab.solde_caisse ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ));
   }
   else if( montant > d->compte.solde ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_SOLDE_COMPTE_INSUFFISANT ));
   }
   else {
      d->montantDeLatransactionEnCours = montant;
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_MONTANT_OK ));
   }
   return UTIL_NO_ERROR;
}

util_error udt_controleur_carte_retiree( udt_controleur * This ) {
   UTIL_LOG_HERE();
   business_logic_data * d = This->user_context;
   UTIL_ERROR_CHECK( udt_site_central_retrait( &This->site_central, d->carte.id, d->montantDeLatransactionEnCours ));
   UTIL_ERROR_CHECK( udt_ihm_ejecter_les_billets( &This->ihm, d->montantDeLatransactionEnCours ));
   This->unite_de_traitement.etat_du_dab.solde_caisse -= d->montantDeLatransactionEnCours;
   d->montantDeLatransactionEnCours = 0.0;
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->retrait_de_la_carte ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_CARTE_RETIREE ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_billets_retires( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_BILLETS_RETIRES ));
   return UTIL_NO_ERROR;
}

/**
 * Appelée par l'automate dès qu'on quite l'état UDT_STATE_RETRAIT_BILLETS
 */
util_error udt_controleur_annuler_le_timeout_de_retrait_des_billets( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_cancel( &This->retrait_des_billets ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_annulation_demandee_par_le_client( udt_controleur * This ) {
   UTIL_LOG_HERE();
   business_logic_data * d = This->user_context;
   d->montantDeLatransactionEnCours = 0.0;
   UTIL_ERROR_CHECK( udt_ihm_ejecter_la_carte( &This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_ANNULATION_CLIENT ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_after_dispatch( udt_controleur * This ) {
   UTIL_LOG_ARGS( "state = %s, solde caisse : %7.2f",
      dabtypes_etat_to_string((dabtypes_etat)This->automaton.current ), This->unite_de_traitement.etat_du_dab.solde_caisse );
   This->unite_de_traitement.etat_du_dab.etat = (dabtypes_etat)This->automaton.current;
   UTIL_ERROR_CHECK( udt_unite_de_traitement_data_publish_etat_du_dab( &This->unite_de_traitement ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_confisquer_la_carte( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( udt_ihm_confisquer_la_carte( &This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_DELAI_EXPIRE ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_placer_les_billets_dans_la_corbeille( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( udt_ihm_placer_les_billets_dans_la_corbeille( &This->ihm ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_DELAI_EXPIRE ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_saisie_du_code_elapsed( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( udt_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_saisie_du_montant_elapsed( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( udt_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_retrait_de_la_carte_elapsed( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( udt_controleur_confisquer_la_carte( This ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_retrait_des_billets_elapsed( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( udt_controleur_placer_les_billets_dans_la_corbeille( This ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_armer_le_timeout_de_saisie_du_code( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_start( &This->saisie_du_code ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_armer_le_timeout_de_saisie_du_montant( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_start( &This->saisie_du_montant ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_armer_le_timeout_de_retrait_de_la_carte( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_start( &This->retrait_de_la_carte ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_armer_le_timeout_de_retrait_des_billets( udt_controleur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( util_timeout_start( &This->retrait_des_billets ));
   return UTIL_NO_ERROR;
}

util_error udt_controleur_shutdown( udt_controleur * This ) {
   UTIL_LOG_HERE();
   This->running = false;
   UTIL_ERROR_CHECK( udt_ihm_shutdown( &This->ihm ));
   UTIL_ERROR_CHECK( udt_site_central_shutdown( &This->site_central ));
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DABTYPES_EVENEMENT_TERMINATE ));
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}

