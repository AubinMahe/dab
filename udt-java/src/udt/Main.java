package udt;

import java.io.IOException;

import util.CommandLine;

public final class Main {

   static private void usage() {
      System.err.printf(
         "\nusage: %s --iface=<network interface>" +
         " --udt-port=<port>" +
         " --sc-address=<IP address or hostname>" +
         " --sc-port=<port>" +
         " --ui-address=<IP address or hostname>" +
         " --ui-port=<port>\n\n",
         Main.class.getName());
      System.exit( 1 );
   }

   /**
    * Point d'entrée du programme, usage typique :
    *      UniteDeTraitement --iface=enp3s0 --udt-port=2417 --sc-address=localhost --sc-port=2416 --ui-address=localhost --ui-port=2418
    * @throws IOException
    */
   static void main( String[] args ) throws IOException {
      final CommandLine arguments = new CommandLine();
      if( ! arguments.parse( args )) {
         usage();
      }
      final String    intrfc    = arguments.getString( "iface"      );
      final int       udtPort   = arguments.getInt   ( "udt-port"   );
      final String    scAddress = arguments.getString( "sc-address" );
      final int       scPort    = arguments.getInt   ( "sc-port"    );
      final String    uiAddress = arguments.getString( "ui-address" );
      final int       uiPort    = arguments.getInt   ( "ui-port"    );
      UniteDeTraitement udt = null;
      try {
         udt = new UniteDeTraitement( intrfc, udtPort, scAddress, scPort, uiAddress, uiPort );
         udt.run();
      }
      catch( final Throwable t ) {
         t.printStackTrace();
         if( udt != null ) {
            udt.shutdown();
         }
         System.exit( 2 );
      }
      System.exit( 0 );
   }
}
