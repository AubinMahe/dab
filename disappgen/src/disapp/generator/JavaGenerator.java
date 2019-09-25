package disapp.generator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.ComponentType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.InterfaceUsageType;
import disapp.generator.model.JavaType;
import disapp.generator.model.StructType;

public class JavaGenerator extends BaseGenerator {

   public JavaGenerator( Model model ) {
      super( model, "java.stg", new BaseRenderer() );
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enum" );
      tmpl.add( "package", _package );
      tmpl.add( "enum"   , enm      );
      write( "", name + ".java", tmpl );
   }

   @Override
   protected void generateStruct( String name ) throws IOException {
      final StructType      struct = _model.getStruct( name );
      final List<FieldType> fields = struct.getField();
      final ST              tmpl   = _group.getInstanceOf( "/struct" );
      tmpl.add( "package", _package );
      tmpl.add( "struct" , struct   );
      setRendererFieldsMaxWidth( fields );
      write( "", name + ".java", tmpl );
   }

   private void generateRequiredIntrf( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final String            ifaceName  = required.getInterface();
         final InterfaceType     iface      = _model.getInterface( ifaceName );
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferCapacity( iface );
         final ST                tmpl       = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "package"  , _package   );
         tmpl.add( "usedTypes", usedTypes  );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize    );
         tmpl.add( "iface"    , iface      );
         write( "", 'I' + required.getInterface() + ".java", tmpl );
      }
   }

   private void generateRequiredImpl( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final String            ifaceName  = required.getInterface();
         final InterfaceType     iface      = _model.getInterface( ifaceName );
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferCapacity( iface );
         final int               ifaceID    = _model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "package"  , _package  );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize   );
         tmpl.add( "iface"    , iface     );
         tmpl.add( "ifaceID"  , ifaceID   );
         setRendererFieldsMaxWidth( iface );
         write( "net", ifaceName + ".java", tmpl );
      }
   }

   private void generateOfferedIntrf( ComponentType component ) throws IOException {
      final String                   compName   = component.getName();
      final List<InterfaceUsageType> allOffered = component.getOffers();
      final SortedSet<String>        usedTypes  = _model.getUsedTypesBy( allOffered );
      final List<EventType>          events     = _model.getEventsOf( allOffered );
      final ST                       tmpl       = _group.getInstanceOf( "/offeredInterface" );
      tmpl.add( "package"  , _package  );
      tmpl.add( "name"     , compName  );
      tmpl.add( "usedTypes", usedTypes );
      tmpl.add( "events"   , events    );
      write( "", 'I' + component.getName() + ".java", tmpl );
   }

   private void generateDispatcherImpl( ComponentType component, int rawSize ) throws IOException {
      final String                       compName     = component.getName();
      final List<InterfaceUsageType>     ifaces       = component.getOffers();
      final Map<String, Integer>         interfaceIDs = _model.getInterfaceIDs( ifaces );
      final Map<String, List<EventType>> events       = _model.getEventsMapOf ( ifaces );
      final SortedSet<String>            usedTypes    = _model.getUsedTypesBy ( ifaces );
      final ST                           tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "package"  , _package     );
      tmpl.add( "compName" , compName     );
      tmpl.add( "ifaces"   , interfaceIDs );
      tmpl.add( "events"   , events       );
      tmpl.add( "usedTypes", usedTypes    );
      tmpl.add( "rawSize"  , rawSize      );
      setRendererInterfaceMaxWidth( "width", ifaces );
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
