package disapp.generator;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Main {

   private final Model _model;

   public Main( File model, boolean force ) throws Exception {
      _model = new Model( model, force );
   }

   private void generateComponents() throws IOException {
      JavaGenerator javaGen = null;
      CppGenerator  cppGen  = null;
      CGenerator    cGen    = null;
      final NodeList xComponents = _model.getComponents();
      for( int i = 0, iCount = xComponents.getLength(); i < iCount; ++i ) {
         final Element  xComponent = (Element)xComponents.item( i );
         final String   name       = xComponent.getAttribute( "name" );
         final NodeList xOffers    = xComponent.getElementsByTagName( "offers" );
         final NodeList xRequires  = xComponent.getElementsByTagName( "requires" );
         final NodeList xJavas     = xComponent.getElementsByTagName( "java" );
         final NodeList xCpps      = xComponent.getElementsByTagName( "cpp" );
         final NodeList xCs        = xComponent.getElementsByTagName( "c" );
         if( xJavas.getLength() == 1 ) {
            final Element xJava    = (Element)xJavas.item( 0 );
            final String  xSrcDir  = xJava.getAttribute( "src-dir" );
            final String  xPackage = xJava.getAttribute( "package" );
            if( javaGen == null ) {
               javaGen = new JavaGenerator( _model );
            }
            javaGen.generateComponent( name, xOffers, xRequires, xSrcDir, xPackage );
         }
         else if( xCpps.getLength() == 1 ) {
            final Element xCpp       = (Element)xCpps.item( 0 );
            final String  xSrcDir    = xCpp.getAttribute( "src-dir" );
            final String  xNamespace = xCpp.getAttribute( "namespace" );
            if( cppGen == null ) {
               cppGen = new CppGenerator( _model );
            }
            cppGen.generateComponent( name, xOffers, xRequires, xSrcDir, xNamespace );
         }
         else if( xCs.getLength() == 1 ) {
            final Element xC         = (Element)xCs.item( 0 );
            final String  xSrcDir    = xC.getAttribute( "src-dir" );
            final String  xNamespace = xC.getAttribute( "prefix" );
            if( cGen == null ) {
               cGen = new CGenerator( _model );
            }
            cGen.generateComponent( name, xOffers, xRequires, xSrcDir, xNamespace );
         }
         else {
            throw new IllegalStateException(
               "XML non valide : l'implémentation n'est ni java, ni c++, ni C !" );
         }
      }
   }

   private static void usage() {
      System.err.println( "usage: disapp.generator.Main <xml system file> [--force=true]" );
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
               switch( name ) {
               case "model": modelPath = new File( value ); break;
               case "force": force = Boolean.parseBoolean( value ); break;
               default: usage();
               }
            }
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
