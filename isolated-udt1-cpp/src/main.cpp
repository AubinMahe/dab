#include <isolated/udt1/ComponentFactory.hpp>

#include <util/Log.hpp>

#include <stdio.h>
#include <stdlib.h>

#include <exception>

int main( void ) {
   try {
      fprintf( stderr, "\n" );
      UTIL_LOG_HERE();
      isolated::udt1::ComponentFactory factory;
      factory.join();
      UTIL_LOG_DONE();
      return EXIT_SUCCESS;
   }
   catch( const std::exception & x ) {
      fprintf( stderr, "%s\n", x.what());
   }
   return EXIT_FAILURE;
}
