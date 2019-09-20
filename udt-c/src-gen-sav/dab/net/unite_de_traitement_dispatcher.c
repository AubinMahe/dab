#include <dab/unite_de_traitement_dispatcher.h>
#include <stdio.h>

typedef enum dab_unite_de_traitement_dispatcher_interface_e {
   UNITE_DE_TRAITEMENT = 2,
   LECTEUR_DE_CARTE    = 3,
} dab_unite_de_traitement_dispatcher_interface;

typedef enum dab_unite_de_traitement_dispatcher_unite_de_traitement_event_e {
   UNITE_DE_TRAITEMENT_MAINTENANCE = 1,
   UNITE_DE_TRAITEMENT_RECHARGER_LA_CAISSE,
   UNITE_DE_TRAITEMENT_ANOMALIE,
   UNITE_DE_TRAITEMENT_LIRE_LA_CARTE,
   UNITE_DE_TRAITEMENT_CODE_SAISI,
   UNITE_DE_TRAITEMENT_MONTANT_SAISI,
   UNITE_DE_TRAITEMENT_CARTE_RETIREE,
   UNITE_DE_TRAITEMENT_BILLETS_RETIRES,
   UNITE_DE_TRAITEMENT_SHUTDOWN,
} dab_unite_de_traitement_dispatcher_unite_de_traitement_event;

static util_error dab_unite_de_traitement_dispatch(
   dab_unite_de_traitement_dispatcher *                         This,
   dab_unite_de_traitement_dispatcher_unite_de_traitement_event event,
   bool *                                                       has_dispatched )
{
   switch( event ) {
   case UNITE_DE_TRAITEMENT_MAINTENANCE:{
      bool maintenance;
      UTIL_ERROR_CHECK( io_byte_buffer_get_bool( &This->in, &maintenance ), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( dab_unite_de_traitement_maintenance( This->listener, maintenance ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   case UNITE_DE_TRAITEMENT_RECHARGER_LA_CAISSE:{
      double montant;
      UTIL_ERROR_CHECK( io_byte_buffer_get_double( &This->in, &montant ), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( dab_unite_de_traitement_recharger_la_caisse( This->listener, montant ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   case UNITE_DE_TRAITEMENT_ANOMALIE:{
      bool anomalie;
      UTIL_ERROR_CHECK( io_byte_buffer_get_bool( &This->in, &anomalie ), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( dab_unite_de_traitement_anomalie( This->listener, anomalie ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   case UNITE_DE_TRAITEMENT_LIRE_LA_CARTE:{
      char carteID[5];
      UTIL_ERROR_CHECK( io_byte_buffer_get_string( &This->in, carteID, sizeof( carteID )), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( dab_unite_de_traitement_lire_la_carte( This->listener, carteID ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   case UNITE_DE_TRAITEMENT_CODE_SAISI:{
      char code[5];
      UTIL_ERROR_CHECK( io_byte_buffer_get_string( &This->in, code, sizeof( code )), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( dab_unite_de_traitement_code_saisi( This->listener, code ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   case UNITE_DE_TRAITEMENT_MONTANT_SAISI:{
      double montant;
      UTIL_ERROR_CHECK( io_byte_buffer_get_double( &This->in, &montant ), __FILE__, __LINE__ );
      UTIL_ERROR_CHECK( dab_unite_de_traitement_montant_saisi( This->listener, montant ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   case UNITE_DE_TRAITEMENT_CARTE_RETIREE:{
      UTIL_ERROR_CHECK( dab_unite_de_traitement_carte_retiree( This->listener ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   case UNITE_DE_TRAITEMENT_BILLETS_RETIRES:{
      UTIL_ERROR_CHECK( dab_unite_de_traitement_billets_retires( This->listener ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   case UNITE_DE_TRAITEMENT_SHUTDOWN:{
      UTIL_ERROR_CHECK( dab_unite_de_traitement_shutdown( This->listener ), __FILE__, __LINE__ );
      *has_dispatched = true;
   break;
   }
   default:
      fprintf( stderr, "dab_unite_de_traitement_dispatcher|Message reçu ignoré\n" );
      fprintf( stderr, "\tinterface = UniteDeTraitement\n" );
      fprintf( stderr, "\tevent     = %d\n", event );
      break;
   }
   return UTIL_NO_ERROR;
}

typedef enum dab_unite_de_traitement_dispatcher_lecteur_de_carte_event_e {
   LECTEUR_DE_CARTE_CARTE_LUE = 1,
} dab_unite_de_traitement_dispatcher_lecteur_de_carte_event;

static util_error dab_lecteur_de_carte_dispatch(
   dab_unite_de_traitement_dispatcher *                      This,
   dab_unite_de_traitement_dispatcher_lecteur_de_carte_event event,
   bool *                                                    has_dispatched )
{
   *has_dispatched = false;
   switch( event ) {
   case LECTEUR_DE_CARTE_CARTE_LUE:{
      dab_carte carte;
      dab_carte_get( &carte, &This->in );
      dab_compte compte;
      dab_compte_get( &compte, &This->in );
      UTIL_ERROR_CHECK( dab_unite_de_traitement_carte_lue( This->listener, &carte, &compte ), __FILE__, __LINE__ );
      *has_dispatched = true;
      break;
   }
   default:
      fprintf( stderr, "dab_unite_de_traitement_dispatcher|Message reçu ignoré\n" );
      fprintf( stderr, "\tinterface = LecteurDeCarte\n" );
      fprintf( stderr, "\tevent     = %d\n", event );
      break;
   }
   return UTIL_NO_ERROR;
}

util_error dab_unite_de_traitement_dispatcher_init( dab_unite_de_traitement_dispatcher * This, SOCKET socket, dab_unite_de_traitement * listener ) {
   if( socket <= 0 || NULL == listener ) {
      return UTIL_NULL_ARG;
   }
   This->socket   = socket;
   This->listener = listener;
   return io_byte_buffer_wrap( &This->in, 39, This->raw );
}

util_error dab_unite_de_traitement_dispatcher_dispatch( dab_unite_de_traitement_dispatcher * This, bool * has_dispatched ) {
   struct sockaddr_in from;
   byte interface, event;

   *has_dispatched = false;
   io_byte_buffer_clear( &This->in );
   UTIL_ERROR_CHECK( io_datagram_socket_receive( This->socket, &This->in, &from ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->in ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_get_byte( &This->in, &interface ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_get_byte( &This->in, &event ), __FILE__, __LINE__ );
   switch( interface ) {
   case UNITE_DE_TRAITEMENT: UTIL_ERROR_CHECK( dab_unite_de_traitement_dispatch( This, event, has_dispatched ), __FILE__, __LINE__ ); break;
   case LECTEUR_DE_CARTE   : UTIL_ERROR_CHECK( dab_lecteur_de_carte_dispatch   ( This, event, has_dispatched ), __FILE__, __LINE__ ); break;
   }
   return UTIL_NO_ERROR;
}
