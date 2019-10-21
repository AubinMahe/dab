#include <sc/repository.h>
#include <sc/banque_ui.h>

#include <dabtypes/evenement.h>

#include <util/args.h>
#include <util/timeout.h>
#include <os/thread.h>
#include <os/errors.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

util_error sc_banque_get_informations( sc_banque * This, const char * carte_id, dabtypes_site_central_get_informations_response * response ) {
   fprintf( stderr, "%s\n", __func__ );
   business_logic_data * bl = (business_logic_data *)This->user_context;
   UTIL_ERROR_CHECK( sc_repository_get_carte ( &bl->repository, carte_id, &response->carte  ));
   UTIL_ERROR_CHECK( sc_repository_get_compte( &bl->repository, carte_id, &response->compte ));
   return UTIL_NO_ERROR;
}

util_error sc_banque_incr_nb_essais( sc_banque * This, const char * carte_id ) {
   fprintf( stderr, "%s\n", __func__ );
   business_logic_data * bl = (business_logic_data *)This->user_context;
   dabtypes_carte carte;
   UTIL_ERROR_CHECK( sc_repository_get_carte ( &bl->repository, carte_id, &carte ));
   ++carte.nb_essais;
   bl->refresh_needed = true;
   return UTIL_NO_ERROR;
}

util_error sc_banque_retrait( sc_banque * This, const char * carte_id, double montant ) {
   fprintf( stderr, "%s\n", __func__ );
   business_logic_data * bl = (business_logic_data *)This->user_context;
   dabtypes_compte compte;
   UTIL_ERROR_CHECK( sc_repository_get_compte( &bl->repository, carte_id, &compte ));
   compte.solde -= montant;
   bl->refresh_needed = true;
   return UTIL_NO_ERROR;
}

util_error sc_banque_shutdown( sc_banque * This ) {
   fprintf( stderr, "%s\n", __func__ );
   business_logic_data * bl = (business_logic_data *)This->user_context;
   bl->shutdown = true;
   return UTIL_NO_ERROR;
}

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in XML application file>\n\n", exename );
   return 1;
}

typedef struct background_thread_context_s {
   sc_banque  banque;
   util_error err;
} background_thread_context;

static void * background_thread_routine( void * ctxt ) {
   background_thread_context * context = (background_thread_context *)ctxt;
   fprintf( stderr, "sc_banque_run\n" );
   context->err = sc_banque_run( &context->banque );
   return NULL;
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
   sc_repository_init( &d.repository );
   background_thread_context context;
   fprintf( stderr, "sc_banque_init\n" );
   context.err = sc_banque_init( &context.banque, name, &d );
   if( UTIL_NO_ERROR == context.err ) {
      os_thread thread;
      context.err = os_thread_create( &thread, background_thread_routine, &context );
      if( context.err == UTIL_NO_ERROR ) {
//         sc_banque_create_ui( &context.banque );
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
   sc_banque_shutdown( &context.banque );
   fprintf( stderr, "end of main\n" );
   return 0;
}
