#include <dab/Distributeur.hpp>
#include <dab/DistributeurUI.hpp>

#include <dabtypes/Evenement.hpp>

#include <util/Args.hpp>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in XML application file>\n\n", exename );
   return 1;
}

int main( int argc, char * argv[] ) {
   fprintf( stderr, "\n" );
   util::Args args( argc, argv );
   const char * name = nullptr;
   if( ! args.getString( "name", name )) {
      return usage( argv[0] );
   }
   dab::Distributeur( name ).run();
   fprintf( stderr, "end of main\n" );
   return 0;
}
