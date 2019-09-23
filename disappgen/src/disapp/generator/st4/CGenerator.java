package disapp.generator.st4;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import disapp.generator.model.CType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.FieldType;
import disapp.generator.model.InterfaceUsageType;

public class CGenerator extends BaseGenerator {

   private final STGroup   _cGroup   = new STGroupFile( getClass().getResource( "/resources/c.stg" ), "utf-8", '<', '>' );
   private final CRenderer _renderer = new CRenderer();

   public CGenerator( Model model ) {
      super( model );
      _cGroup.registerRenderer( String.class, _renderer );
      _cGroup.registerModelAdaptor( FieldType.class, new FieldAdaptor());
   }

   @Override
   protected void generateEnum( String enumName ) throws IOException {
      final ST tmpl = _cGroup.getInstanceOf( "/enumHeader" );
      _model.fillEnumTemplate( _package, enumName, tmpl );
      write( "", CRenderer.cname( enumName ) + ".h", tmpl );
   }

   private void generateStructHeader( String structName ) throws IOException {
      final ST tmpl = _cGroup.getInstanceOf( "/structHeader" );
      _model.fillStructHeaderTemplate( _package, structName, tmpl );
      write( "", CRenderer.cname( structName ) + ".h", tmpl );
   }

   private void generateStructBody( String structName ) throws IOException {
      final ST tmpl = _cGroup.getInstanceOf( "/structBody" );
      _model.fillStructBodyTemplate( _package, structName, tmpl );
      write( "", CRenderer.cname( structName ) + ".c", tmpl );
   }

   @Override
   protected void generateStruct( String xUser ) throws IOException {
      generateStructHeader( xUser );
      generateStructBody  ( xUser );
   }

   private void generateRequiredHeaders( ComponentType component, int rawSize ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final String func = CRenderer.cname( required.getInterface()).toString();
         final ST     tmpl = _cGroup.getInstanceOf( "/requiredHeader" );
         _model.fillRequiredHeaderTemplate( _package, func, rawSize, required, tmpl );
         write( "", func + ".h", tmpl );
      }
   }

   private void generateRequiredBodies( ComponentType component, int rawSize ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final ST     tmpl = _cGroup.getInstanceOf( "/requiredBody" );
         final String func = CRenderer.cname( required.getInterface()).toString();
         _model.fillRequiredBodyTemplate( _package, func, rawSize, required, tmpl );
         write( "net", func + ".c", tmpl );
      }
   }

   private void generateOfferedHeader( ComponentType component ) throws IOException {
      final ST tmpl = _cGroup.getInstanceOf( "/offeredHeader" );
      _model.fillOfferedHeader( _package, component.getName(), component.getOffers(), tmpl );
      write( "", CRenderer.cname( component.getName()) + ".h", tmpl );
   }

   private void generateDispatcherHeader( ComponentType component, int rawSize ) throws IOException {
      final ST  tmpl = _cGroup.getInstanceOf( "/dispatcherHeader" );
      _model.fillDispatcherHeader( _package, component.getName(), rawSize, tmpl );
      write( "", CRenderer.cname( component.getName()) + "_dispatcher.h", tmpl );
   }

   private void generateDispatcherBody( ComponentType component, int rawSize ) throws IOException {
      final ST tmpl = _cGroup.getInstanceOf( "/dispatcherBody" );
      _model.fillDispatcherBody( _package, component.getName(), component.getOffers(), rawSize, tmpl );
      write( "net", CRenderer.cname( component.getName()) + "_dispatcher.c", tmpl );
   }

   private void generateComponent( ComponentType component ) throws IOException {
      final int requiresRawSize = _model.getBufferCapacity( component.getRequires());
      final int offersRawSize   = _model.getBufferCapacity( component.getOffers());
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy( component.getOffers()  , generated );
      generateTypesUsedBy( component.getRequires(), generated );
      generateRequiredHeaders ( component, requiresRawSize );
      generateRequiredBodies  ( component, requiresRawSize );
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
