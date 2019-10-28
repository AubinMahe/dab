#include <dab/Distributeur.hpp>

#include <util/Args.hpp>
#include <util/Log.hpp>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in XML application file>\n\n", exename );
   return 1;
}

int main( int argc, char * argv[] ) {
   fprintf( stderr, "\n" );
   UTIL_LOG_HERE();
   util::Args args( argc, argv );
   const char * name = nullptr;
   if( ! args.getString( "name", name )) {
      return usage( argv[0] );
   }
   dab::Distributeur( name ).run();
   UTIL_LOG_DONE();
   return 0;
}
