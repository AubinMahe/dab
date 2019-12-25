#include <allin_one/factory.h>
#include <DAB/distributeur_ui.h>

#include <os/errors.h>
#include <util/log.h>

#include <stdio.h>
#include <stdlib.h>

static util_error allin_one_run( void ) {
   fprintf( stderr, "\n" );
   UTIL_LOG_HERE();
   allin_one_factory factory;
   OS_CHECK(         io_winsock_init());
   UTIL_ERROR_CHECK( allin_one_factory_create ( &factory ));
   UTIL_ERROR_CHECK( DAB_distributeur_create_ui( &factory.ihm1 ));
//   UTIL_ERROR_CHECK( DAB_distributeur_create_ui( &factory.ihm2 )); // ça ne marche pas : on ne peut pas partager l'écran
   UTIL_ERROR_CHECK( allin_one_factory_join   ( &factory ));
   UTIL_ERROR_CHECK( allin_one_factory_destroy( &factory ));
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}

int main( void ) {
   fprintf( stderr, "\n" );
   return ( allin_one_run() == UTIL_NO_ERROR ) ? EXIT_SUCCESS : EXIT_FAILURE;
}
