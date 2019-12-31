#include <ISOIHM2/factory.h>
#include <DAB/distributeur_ui.h>

#include <os/errors.h>
#include <util/log.h>

#include <stdio.h>
#include <stdlib.h>

static util_error ISOIHM2_run( void ) {
   fprintf( stderr, "\n" );
   UTIL_LOG_HERE();
   ISOIHM2_factory factory;
   OS_CHECK(         io_winsock_init());
   UTIL_ERROR_CHECK( ISOIHM2_factory_create ( &factory ));
   UTIL_ERROR_CHECK( DAB_distributeur_create_ui( &factory.ihm2 ));
   UTIL_ERROR_CHECK( ISOIHM2_factory_join   ( &factory ));
   UTIL_ERROR_CHECK( ISOIHM2_factory_destroy( &factory ));
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}

int main( void ) {
   fprintf( stderr, "\n" );
   return ( ISOIHM2_run() == UTIL_NO_ERROR ) ? EXIT_SUCCESS : EXIT_FAILURE;
}
