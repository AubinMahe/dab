#include <dab/udt/UniteDeTraitement.hpp>

#include <util/Args.hpp>

#include <string.h>

static int usage( const char * exename ) {
   fprintf( stderr,
      "\nusage: %s --iface=<network interface>"
      " --udt-port=<port>"
      " --sc-address=<IP address or hostname>"
      " --sc-port=<port>"
      " --ui-address=<IP address or hostname>"
      " --ui-port=<port>\n\n",
      exename );
   return 1;
}

/**
 * Point d'entrée du programme, usage typique :
 *      UniteDeTraitement --iface=enp3s0 --udt-port=2417 --sc-address=localhost --sc-port=2416 --ui-address=localhost --ui-port=2418
 */
int main( int argc, char * argv[] ) {
   util::Args args( argc, argv );
   std::string    intrfc;
   unsigned short udtPort;
   std::string    uiAddress;
   unsigned short uiPort;
   std::string    scAddress;
   unsigned short scPort;
   bool ok = args.getString( "iface"     , intrfc    )
      &&     args.getUShort( "udt-port"  , udtPort   )
      &&     args.getString( "sc-address", scAddress )
      &&     args.getUShort( "sc-port"   , scPort    )
      &&     args.getString( "ui-address", uiAddress )
      &&     args.getUShort( "ui-port"   , uiPort    );
   if( ! ok ) {
      return usage( argv[0] );
   }
   dab::IUniteDeTraitement * udt = 0;
   try {
      udt = dab::udt::newUniteDeTraitement( intrfc.c_str(), udtPort, scAddress.c_str(), scPort, uiAddress.c_str(), uiPort );
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
