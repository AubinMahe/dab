package disapp.generator.st4;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.stringtemplate.v4.ST;

import disapp.generator.model.ComponentType;
import disapp.generator.model.InterfaceUsageType;
import disapp.generator.model.JavaType;

public class JavaGenerator extends BaseGenerator {

   public JavaGenerator( Model model ) {
      super( model, "java.stg" );
      _group.registerRenderer( String.class, new BaseRenderer());
   }

   @Override
   protected void generateEnum( String enumName ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/enum" );
      tmpl.add( "package", _package );
      _model.fillEnumTemplate( enumName, tmpl );
      write( "", enumName + ".java", tmpl );
   }

   @Override
   protected void generateStruct( String structName ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/struct" );
      tmpl.add( "package", _package );
      _model.fillStructBodyTemplate( structName, tmpl );
      write( "", structName + ".java", tmpl );
   }

   private void generateRequiredIntrf( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final ST tmpl = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "package", _package );
         _model.fillRequiredHeaderTemplate( required.getInterface(), required, tmpl );
         write( "", 'I' + required.getInterface() + ".java", tmpl );
      }
   }

   private void generateRequiredImpl( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final ST tmpl = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "package", _package );
         _model.fillRequiredBodyTemplate( required.getInterface(), required, tmpl );
         write( "net", required.getInterface() + ".java", tmpl );
      }
   }

   private void generateOfferedIntrf( ComponentType component ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/offeredInterface" );
      tmpl.add( "package", _package );
      _model.fillOfferedHeader( component.getName(), component.getOffers(), tmpl );
      write( "", 'I' + component.getName() + ".java", tmpl );
   }

   private void generateDispatcherImpl( ComponentType component, int rawSize ) throws IOException {
      final ST  tmpl = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "package", _package );
      _model.fillDispatcherBody( component.getName(), component.getOffers(), rawSize, tmpl );
      write( "net", component.getName() + "Dispatcher.java", tmpl );
   }

   private void generateComponent( ComponentType component ) throws IOException {
      final int offersRawSize = _model.getBufferCapacity( component.getOffers());
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy   ( component.getOffers()  , generated );
      generateTypesUsedBy   ( component.getRequires(), generated );
      generateRequiredIntrf ( component );
      generateRequiredImpl  ( component );
      generateOfferedIntrf  ( component );
      generateDispatcherImpl( component, offersRawSize );
   }

   public void generateComponents() throws IOException {
      for( final ComponentType component : _model.getApplication().getComponent()) {
         final JavaType implType = component.getJava();
         if( implType != null ) {
            _genDir  = implType.getSrcDir();
            _package = implType.getPackage();
            generateComponent( component );
         }
      }
   }
}
