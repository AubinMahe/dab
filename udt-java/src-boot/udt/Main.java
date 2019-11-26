package udt;

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
         final Thread factory;
         switch( name ) {
         case "isolated.udt1": factory = new isolated.udt1.ComponentFactory(); break;
         case "isolated.udt2": factory = new isolated.udt2.ComponentFactory(); break;
         default: throw new IllegalStateException( name + " isn't a valid deployment.process name");
         }
         factory.join();
      }
      catch( final Throwable t ) {
         t.printStackTrace();
         System.exit( 2 );
      }
      System.out.printf( "%s.main|done.\n", Main.class.getName());
      System.exit( 0 );
   }
}
