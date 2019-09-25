#include <dab/udt/unite_de_traitement.h>

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

typedef enum dab_event_e {
   DAB_EVENT_FIRST,

   DAB_EVENT_MAINTENANCE_ON = DAB_EVENT_FIRST,
   DAB_EVENT_MAINTENANCE_OFF,
   DAB_EVENT_SOLDE_CAISSE_INSUFFISANT,
   DAB_EVENT_ANOMALIE_ON,
   DAB_EVENT_ANOMALIE_OFF,
   DAB_EVENT_CARTE_INSEREE,
   DAB_EVENT_CARTE_LUE_0,
   DAB_EVENT_CARTE_LUE_1,
   DAB_EVENT_CARTE_LUE_2,
   DAB_EVENT_CARTE_INVALIDE,
   DAB_EVENT_BON_CODE,
   DAB_EVENT_MAUVAIS_CODE_1,
   DAB_EVENT_MAUVAIS_CODE_2,
   DAB_EVENT_MAUVAIS_CODE_3,
   DAB_EVENT_CARTE_CONFISQUEE,
   DAB_EVENT_SOLDE_INSUFFISANT,
   DAB_EVENT_MONTANT_OK,
   DAB_EVENT_CARTE_RETIREE,
   DAB_EVENT_BILLETS_RETIRES,
   DAB_EVENT_TERMINATE,

   DAB_EVENT_LAST,
} dab_event;

const double DAB_RETRAIT_MAX = 1000.0;

util_error dab_unite_de_traitement_init(
   dab_unite_de_traitement * This,
   const char *              intrfc,
   unsigned short            udt_port,
   const char *              sc_address,
   unsigned short            sc_port,
   const char *              ui_address,
   unsigned short            ui_port     )
{
   static util_arc arcs[] = {
      { DAB_ETAT_AUCUN          , DAB_EVENT_MAINTENANCE_ON          , DAB_ETAT_MAINTENANCE     },
      { DAB_ETAT_MAINTENANCE    , DAB_EVENT_MAINTENANCE_OFF         , DAB_ETAT_EN_SERVICE      },
      { DAB_ETAT_MAINTENANCE    , DAB_EVENT_SOLDE_CAISSE_INSUFFISANT, DAB_ETAT_MAINTENANCE     },
      { DAB_ETAT_EN_SERVICE     , DAB_EVENT_MAINTENANCE_ON          , DAB_ETAT_MAINTENANCE     },
      { DAB_ETAT_MAINTENANCE    , DAB_EVENT_ANOMALIE_ON             , DAB_ETAT_HORS_SERVICE    },
      { DAB_ETAT_HORS_SERVICE   , DAB_EVENT_ANOMALIE_OFF            , DAB_ETAT_MAINTENANCE     },
      { DAB_ETAT_HORS_SERVICE   , DAB_EVENT_MAINTENANCE_ON          , DAB_ETAT_MAINTENANCE     },
      { DAB_ETAT_EN_SERVICE     , DAB_EVENT_SOLDE_CAISSE_INSUFFISANT, DAB_ETAT_HORS_SERVICE    },
      { DAB_ETAT_EN_SERVICE     , DAB_EVENT_CARTE_INSEREE           , DAB_ETAT_LECTURE_CARTE   },
      { DAB_ETAT_LECTURE_CARTE  , DAB_EVENT_CARTE_LUE_0             , DAB_ETAT_SAISIE_CODE_1   },
      { DAB_ETAT_LECTURE_CARTE  , DAB_EVENT_CARTE_LUE_1             , DAB_ETAT_SAISIE_CODE_2   },
      { DAB_ETAT_LECTURE_CARTE  , DAB_EVENT_CARTE_LUE_2             , DAB_ETAT_SAISIE_CODE_3   },
      { DAB_ETAT_LECTURE_CARTE  , DAB_EVENT_CARTE_INVALIDE          , DAB_ETAT_EN_SERVICE      },
      { DAB_ETAT_LECTURE_CARTE  , DAB_EVENT_CARTE_CONFISQUEE        , DAB_ETAT_EN_SERVICE      },
      { DAB_ETAT_SAISIE_CODE_1  , DAB_EVENT_BON_CODE                , DAB_ETAT_SAISIE_MONTANT  },
      { DAB_ETAT_SAISIE_CODE_1  , DAB_EVENT_MAUVAIS_CODE_1          , DAB_ETAT_SAISIE_CODE_2   },
      { DAB_ETAT_SAISIE_CODE_2  , DAB_EVENT_BON_CODE                , DAB_ETAT_SAISIE_MONTANT  },
      { DAB_ETAT_SAISIE_CODE_2  , DAB_EVENT_MAUVAIS_CODE_2          , DAB_ETAT_SAISIE_CODE_3   },
      { DAB_ETAT_SAISIE_CODE_3  , DAB_EVENT_BON_CODE                , DAB_ETAT_SAISIE_MONTANT  },
      { DAB_ETAT_SAISIE_CODE_3  , DAB_EVENT_MAUVAIS_CODE_3          , DAB_ETAT_EN_SERVICE      },
      { DAB_ETAT_SAISIE_MONTANT , DAB_EVENT_MONTANT_OK              , DAB_ETAT_RETRAIT_CARTE   },
      { DAB_ETAT_RETRAIT_CARTE  , DAB_EVENT_CARTE_RETIREE           , DAB_ETAT_RETRAIT_BILLETS },
      { DAB_ETAT_RETRAIT_BILLETS, DAB_EVENT_BILLETS_RETIRES         , DAB_ETAT_EN_SERVICE      },
   };
   static util_shortcut shortcuts[] = {
      { DAB_EVENT_TERMINATE  , DAB_ETAT_HORS_SERVICE },
      { DAB_EVENT_ANOMALIE_ON, DAB_ETAT_HORS_SERVICE },
   };

   memset( This, 0, sizeof( dab_unite_de_traitement ));
   UTIL_ERROR_CHECK(
      util_automaton_init( &This->automaton, DAB_ETAT_AUCUN,
         arcs     , sizeof( arcs )/sizeof( arcs[0]),
         shortcuts, sizeof( shortcuts )/sizeof( shortcuts[0])), __FILE__, __LINE__ );
   This->valeur_caisse = 0.0;
   This->socket = socket( AF_INET, SOCK_DGRAM, 0 );
   if( This->socket == INVALID_SOCKET ) {
      return UTIL_OS_ERROR;
   }
   UTIL_ERROR_CHECK( dab_ihm_init                           ( &This->ui        , This->socket ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_site_central_init                  ( &This->sc        , This->socket ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_unite_de_traitement_dispatcher_init( &This->dispatcher, This->socket, This ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_bind( This->socket, intrfc, udt_port ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_init( sc_address, sc_port, &This->sc_target ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_init( ui_address, ui_port, &This->ui_target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

//--------------------------------------------------------------------------------------------------
// E4 : La mise en service d'un DAB est faite manuellement par l'opérateur
//--------------------------------------------------------------------------------------------------
util_error dab_unite_de_traitement_maintenance( dab_unite_de_traitement * This, bool maintenance ) {
   if( maintenance ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_MAINTENANCE_ON ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_MAINTENANCE_OFF ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

//--------------------------------------------------------------------------------------------------
// E1 : Le DAB peut déclencher sa mise hors service, lorsqu'il détecte que sa caisse comporte un
// maximum autorisé pour un retrait (1 000 €)
// E5 : l'opérateur est chargé du rechargement de la caisse du DAB.
//--------------------------------------------------------------------------------------------------
util_error dab_unite_de_traitement_recharger_la_caisse( dab_unite_de_traitement * This, double montant ) {
   This->valeur_caisse += montant;
   UTIL_ERROR_CHECK( dab_ihm_set_solde_caisse( &This->ui, &This->ui_target, This->valeur_caisse ), __FILE__, __LINE__ );
   if( This->valeur_caisse < DAB_RETRAIT_MAX ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_SOLDE_CAISSE_INSUFFISANT ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_anomalie( dab_unite_de_traitement * This, bool anomalie ) {
   if( anomalie ) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_ANOMALIE_ON ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_ANOMALIE_OFF ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

//--------------------------------------------------------------------------------------------------
// E20 : Le calcul du montant maximum du retrait auquel le client a droit, en fonction du solde du
// laquelle tous les clients ont droit (montant fixé à 1000 €) : la somme maximum que le client peut
// retirer correspond au minimum de ces deux valeurs.
//--------------------------------------------------------------------------------------------------
//static double dab_unite_de_traitement_get_retrait_max( dab_unite_de_traitement * This ) {
//   return ( This->compte.solde < DAB_RETRAIT_MAX ) ? This->compte.solde : DAB_RETRAIT_MAX;
//}

util_error dab_unite_de_traitement_carte_inseree( dab_unite_de_traitement * This, const char * id ) {
   This->carte.is_valid = false;
   This->compte.is_valid = false;
   UTIL_ERROR_CHECK( dab_site_central_get_informations( &This->sc, &This->sc_target, id ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_CARTE_INSEREE ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_carte_lue( dab_unite_de_traitement * This, const dab_carte * carte, const dab_compte * compte ) {
   dab_udt_carte_set( &This->carte, carte->id, carte->code, carte->month, carte->year, carte->nb_essais );
   dab_udt_compte_set( &This->compte, compte->id, compte->solde, compte->autorise );
   if( This->carte.is_valid && This->compte.is_valid ) {
      if( This->carte.nb_essais == 0 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_CARTE_LUE_0 ), __FILE__, __LINE__ );
      }
      else if( This->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_CARTE_LUE_1 ), __FILE__, __LINE__ );
      }
      else if( This->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_CARTE_LUE_2 ), __FILE__, __LINE__ );
      }
      else {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_CARTE_CONFISQUEE ), __FILE__, __LINE__ );
         UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->ui, &This->ui_target ), __FILE__, __LINE__ );
      }
   }
   else {
      fprintf( stderr, "Carte et/ou compte invalide\n" );
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_CARTE_INVALIDE ), __FILE__, __LINE__ );
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_code_saisi( dab_unite_de_traitement * This, const char * code ) {
   if( dab_udt_carte_compare_code( &This->carte, code )) {
      UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_BON_CODE ), __FILE__, __LINE__ );
   }
   else {
      UTIL_ERROR_CHECK( dab_site_central_incr_nb_essais( &This->sc, &This->sc_target, This->carte.id ), __FILE__, __LINE__ );
      ++( This->carte.nb_essais );
      if( This->carte.nb_essais == 1 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_MAUVAIS_CODE_1 ), __FILE__, __LINE__ );
      }
      else if( This->carte.nb_essais == 2 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_MAUVAIS_CODE_2 ), __FILE__, __LINE__ );
      }
      else if( This->carte.nb_essais == 3 ) {
         UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_MAUVAIS_CODE_3 ), __FILE__, __LINE__ );
         UTIL_ERROR_CHECK( dab_ihm_confisquer_la_carte( &This->ui, &This->ui_target ), __FILE__, __LINE__ );
      }
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_montant_saisi( dab_unite_de_traitement * This, double montant ) {
   This->valeur_caisse -= montant;
   UTIL_ERROR_CHECK( dab_ihm_set_solde_caisse( &This->ui, &This->ui_target, This->valeur_caisse ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_site_central_retrait( &This->sc, &This->sc_target, This->carte.id, montant ), __FILE__, __LINE__ );
   return util_automaton_process( &This->automaton, DAB_EVENT_MONTANT_OK );
}

util_error dab_unite_de_traitement_carte_retiree( dab_unite_de_traitement * This ) {
   return util_automaton_process( &This->automaton, DAB_EVENT_CARTE_RETIREE );
}

util_error dab_unite_de_traitement_billets_retires( dab_unite_de_traitement * This ) {
   return util_automaton_process( &This->automaton, DAB_EVENT_BILLETS_RETIRES );
}

util_error dab_unite_de_traitement_shutdown( dab_unite_de_traitement * This ) {
   This->running = false;
   UTIL_ERROR_CHECK( dab_ihm_shutdown( &This->ui, &This->ui_target ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( dab_site_central_shutdown( &This->sc, &This->sc_target ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( util_automaton_process( &This->automaton, DAB_EVENT_TERMINATE ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_run( dab_unite_de_traitement * This ) {
   This->running = true;
   while( This->running ) {
      bool has_dispatched;
      UTIL_ERROR_CHECK( dab_unite_de_traitement_dispatcher_dispatch( &This->dispatcher, &has_dispatched ), __FILE__, __LINE__ );
      if( has_dispatched ) {
         UTIL_ERROR_CHECK( dab_ihm_set_status( &This->ui, &This->ui_target, This->automaton.current ), __FILE__, __LINE__ );
      }
   }
   return UTIL_NO_ERROR;
}
