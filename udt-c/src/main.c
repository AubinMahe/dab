#include <dab/udt/unite_de_traitement.h>

#include <io/sockets.h>
#include <util/args.h>

#include <string.h>
#include <stdio.h>

static int usage( const char * exename ) {
   fprintf( stderr,
      "\nusage: %s --iface=<network interface>"
      " --udt-port=<port>"
      " --sc-address=<IP address or hostname>"
      " --sc-port=<port>"
      " --dab-address=<IP address or hostname>"
      " --dab-port=<port>\n\n",
      exename );
   return 1;
}

/**
 * Point d'entrée du programme, usage typique :
 *      UniteDeTraitement --iface=enp3s0 --udt-port=2417 --sc-address=localhost --sc-port=2416 --ui-address=localhost --ui-port=2418
 */
int main( int argc, char * argv[] ) {
   util_pair pairs[argc];
   util_map  map;
   util_args_parse( &map, argc, pairs, argc, argv );
   const char *   intrfc;
   unsigned short udtPort;
   const char *   dabAddress;
   unsigned short dabPort;
   const char *   scAddress;
   unsigned short scPort;
   bool ok =
        ( UTIL_NO_ERROR == util_args_get_string( &map, "iface"      , &intrfc   ))
      &&( UTIL_NO_ERROR == util_args_get_ushort( &map, "udt-port"   , &udtPort  ))
      &&( UTIL_NO_ERROR == util_args_get_string( &map, "sc-address" , &scAddress ))
      &&( UTIL_NO_ERROR == util_args_get_ushort( &map, "sc-port"    , &scPort    ))
      &&( UTIL_NO_ERROR == util_args_get_string( &map, "dab-address", &dabAddress ))
      &&( UTIL_NO_ERROR == util_args_get_ushort( &map, "dab-port"   , &dabPort    ));
   if( ! ok ) {
      return usage( argv[0] );
   }
   dab_unite_de_traitement udt;
   util_error err = dab_unite_de_traitement_init( &udt, intrfc, udtPort, scAddress, scPort, dabAddress, dabPort );
   if( UTIL_NO_ERROR == err ) {
      err = dab_unite_de_traitement_run( &udt );
   }
   if( UTIL_OS_ERROR == err ) {
      perror( util_error_messages[err] );
   }
   else if( UTIL_NO_ERROR != err ) {
      fprintf( stderr, "%s\n", util_error_messages[err] );
   }
   dab_unite_de_traitement_shutdown( &udt );
   return 0;
}
