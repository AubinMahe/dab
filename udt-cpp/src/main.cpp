#include "Controleur.hpp"

#include <util/Args.hpp>

#include <string.h>

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in dab.xml>\n\n", exename );
   return 1;
}

int main( int argc, char * argv[] ) {
   util::Args  args( argc, argv );
   std::string name;
   bool ok = args.getString( "name", name );
   if( ! ok ) {
      return usage( argv[0] );
   }
   dab::IUniteDeTraitement * udt = 0;
   try {
      udt = new udt::Controleur( name );
      udt->run();
   }
   catch( const std::exception & err ) {
      fprintf( stderr, "\n%s\n", err.what());
      if( udt ) {
         udt->shutdown();
      }
      return 5;
   }
   return 0;
}
