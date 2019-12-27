#include <mixed/dab2/ComponentFactory.hpp>

#include <util/Log.hpp>

#include <stdio.h>
#include <stdlib.h>

#include <exception>

int main( void ) {
   try {
      fprintf( stderr, "\n" );
      UTIL_LOG_HERE();
      mixed::dab2::ComponentFactory factory;
      hpms::dab::Distributeur & component( factory.getIhm2());
      hpms::dab::DistributeurUI ui( component );
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
