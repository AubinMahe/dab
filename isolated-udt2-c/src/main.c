#include <UDT/controleur.h>
#include <ISOUDT2/factory.h>

#include <os/errors.h>
#include <util/log.h>

#include <stdio.h>
#include <stdlib.h>

static util_error ISOUDT2_run( void ) {
   fprintf( stderr, "\n" );
   UTIL_LOG_HERE();
   ISOUDT2_factory factory;
   OS_CHECK(         io_winsock_init());
   UTIL_ERROR_CHECK( ISOUDT2_factory_create ( &factory ));
   UTIL_ERROR_CHECK( ISOUDT2_factory_join   ( &factory ));
   UTIL_ERROR_CHECK( ISOUDT2_factory_destroy( &factory ));
   UTIL_LOG_DONE();
   return 0;
}

int main( void ) {
   fprintf( stderr, "\n" );
   return ( ISOUDT2_run() == UTIL_NO_ERROR ) ? EXIT_SUCCESS : EXIT_FAILURE;
}
