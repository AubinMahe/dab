#include <hpms/dab/Distributeur.hpp>
#include <hpms/dab/DistributeurUI.hpp>
#include <isolated/ihm1/ComponentFactory.hpp>
#include <isolated/ihm2/ComponentFactory.hpp>

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
   if( 0 == strcmp( name, "isolated.ihm1" )) {
      hpms::dab::DistributeurUI( isolated::ihm1::ComponentFactory().getIhm1()).run();
   }
   else if( 0 == strcmp( name, "isolated.ihm2" )) {
      hpms::dab::DistributeurUI( isolated::ihm2::ComponentFactory().getIhm2()).run();
   }
   else {
      fprintf( stderr, "'%s' isn't a valid deployment.process name\n", name );
      exit( 1 );
   }
   UTIL_LOG_DONE();
   return 0;
}
