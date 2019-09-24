package disapp.generator;

import java.io.File;
import java.io.IOException;

import disapp.generator.st4.CGenerator;
import disapp.generator.st4.CppGenerator;
import disapp.generator.st4.JavaGenerator;
import disapp.generator.st4.Model;

public class Main {

   private final Model _model;

   public Main( File model, boolean force ) throws Exception {
      _model = new Model( model, force );
   }

   private void generateComponents() throws IOException {
      new JavaGenerator( _model ).generateComponents();
      new CGenerator   ( _model ).generateComponents();
      new CppGenerator ( _model ).generateComponents();
   }

   private static void usage() {
      System.err.println( "usage: disapp.generator.Main --model=<xml system file> [--force=<true|false>]" );
      System.exit(1);
   }

   public static void main( String[] args ) throws Throwable {
      File    modelPath = null;
      boolean force     = false;
      for( final String arg : args ) {
         if( arg.startsWith( "--" )) {
            final int sep = arg.indexOf( '=' );
            if( sep > -1 ) {
               final String name  = arg.substring( 2, sep );
               final String value = arg.substring( sep + 1 );
               if( value.isBlank()) {
                  System.err.printf( "Argument expected after '%s'\n", name );
                  usage();
               }
               switch( name ) {
               case "model": modelPath = new File( value ); break;
               case "force": force = Boolean.parseBoolean( value ); break;
               default: usage();
               }
            }
            else {
               usage();
            }
         }
         else {
            usage();
         }
      }
      if( modelPath == null ) {
         usage();
      }
      else {
         new Main( modelPath, force ).generateComponents();
         System.exit(0);
      }
   }
}
