package udt;

import udt.ComponentFactory_udt1;
import udt.ComponentFactory_udt2;
import util.CommandLine;

public final class Main {

   static private void usage() {
      System.err.printf( "\nusage: %s --name=<instance name as defined in XML application file>\n\n", Main.class.getName());
      System.exit( 1 );
   }

   public static void main( String[] args ) {
      final CommandLine arguments = new CommandLine();
      if( ! arguments.parse( args )) {
         usage();
      }
      final String name = arguments.getString( "name" );
      if( name == null ) {
         usage();
         return; // unreachable
      }
      try {
         switch( name ) {
         case "udt1": new ComponentFactory_udt1(); break;
         case "udt2": new ComponentFactory_udt2(); break;
         default: throw new IllegalStateException( name + " isn't a valid process name");
         }
      }
      catch( final Throwable t ) {
         t.printStackTrace();
         System.exit( 2 );
      }
      System.exit( 0 );
   }
}
