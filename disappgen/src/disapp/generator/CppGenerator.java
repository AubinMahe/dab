package disapp.generator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.ComponentType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.FieldType;
import disapp.generator.model.ImplementationType;
import disapp.generator.model.InstanceType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.RequiresType;
import disapp.generator.model.StructType;

public class CppGenerator extends BaseGenerator {

   public CppGenerator( Model model ) {
      super( model, "cpp.stg", new BaseRenderer() );
   }

   private void generateEnumHeader( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "namespace", _moduleName );
      tmpl.add( "enum"     , enm      );
      write( name + ".hpp", tmpl );
   }

   private void generateEnumBody( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "namespace", _moduleName );
      tmpl.add( "enum"     , enm      );
      write( name + ".cpp", tmpl );
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      generateEnumHeader( name );
      generateEnumBody  ( name );
   }

   private void generateStructHeader( String name ) throws IOException {
      final StructType struct = _model.getStruct( name );
      final ST         tmpl   = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "namespace", _moduleName );
      tmpl.add( "struct"   , struct   );
      write( name + ".hpp", tmpl );
   }

   private void generateStructBody( String name ) throws IOException {
      final StructType      struct = _model.getStruct( name );
      final List<FieldType> fields = struct.getField();
      final ST              tmpl   = _group.getInstanceOf( "/structBody" );
      tmpl.add( "namespace", _moduleName );
      tmpl.add( "struct"   , struct   );
      setRendererFieldsMaxWidth( fields );
      write( name + ".cpp", tmpl );
   }

   @Override
   protected void generateStruct( String name ) throws IOException {
      generateStructHeader( name );
      generateStructBody  ( name );
   }

   private void generateRequiredInterfaces( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface      = (InterfaceType)required.getInterface();
         final String            ifaceName  = iface.getName();
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferOutCapacity( required );
         final ST                tmpl       = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "namespace", _moduleName   );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "rawSize"  , rawSize );
         tmpl.add( "iface"    , iface );
         write( 'I' + ifaceName + ".hpp", tmpl );
      }
   }

   private void generateRequiredImplementations( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface      = (InterfaceType)required.getInterface();
         final String            ifaceName  = iface.getName();
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferOutCapacity( required );
         final int               ifaceID    = _model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "namespace", _moduleName  );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize   );
         tmpl.add( "iface"    , iface     );
         tmpl.add( "ifaceID"  , ifaceID   );
         setRendererFieldsMaxWidth( iface );
         write( ifaceName + ".cpp", tmpl );
      }
   }

   private void generateOfferedInterface( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType     iface            = (InterfaceType)offered.getInterface();
         final String            ifaceName        = iface.getName();
         final SortedSet<String> usedTypes        = _model.getUsedTypesBy( ifaceName );
         final List<Object>      eventsOrRequests = _model.getFacets().get( ifaceName );
         final ST                tmpl             = _group.getInstanceOf( "/offeredInterface" );
         tmpl.add( "namespace"       , _moduleName );
         tmpl.add( "name"            , ifaceName );
         tmpl.add( "usedTypes"       , usedTypes );
         tmpl.add( "eventsOrRequests", eventsOrRequests );
         write( 'I' + ifaceName + ".hpp", tmpl );
      }
   }

   private void generateDispatcherInterface( ComponentType component ) throws IOException {
      final String compName = component.getName();
      final int    rawSize  = _model.getBufferInCapacity( component );
      final ST     tmpl     = _group.getInstanceOf( "/dispatcherInterface" );
      tmpl.add( "namespace", _moduleName );
      tmpl.add( "name"     , compName );
      tmpl.add( "rawSize"  , rawSize );
      write( 'I' + component.getName() + "Dispatcher.hpp", tmpl );
   }

   private void generateDispatcherImplementation( ComponentType component ) throws IOException {
      final String                          compName     = component.getName();
      final List<OfferedInterfaceUsageType> ifaces       = component.getOffers();
      final Map<String, Integer>            interfaceIDs = _model.getInterfaceIDs( ifaces );
      final Map<String, List<Object>>       events       = _model.getOfferedEventsOrRequests( component );
      final SortedSet<String>               usedTypes    = _model.getUsedTypesBy( ifaces );
      final int                             rawSize      = _model.getBufferInCapacity( component );
      final ST                              tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "namespace"  , _moduleName );
      tmpl.add( "compName"   , compName );
      tmpl.add( "ifaces"     , interfaceIDs );
      tmpl.add( "events"     , events );
      tmpl.add( "usedTypes"  , usedTypes );
      tmpl.add( "rawSize"    , rawSize );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( component.getName() + "Dispatcher.cpp", tmpl );
   }

   private void generateComponentInterface( ComponentType component ) throws IOException {
      final List<InstanceType>              instances       = _model.getInstancesOf( component );
      final Map<String, List<RequiresType>> requires        = _model.getRequiredInstancesOf( component );
      final Map<String, InstanceType>       instancesByName = _model.getInstancesByName();
      final Set<String>                     actions         = _model.getAutomatonActions( component );
      final ST tmpl = _group.getInstanceOf( "/componentInterface" );
      tmpl.add( "namespace"      , _moduleName );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , requires );
      tmpl.add( "instancesByName", instancesByName );
      tmpl.add( "instances"      , instances );
      tmpl.add( "actions"        , actions );
      write( component.getName() + "Component.hpp", tmpl );
   }

   private void generateComponentImplementation( ComponentType component ) throws IOException {
      final List<InstanceType>              instances       = _model.getInstancesOf( component );
      final Map<String, List<RequiresType>> requires        = _model.getRequiredInstancesOf( component );
      final Map<String, InstanceType>       instancesByName = _model.getInstancesByName();
      final ST tmpl = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "namespace"      , _moduleName );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , requires );
      tmpl.add( "instancesByName", instancesByName );
      tmpl.add( "instances"      , instances );
      write( component.getName() + "Component.cpp", tmpl );
   }

   private void generateAutomaton( ComponentType component ) throws IOException {
      if( component.getAutomaton() != null ) {
         final ST header = _group.getInstanceOf( "/automatonHeader" );
         header.add( "namespace", _moduleName  );
         header.add( "component", component );
         write( "Automaton.hpp", header );
         final ST body = _group.getInstanceOf( "/automatonBody" );
         body.add( "namespace", _moduleName  );
         body.add( "component", component );
         write( "Automaton.cpp", body );
      }
   }

   void generateComponent( ComponentType component, ImplementationType implementation ) throws IOException {
      _genDir     = implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      generateTypesUsedBy             ( component );
      generateRequiredInterfaces      ( component );
      generateRequiredImplementations ( component );
      generateOfferedInterface        ( component );
      generateDispatcherInterface     ( component );
      generateDispatcherImplementation( component );
      generateComponentInterface      ( component );
      generateComponentImplementation ( component );
      generateAutomaton               ( component );
      generateMakefileSourcesList( ".hpp", ".cpp" );
   }
}
