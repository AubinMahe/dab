#include "Controleur.hpp"

#include <util/Args.hpp>

#include <string.h>

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in dab.xml>\n\n", exename );
   return 1;
}

int main( int argc, char * argv[] ) {
   fprintf( stderr, "\n" );
   util::Args  args( argc, argv );
   const char * name;
   bool ok = args.getString( "name", name );
   if( ! ok ) {
      return usage( argv[0] );
   }
   try {
      dab::Controleur ctrl( name );
      try {
         ctrl.run();
      }
      catch( const std::exception & err ) {
         UTIL_LOG_ARGS( "%s", err.what());
         ctrl.shutdown();
         return 5;
      }
   }
   catch( const std::exception & err ) {
      UTIL_LOG_ARGS( "%s", err.what());
      return 6;
   }
   return 0;
}
