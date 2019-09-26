#include <dab/udt/unite_de_traitement.h>
#include <dab/evenement.h>

#include <stdio.h>
#include <stdlib.h>

static void dab_udt_date_set( dab_udt_date * This, byte month, ushort year ) {
   This->month= month;
   This->year = year;
}

static void dab_udt_carte_set(
   dab_udt_carte * This,
   const char *    carteID,
   const char *    code,
   byte            month,
   ushort          year,
   byte            nb_essais )
{
   strncpy( This->id  , carteID, sizeof( This->id ));
   strncpy( This->code, code   , sizeof( This->code ));
   This->nb_essais = nb_essais;
   This->is_valid   = true;
   dab_udt_date_set( &This->peremption, month, year );
}

static bool dab_udt_carte_compare_code( const dab_udt_carte * carte, const char * code ) {
   return 0 == strncmp( carte->code, code, sizeof( carte->code ));
}

void dab_udt_compte_set( dab_udt_compte * This, const char * id, double solde, bool autorise ) {
   strncpy( This->id, id, sizeof( This->id ));
   This->solde    = solde;
   This->autorise = autorise;
   This->is_valid  = true;
}

void dab_udt_compte_invalidate( dab_udt_compte * This ) {
   fprintf( stderr, "dab_udt_compte_invalidate\n" );
   This->is_valid = false;
}

static const double DAB_RETRAIT_MAX = 1000.0;

util_error dab_unite_de_traitement_init(
   dab_unite_de_traitement * This,
   const char *              intrfc,
   unsigned short            udt_port,
   const char *              sc_address,
   unsigned short            sc_port,
   const char *              dab_address,
   unsigned short            dab_port     )
{
   memset( This, 0, sizeof( dab_unite_de_traitement ));
   This->socket = socket( AF_INET, SOCK_DGRAM, 0 );
   if( This->socket == INVALID_SOCKET ) {
      return UTIL_OS_ERROR;
   }
   This->running = false;
   UTIL_ERROR_CHECK( dab_automaton_init( &This->automaton ), __FILE__, __LINE__ );
   This->valeur_caisse = 0.0;
   static struct sockaddr_in   sc_target;
   static struct sockaddr_in * sc_targets [] = { &sc_target  };
   static struct sockaddr_in   dab_target;
   static struct sockaddr_in * dab_targets[] = { &dab_target };
   UTIL_ERROR_CHECK( io_datagram_socket_init( sc_address , sc_port , &sc_target  ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_init( dab_address, dab_port, &dab_target ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_ihm_init            ( &This->dab       , This->socket, dab_targets, 1U ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_site_central_init   ( &This->sc        , This->socket, sc_targets , 1U ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK(
      dab_unite_de_traitement_dispatcher_init( &This->dispatcher, This->socket, This ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_bind( This->socket, intrfc, udt_port ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_maintenance( dab_unite_de_traitement * This, bool maintenance ) {
   if( maintenance ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAINTENANCE_ON ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAINTENANCE_OFF ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_recharger_la_caisse( dab_unite_de_traitement * This, double montant ) {
   This->valeur_caisse += montant;
   UTIL_ERROR_CHECK( dab_ihm_set_solde_caisse( &This->dab, This->valeur_caisse ), __FILE__, __LINE__ );
   if( This->valeur_caisse < DAB_RETRAIT_MAX ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_SOLDE_CAISSE_INSUFFISANT ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_anomalie( dab_unite_de_traitement * This, bool anomalie ) {
   if( anomalie ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_ANOMALIE_ON ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_ANOMALIE_OFF ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_carte_inseree( dab_unite_de_traitement * This, const char * id ) {
   This->carte.is_valid = false;
   This->compte.is_valid = false;
   UTIL_ERROR_CHECK( dab_site_central_get_informations( &This->sc, id ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_INSEREE ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_carte_lue( dab_unite_de_traitement * This, const dab_carte * carte, const dab_compte * compte ) {
   dab_udt_carte_set( &This->carte, carte->id, carte->code, carte->month, carte->year, carte->nb_essais );
   dab_udt_compte_set( &This->compte, compte->id, compte->solde, compte->autorise );
   if( This->carte.is_valid && This->compte.is_valid ) {
      if( This->carte.nb_essais == 0 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_0 ), __FILE__, __LINE__ );
      }
      else if( This->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_1 ), __FILE__, __LINE__ );
      }
      else if( This->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_LUE_2 ), __FILE__, __LINE__ );
      }
      else {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_CONFISQUEE ), __FILE__, __LINE__ );
         UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->dab ), __FILE__, __LINE__ );
      }
   }
   else {
      fprintf( stderr, "Carte et/ou compte invalide\n" );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_INVALIDE ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_code_saisi( dab_unite_de_traitement * This, const char * code ) {
   if( dab_udt_carte_compare_code( &This->carte, code )) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_BON_CODE ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( dab_site_central_incr_nb_essais( &This->sc, This->carte.id ), __FILE__, __LINE__ );
      ++( This->carte.nb_essais );
      if( This->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_1 ), __FILE__, __LINE__ );
      }
      else if( This->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_2 ), __FILE__, __LINE__ );
      }
      else if( This->carte.nb_essais == 3 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_MAUVAIS_CODE_3 ), __FILE__, __LINE__ );
         UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->dab ), __FILE__, __LINE__ );
      }
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_montant_saisi( dab_unite_de_traitement * This, double montant ) {
   This->valeur_caisse -= montant;
   UTIL_ERROR_CHECK( dab_ihm_set_solde_caisse( &This->dab, This->valeur_caisse ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_site_central_retrait( &This->sc, This->carte.id, montant ), __FILE__, __LINE__ );
   return util_automaton_process( &This->automaton, DAB_EVENEMENT_MONTANT_OK );
}

util_error dab_unite_de_traitement_carte_retiree( dab_unite_de_traitement * This ) {
   return util_automaton_process( &This->automaton, DAB_EVENEMENT_CARTE_RETIREE );
}

util_error dab_unite_de_traitement_billets_retires( dab_unite_de_traitement * This ) {
   return util_automaton_process( &This->automaton, DAB_EVENEMENT_BILLETS_RETIRES );
}

util_error dab_unite_de_traitement_shutdown( dab_unite_de_traitement * This ) {
   This->running = false;
   UTIL_ERROR_CHECK( dab_ihm_shutdown( &This->dab ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_site_central_shutdown( &This->sc ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENEMENT_TERMINATE ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_run( dab_unite_de_traitement * This ) {
   This->running = true;
   while( This->running ) {
      bool has_dispatched;
      UTIL_ERROR_CHECK( dab_unite_de_traitement_dispatcher_dispatch( &This->dispatcher, &has_dispatched ), __FILE__, __LINE__ );
      if( has_dispatched ) {
         UTIL_ERROR_CHECK( dab_ihm_set_status( &This->dab, This->automaton.current ), __FILE__, __LINE__ );
      }
   }
   return UTIL_NO_ERROR;
}
