#include <ISOSC/factory.h>
#include <SC/banque_ui.h>

#include <os/errors.h>
#include <util/log.h>

#include <stdio.h>
#include <stdlib.h>

static util_error run( void ) {
   fprintf( stderr, "\n" );
   UTIL_LOG_HERE();
   ISOSC_factory factory;
   OS_CHECK(         io_winsock_init());
   UTIL_ERROR_CHECK( ISOSC_factory_create ( &factory ));
   UTIL_ERROR_CHECK( SC_banque_create_ui( &factory.sc ));
   UTIL_ERROR_CHECK( ISOSC_factory_join   ( &factory ));
   UTIL_ERROR_CHECK( ISOSC_factory_destroy( &factory ));
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}

int main( void ) {
   fprintf( stderr, "\n" );
   return ( run() == UTIL_NO_ERROR ) ? EXIT_SUCCESS : EXIT_FAILURE;
}
