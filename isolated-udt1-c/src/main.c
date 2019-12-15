#include <udt/controleur.h>

#include <util/args.h>
#include <util/timeout.h>
#include <util/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int usage( const char * exename ) {
   fprintf( stdout, "\nusage: %s --name=<name as defined in XML application file>\n\n", exename );
   fprintf( stderr, "\nusage: %s --name=<name as defined in XML application file>\n\n", exename );
   return 1;
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
   udt_controleur controleur;
   util_error err = udt_controleur_init( &controleur, name, &d );
   if( UTIL_NO_ERROR == err ) {
      controleur.automaton.debug = true;
      err = udt_controleur_run( &controleur );
   }
   if( UTIL_OS_ERROR == err ) {
      perror( util_error_messages[err] );
   }
   else if( UTIL_NO_ERROR != err ) {
      UTIL_LOG_MSG( util_error_messages[err] );
   }
   udt_controleur_shutdown( &controleur );
   UTIL_LOG_DONE();
   return 0;
}
