package disapp.generator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.ComponentType;
import disapp.generator.model.CppType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.InterfaceUsageType;
import disapp.generator.model.StructType;

public class CppGenerator extends BaseGenerator {

   public CppGenerator( Model model ) {
      super( model, "cpp.stg", new BaseRenderer() );
   }

   private void generateEnumHeader( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "namespace", _package );
      tmpl.add( "enum"     , enm      );
      write( "", name + ".hpp", tmpl );
   }

   private void generateEnumBody( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "namespace", _package );
      tmpl.add( "enum"     , enm      );
      write( "", name + ".cpp", tmpl );
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      generateEnumHeader( name );
      generateEnumBody  ( name );
   }

   private void generateStructHeader( String name ) throws IOException {
      final StructType struct = _model.getStruct( name );
      final ST         tmpl   = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "namespace", _package );
      tmpl.add( "struct"   , struct   );
      write( "", name + ".hpp", tmpl );
   }

   private void generateStructBody( String name ) throws IOException {
      final StructType      struct = _model.getStruct( name );
      final List<FieldType> fields = struct.getField();
      final ST              tmpl   = _group.getInstanceOf( "/structBody" );
      tmpl.add( "namespace", _package );
      tmpl.add( "struct"   , struct   );
      setRendererFieldsMaxWidth( fields );
      write( "", name + ".cpp", tmpl );
   }

   @Override
   protected void generateStruct( String name ) throws IOException {
      generateStructHeader( name );
      generateStructBody  ( name );
   }

   private void generateRequiredInterface( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final String            ifaceName  = required.getInterface();
         final InterfaceType     iface      = _model.getInterface( ifaceName );
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferCapacity( iface );
         final ST                tmpl       = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "namespace", _package  );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize   );
         tmpl.add( "iface"    , iface     );
         write( "", 'I' + required.getInterface() + ".hpp", tmpl );
      }
   }

   private void generateRequiredImplementations( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : component.getRequires()) {
         final String            ifaceName  = required.getInterface();
         final InterfaceType     iface      = _model.getInterface( ifaceName );
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferCapacity( iface );
         final int               ifaceID    = _model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "namespace", _package  );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize   );
         tmpl.add( "iface"    , iface     );
         tmpl.add( "ifaceID"  , ifaceID   );
         setRendererFieldsMaxWidth( iface );
         write( "net", ifaceName + ".cpp", tmpl );
      }
   }

   private void generateOfferedInterface( ComponentType component ) throws IOException {
      final String                   compName   = component.getName();
      final List<InterfaceUsageType> allOffered = component.getOffers();
      final SortedSet<String>        usedTypes  = _model.getUsedTypesBy( allOffered );
      final List<EventType>          events     = _model.getEventsOf( allOffered );
      final ST                       tmpl       = _group.getInstanceOf( "/offeredInterface" );
      tmpl.add( "namespace", _package  );
      tmpl.add( "name"     , compName  );
      tmpl.add( "usedTypes", usedTypes );
      tmpl.add( "events"   , events    );
      write( "", 'I' + component.getName() + ".hpp", tmpl );
   }

   private void generateDispatcherInterface( ComponentType component, int rawSize ) throws IOException {
      final String compName = component.getName();
      final ST     tmpl     = _group.getInstanceOf( "/dispatcherInterface" );
      tmpl.add( "namespace", _package );
      tmpl.add( "name"     , compName );
      tmpl.add( "rawSize"  , rawSize );
      write( "", 'I' + component.getName() + "Dispatcher.hpp", tmpl );
   }

   private void generateDispatcherImplementation( ComponentType component, int rawSize ) throws IOException {
      final String                       compName     = component.getName();
      final List<InterfaceUsageType>     ifaces       = component.getOffers();
      final Map<String, Integer>         interfaceIDs = _model.getInterfaceIDs( ifaces );
      final Map<String, List<EventType>> events       = _model.getEventsMapOf ( ifaces );
      final SortedSet<String>            usedTypes    = _model.getUsedTypesBy ( ifaces );
      final ST                           tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "namespace", _package     );
      tmpl.add( "compName" , compName     );
      tmpl.add( "ifaces"   , interfaceIDs );
      tmpl.add( "events"   , events       );
      tmpl.add( "usedTypes", usedTypes    );
      tmpl.add( "rawSize"  , rawSize      );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( "net", component.getName() + "Dispatcher.cpp", tmpl );
   }

   private void generateComponent( ComponentType component ) throws IOException {
      final int offersRawSize = _model.getBufferCapacity( component.getOffers());
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy             ( component.getOffers()  , generated );
      generateTypesUsedBy             ( component.getRequires(), generated );
      generateRequiredInterface       ( component );
      generateRequiredImplementations ( component );
      generateOfferedInterface        ( component );
      generateDispatcherInterface     ( component, offersRawSize );
      generateDispatcherImplementation( component, offersRawSize );
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
