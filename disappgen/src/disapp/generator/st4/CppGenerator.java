package disapp.generator.st4;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.stringtemplate.v4.ST;

import disapp.generator.model.ComponentType;
import disapp.generator.model.CppType;
import disapp.generator.model.InterfaceUsageType;

public class CppGenerator extends BaseGenerator {

   public CppGenerator( Model model ) {
      super( model, "cpp.stg" );
      _group.registerRenderer( String.class, new BaseRenderer());
   }

   private void generateEnumHeader( String enumName ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "namespace", _package );
      _model.fillEnumTemplate( enumName, tmpl );
      write( "", enumName + ".hpp", tmpl );
   }

   private void generateEnumBody( String enumName ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "namespace", _package );
      _model.fillEnumTemplate( enumName, tmpl );
      write( "", enumName + ".cpp", tmpl );
   }

   @Override
   protected void generateEnum( String enumName ) throws IOException {
      generateEnumHeader( enumName );
      generateEnumBody  ( enumName );
   }

   private void generateStructHeader( String structName ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "namespace", _package );
      _model.fillStructHeaderTemplate( structName, tmpl );
      write( "", structName + ".hpp", tmpl );
   }

   private void generateStructBody( String structName ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/structBody" );
      tmpl.add( "namespace", _package );
      _model.fillStructBodyTemplate( structName, tmpl );
      write( "", structName + ".cpp", tmpl );
   }

   @Override
   protected void generateStruct( String xUser ) throws IOException {
      generateStructHeader( xUser );
      generateStructBody  ( xUser );
   }

   private void generateRequiredHeaders( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final ST tmpl = _group.getInstanceOf( "/requiredHeader" );
         tmpl.add( "namespace", _package );
         _model.fillRequiredHeaderTemplate( required.getInterface(), required, tmpl );
         write( "", 'I' + required.getInterface() + ".hpp", tmpl );
      }
   }

   private void generateRequiredBodies( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final ST tmpl = _group.getInstanceOf( "/requiredBody" );
         tmpl.add( "namespace", _package );
         _model.fillRequiredBodyTemplate( required.getInterface(), required, tmpl );
         write( "net", required.getInterface() + ".cpp", tmpl );
      }
   }

   private void generateOfferedHeader( ComponentType component ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/offeredHeader" );
      tmpl.add( "namespace", _package );
      _model.fillOfferedHeader( component.getName(), component.getOffers(), tmpl );
      write( "", 'I' + component.getName() + ".hpp", tmpl );
   }

   private void generateDispatcherHeader( ComponentType component, int rawSize ) throws IOException {
      final ST  tmpl = _group.getInstanceOf( "/dispatcherHeader" );
      tmpl.add( "namespace", _package );
      _model.fillDispatcherHeader( component.getName(), rawSize, tmpl );
      write( "", 'I' + component.getName() + "Dispatcher.hpp", tmpl );
   }

   private void generateDispatcherBody( ComponentType component, int rawSize ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/dispatcherBody" );
      tmpl.add( "namespace", _package );
      _model.fillDispatcherBody( component.getName(), component.getOffers(), rawSize, tmpl );
      write( "net", component.getName() + "Dispatcher.cpp", tmpl );
   }

   private void generateComponent( ComponentType component ) throws IOException {
      final int offersRawSize = _model.getBufferCapacity( component.getOffers());
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy( component.getOffers()  , generated );
      generateTypesUsedBy( component.getRequires(), generated );
      generateRequiredHeaders ( component );
      generateRequiredBodies  ( component );
      generateOfferedHeader   ( component );
      generateDispatcherHeader( component, offersRawSize );
      generateDispatcherBody  ( component, offersRawSize );
   }

   public void generateComponents() throws IOException {
      for( final ComponentType component : _model.getApplication().getComponent()) {
         final CppType implType = component.getCpp();
         if( implType != null ) {
            _genDir  = implType.getSrcDir();
            _package = implType.getNamespace();
            generateComponent( component );
         }
      }
   }
}
