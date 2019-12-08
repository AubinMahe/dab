package disapp.generator;

import java.io.File;
import java.io.IOException;

import disapp.generator.model.ComponentImplType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.DeploymentType;
import disapp.generator.model.ProcessType;

public class Main {

   private final Model _model;

   public Main( File model, boolean force ) throws Exception {
      _model = new Model( model, force );
   }

   private void generateComponents( String deployment ) throws IOException {
      final DeploymentType dep = _model.getDeployment( deployment );
      if( dep == null ) {
         throw new IllegalStateException( "'" + deployment + "' is not a valid deployment name" );
      }
      final JavaGenerator java = new JavaGenerator( _model );
//      final CGenerator    c    = new CGenerator   ( _model, deployment );
//      final CppGenerator  cpp  = new CppGenerator ( _model, deployment );
      for( final ComponentType component : _model.getApplication().getComponent()) {
         for( final ComponentImplType implementation : component.getImplementation()) {
            switch( implementation.getLanguage()) {
            case "Java": java.component( component, implementation ); break;
//            case "C"   : c   .generateComponent( component, implementation ); break;
//            case "C++" : cpp .generateComponent( component, implementation ); break;
            }
         }
      }
      for( final ProcessType process : dep.getProcess()) {
         java.factory( deployment, process );
      }
//      c  .generateTypesMakefileSourcesList();
//      cpp.generateTypesMakefileSourcesList();
   }

   private static void usage() {
      System.err.println( "usage: disapp.generator.Main --model=<xml system file> --deployment=<deployment name> [--force=<true|false>]" );
      System.exit(1);
   }

   public static void main( String[] args ) throws Throwable {
      File    modelPath  = null;
      String  deployment = null;
      boolean force      = false;
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
               case "model"     : modelPath  = new File( value );             break;
               case "deployment": deployment = value;                         break;
               case "force"     : force      = Boolean.parseBoolean( value ); break;
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
      if( modelPath == null || deployment == null ) {
         usage();
      }
      else {
         new Main( modelPath, force ).generateComponents( deployment );
         System.exit(0);
      }
   }
}
