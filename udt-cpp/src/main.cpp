#include "hpms/udt/Controleur.hpp"

#include <isolated/udt1/ComponentFactory.hpp>
#include <isolated/udt2/ComponentFactory.hpp>

#include <util/Args.hpp>
#include <util/Log.hpp>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in dab.xml>\n\n", exename );
   return 1;
}

int main( int argc, char * argv[] ) {
   fprintf( stderr, "\n" );
   UTIL_LOG_HERE();
   util::Args  args( argc, argv );
   const char * name = nullptr;
   if( ! args.getString( "name", name )) {
      return usage( argv[0] );
   }
   os::Thread * thread;
   if( 0 == strcmp( name, "isolated.udt1" )) {
      thread = new isolated::udt1::ComponentFactory();
   }
   else if( 0 == strcmp( name, "isolated.ihm2" )) {
      thread = new isolated::udt2::ComponentFactory();
   }
   else {
      fprintf( stderr, "'%s' isn't a valid deployment.process name\n", name );
      exit( 1 );
   }
   thread->join();
   delete thread;
   return 0;
}
