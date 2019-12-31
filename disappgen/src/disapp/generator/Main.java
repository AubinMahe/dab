package disapp.generator;

import java.io.File;
import java.io.IOException;

import disapp.generator.genmodel.CompImplType;
import disapp.generator.genmodel.FactoryType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.DeploymentType;
import disapp.generator.model.ProcessType;

public class Main {

   private final Model _model;

   public Main( File model, File genModel, boolean force ) throws Exception {
      _model = new Model( model, genModel, force );
   }

   private void generateComponents( String deployment ) throws IOException {
      final DeploymentType dep = _model.getDeployment( deployment );
      if( dep == null ) {
         throw new IllegalStateException( "'" + deployment + "' is not a valid deployment name" );
      }
      final JavaGenerator java = new JavaGenerator( _model );
      final CGenerator    c    = new CGenerator   ( _model );
      final CppGenerator  cpp  = new CppGenerator ( _model );
      for( final ComponentType component : _model.getApplication().getComponent()) {
         for( final CompImplType implementation : _model.getCompImpls( component.getName())) {
            switch( implementation.getLanguage()) {
            case "Java": java.component( component, implementation ); break;
            case "C"   : c   .component( component, implementation ); break;
            case "C++" : cpp .component( component, implementation ); break;
            }
         }
      }
      final disapp.generator.genmodel.DeploymentType depImpl = _model.getDeploymentImpl( deployment );
      for( final Object o : depImpl.getProcessOrProcessRef()) {
         if( o instanceof disapp.generator.genmodel.ProcessType ) {
            final disapp.generator.genmodel.ProcessType processImpl = (disapp.generator.genmodel.ProcessType)o;
            final ProcessType process = Model.getProcess( dep, processImpl.getName());
            for( final FactoryType factory : processImpl.getFactory()) {
               switch( factory.getLanguage()) {
               case "Java": java.factory( dep, depImpl, process, processImpl, factory ); break;
               case "C"   : c   .factory( dep, depImpl, process, processImpl, factory ); break;
               case "C++" : cpp .factory( dep, depImpl, process, processImpl, factory ); break;
               }
            }
         }
      }
      c  .typesMakefileSourcesList();
      cpp.typesMakefileSourcesList();
   }

   private static void usage() {
      System.err.println( "usage: disapp.generator.Main"
         + " --model=<xml system file>"
         + " --generation=<xml generation directives>"
         + " --deployment=<deployment name>"
         + " [--force=<true|false>]" );
      System.exit(1);
   }

   public static void main( String[] args ) throws Throwable {
      File    modelPath    = null;
      File    genModelPath = null;
      String  deployment   = null;
      boolean force        = false;
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
               case "model"     : modelPath    = new File( value );             break;
               case "generation": genModelPath = new File( value );             break;
               case "deployment": deployment   = value;                         break;
               case "force"     : force        = Boolean.parseBoolean( value ); break;
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
      if( modelPath == null || genModelPath == null || deployment == null ) {
         usage();
      }
      else {
         new Main( modelPath, genModelPath, force ).generateComponents( deployment );
         System.exit(0);
      }
   }
}
