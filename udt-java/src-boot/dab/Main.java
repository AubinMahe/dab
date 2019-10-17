package dab;

import java.io.IOException;

import util.CommandLine;

public final class Main {

   static private void usage() {
      System.err.printf( "\nusage: %s --name=<instance name as defined in XML application file>\n\n", Main.class.getName());
      System.exit( 1 );
   }

   public static void main( String[] args ) throws IOException {
      final CommandLine arguments = new CommandLine();
      if( ! arguments.parse( args )) {
         usage();
      }
      final String name = arguments.getString( "name" );
      if( name == null ) {
         usage();
      }
      Controleur udt = null;
      try {
         udt = new Controleur( name );
         System.err.println( "Controleur '" + name + "'" );
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
