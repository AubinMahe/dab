#include <dab/distributeur.h>
#include <dab/distributeur_ui.h>

#include <dabtypes/evenement.h>

#include <util/args.h>
#include <util/timeout.h>
#include <util/log.h>
#include <os/thread.h>
#include <os/errors.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

util_error dab_distributeur_etat_du_dab_published( dab_distributeur * This ) {
   UTIL_LOG_HERE();
   business_logic_data * bl = (business_logic_data *)This->user_context;
   bl->refresh = true;
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_ejecter_la_carte( dab_distributeur * This ) {
   UTIL_LOG_HERE();
   business_logic_data * bl = (business_logic_data *)This->user_context;
   bl->refresh = true;
   return UTIL_NO_ERROR;
   (void)This;
}

util_error dab_distributeur_ejecter_les_billets( dab_distributeur * This, double montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f", montant );
   business_logic_data * bl = (business_logic_data *)This->user_context;
   bl->montant = montant;
   bl->refresh = true;
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_confisquer_la_carte( dab_distributeur * This ) {
   UTIL_LOG_HERE();
   business_logic_data * bl = (business_logic_data *)This->user_context;
   bl->refresh = true;
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_placer_les_billets_dans_la_corbeille( dab_distributeur * This ) {
   UTIL_LOG_HERE();
   business_logic_data * bl = (business_logic_data *)This->user_context;
   bl->refresh = true;
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_shutdown( dab_distributeur * This ) {
   UTIL_LOG_HERE();
   This->running = false;
   business_logic_data * bl = (business_logic_data *)This->user_context;
   bl->shutdown = true;
   return UTIL_NO_ERROR;
}

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in XML application file>\n\n", exename );
   return 1;
}

typedef struct background_thread_context_s {
   dab_distributeur distributeur;
   util_error       err;
} background_thread_context;

static void * background_thread_routine( void * ctxt ) {
   background_thread_context * context = (background_thread_context *)ctxt;
   UTIL_LOG_HERE();
   context->err = dab_distributeur_run( &context->distributeur );
   return NULL;
}

int main( int argc, char * argv[] ) {
   fprintf( stderr, "\n" );
   UTIL_LOG_HERE();
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
   background_thread_context context;
   context.err = dab_distributeur_init( &context.distributeur, name, &d );
   context.distributeur.etat_du_dab.etat = DABTYPES_ETAT_MAINTENANCE;
   if( UTIL_NO_ERROR == context.err ) {
      os_thread thread;
      context.err = os_thread_create( &thread, background_thread_routine, &context );
      if( context.err == UTIL_NO_ERROR ) {
         dab_distributeur_create_ui( &context.distributeur );
      }
      else {
         OS_ERROR_PRINT( "os_thread_create", 6 );
         context.err = UTIL_NO_ERROR;
      }
   }
   if( UTIL_OS_ERROR == context.err ) {
      perror( util_error_messages[context.err] );
   }
   else if( UTIL_NO_ERROR != context.err ) {
      fprintf( stderr, "%s\n", util_error_messages[context.err] );
   }
   dab_distributeur_shutdown( &context.distributeur );
   UTIL_LOG_MSG( "end of main" );
   return 0;
}
