#include <mixed/sc/ComponentFactory.hpp>

#include <util/Log.hpp>

#include <stdio.h>
#include <stdlib.h>

#include <exception>

int main( void ) {
   try {
      fprintf( stderr, "\n" );
      UTIL_LOG_HERE();
      mixed::sc::ComponentFactory factory;
      hpms::sc::Banque & component( factory.getSc());
      hpms::sc::BanqueUI ui( component );
      component.setUI( ui );
      ui.run();
      UTIL_LOG_DONE();
      return EXIT_SUCCESS;
   }
   catch( const std::exception & x ) {
      fprintf( stderr, "%s\n", x.what());
   }
   return EXIT_FAILURE;
}
