#include <UDT/controleur.h>
#include <isolated_udt2/factory.h>

#include <os/errors.h>
#include <util/log.h>

#include <stdio.h>
#include <stdlib.h>

static util_error isolated_udt2_run( void ) {
   fprintf( stderr, "\n" );
   UTIL_LOG_HERE();
   isolated_udt2_factory factory;
   OS_CHECK(         io_winsock_init());
   UTIL_ERROR_CHECK( isolated_udt2_factory_create ( &factory ));
   UTIL_ERROR_CHECK( isolated_udt2_factory_join   ( &factory ));
   UTIL_ERROR_CHECK( isolated_udt2_factory_destroy( &factory ));
   UTIL_LOG_DONE();
   return 0;
}

int main( void ) {
   fprintf( stderr, "\n" );
   return ( isolated_udt2_run() == UTIL_NO_ERROR ) ? EXIT_SUCCESS : EXIT_FAILURE;
}
