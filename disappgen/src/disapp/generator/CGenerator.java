package disapp.generator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.stringtemplate.v4.ST;

import disapp.generator.model.CType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.InterfaceUsageType;

public class CGenerator extends BaseGenerator {

   public CGenerator( Model model ) {
      super( model, "c.stg" );
      _group.registerRenderer( String.class, new CRenderer());
   }

   private void generateEnumHeader( String name ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "prefix", _package );
      _model.fillEnumTemplate( name, tmpl );
      write( "", CRenderer.cname( name ) + ".h", tmpl );
   }

   private void generateEnumBody( String name ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "prefix", _package );
      _model.fillEnumTemplate( name, tmpl );
      write( "", CRenderer.cname( name ) + ".c", tmpl );
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      generateEnumHeader( name );
      generateEnumBody  ( name );
   }

   private void generateStructHeader( String name ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "prefix", _package );
      _model.fillStructHeaderTemplate( name, tmpl );
      write( "", CRenderer.cname( name ) + ".h", tmpl );
   }

   private void generateStructBody( String name ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/structBody" );
      tmpl.add( "prefix", _package );
      _model.fillStructBodyTemplate( name, tmpl );
      write( "", CRenderer.cname( name ) + ".c", tmpl );
   }

   @Override
   protected void generateStruct( String name ) throws IOException {
      generateStructHeader( name );
      generateStructBody  ( name );
   }

   private void generateRequiredHeaders( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final String ifaceName = CRenderer.cname( required.getInterface());
         final ST     tmpl      = _group.getInstanceOf( "/requiredHeader" );
         tmpl.add( "prefix", _package );
         _model.fillRequiredHeaderTemplate( ifaceName, required, tmpl );
         write( "", ifaceName + ".h", tmpl );
      }
   }

   private void generateRequiredBodies( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final ST     tmpl      = _group.getInstanceOf( "/requiredBody" );
         final String ifaceName = CRenderer.cname( required.getInterface());
         tmpl.add( "prefix", _package );
         _model.fillRequiredBodyTemplate( ifaceName, required, tmpl );
         write( "net", ifaceName + ".c", tmpl );
      }
   }

   private void generateOfferedHeader( ComponentType component ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/offeredHeader" );
      tmpl.add( "prefix", _package );
      _model.fillOfferedHeader( component.getName(), component.getOffers(), tmpl );
      write( "", CRenderer.cname( component.getName()) + ".h", tmpl );
   }

   private void generateDispatcherHeader( ComponentType component, int rawSize ) throws IOException {
      final ST  tmpl = _group.getInstanceOf( "/dispatcherHeader" );
      tmpl.add( "prefix", _package );
      _model.fillDispatcherHeader( component.getName(), rawSize, tmpl );
      write( "", CRenderer.cname( component.getName()) + "_dispatcher.h", tmpl );
   }

   private void generateDispatcherBody( ComponentType component, int rawSize ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/dispatcherBody" );
      tmpl.add( "prefix", _package );
      _model.fillDispatcherBody( component.getName(), component.getOffers(), rawSize, tmpl );
      write( "net", CRenderer.cname( component.getName()) + "_dispatcher.c", tmpl );
   }

   private void generateComponent( ComponentType component ) throws IOException {
      final int offersRawSize   = _model.getBufferCapacity( component.getOffers());
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
         final CType implType = component.getC();
         if( implType != null ) {
            _genDir  = implType.getSrcDir();
            _package = implType.getPrefix();
            generateComponent( component );
         }
      }
   }
}
