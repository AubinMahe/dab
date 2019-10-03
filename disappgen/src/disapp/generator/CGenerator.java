package disapp.generator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

public class CGenerator extends BaseGenerator {

   public CGenerator( Model model ) {
      super( model, "c.stg", new CRenderer());
   }

   private void generateEnumHeader( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "prefix", _moduleName );
      tmpl.add( "enum"  , enm      );
      write( CRenderer.cname( name ) + ".h", tmpl );
   }

   private void generateEnumBody( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "prefix", _moduleName );
      tmpl.add( "enum"  , enm      );
      write( CRenderer.cname( name ) + ".c", tmpl );
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      generateEnumHeader( name );
      generateEnumBody  ( name );
   }

   private void generateStructHeader( String name ) throws IOException {
      final StructType struct = _model.getStruct( name );
      final ST         tmpl   = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "prefix", _moduleName );
      tmpl.add( "struct", struct   );
      write( CRenderer.cname( name ) + ".h", tmpl );
   }

   private void generateStructBody( String name ) throws IOException {
      final StructType      struct = _model.getStruct( name );
      final List<FieldType> fields = struct.getField();
      final ST              tmpl   = _group.getInstanceOf( "/structBody" );
      tmpl.add( "prefix", _moduleName );
      tmpl.add( "struct", struct   );
      setRendererFieldsMaxWidth( fields );
      write( CRenderer.cname( name ) + ".c", tmpl );
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
         final int               rawSize    = _model.getBufferInCapacity( component );
         final ST                tmpl       = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "prefix"   , _moduleName   );
         tmpl.add( "usedTypes", usedTypes  );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize    );
         tmpl.add( "iface"    , iface      );
         write( CRenderer.cname( ifaceName ) + ".h", tmpl );
      }
   }

   private void generateRequiredImplementations( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface      = (InterfaceType)required.getInterface();
         final String            ifaceName  = iface.getName();
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferInCapacity( component );
         final int               ifaceID    = _model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "prefix"   , _moduleName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize );
         tmpl.add( "iface"    , iface );
         tmpl.add( "ifaceID"  , ifaceID );
         setRendererFieldsMaxWidth( iface );
         write( CRenderer.cname( ifaceName ) + ".c", tmpl );
      }
   }

   private void generateDispatcherInterface( ComponentType component ) throws IOException {
      final String compName = component.getName();
      final int    rawSize  = _model.getBufferInCapacity( component );
      final ST     tmpl     = _group.getInstanceOf( "/dispatcherInterface" );
      tmpl.add( "prefix", _moduleName );
      tmpl.add( "name"   , compName );
      tmpl.add( "rawSize", rawSize );
      write( CRenderer.cname( component.getName()) + "_dispatcher.h", tmpl );
   }

   private void generateDispatcherImplementation( ComponentType component ) throws IOException {
      final String                          compName     = component.getName();
      final List<OfferedInterfaceUsageType> ifaces       = component.getOffers();
      final Map<String, Integer>            interfaceIDs = _model.getInterfaceIDs( ifaces );
      final Map<String, List<Object>>       events       = _model.getOfferedEventsOrRequests( component );
      final SortedSet<String>               usedTypes    = _model.getUsedTypesBy( ifaces );
      final int                             rawSize      = _model.getBufferInCapacity( component );
      final ST                              tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "prefix"     , _moduleName );
      tmpl.add( "compName"   , compName );
      tmpl.add( "ifaces"     , interfaceIDs );
      tmpl.add( "events"     , events );
      tmpl.add( "usedTypes"  , usedTypes );
      tmpl.add( "rawSize"    , rawSize );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( CRenderer.cname( compName ) + "_dispatcher.c", tmpl );
   }

   private void generateComponentInterface( ComponentType component ) throws IOException {
      final SortedSet<String> usedTypes        = new TreeSet<>();
      final List<Object>      eventsOrRequests = new LinkedList<>();
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType     iface     = (InterfaceType)offered.getInterface();
         final String            ifaceName = iface.getName();
         final SortedSet<String> ut        = _model.getUsedTypesBy( ifaceName );
         final List<Object>      eor       = _model.getEventsOrRequests().get( ifaceName );
         if( ut != null ) {
            usedTypes.addAll( ut );
         }
         if( eor != null ) {
            eventsOrRequests.addAll( eor );
         }
      }
      final String                          compName        = component.getName();
      final List<InstanceType>              instances       = _model.getInstancesOf( component );
      final Map<String, List<RequiresType>> requires        = _model.getRequiredInstancesOf( component );
      final Map<String, InstanceType>       instancesByName = _model.getInstancesByName();
      final Set<String>                     actions         = _model.getAutomatonActions( component );
      final ST tmpl = _group.getInstanceOf( "/componentInterface" );
      tmpl.add( "prefix"          , _moduleName );
      tmpl.add( "component"       , component );
      tmpl.add( "requires"        , requires );
      tmpl.add( "instancesByName" , instancesByName );
      tmpl.add( "instances"       , instances );
      tmpl.add( "usedTypes"       , usedTypes );
      tmpl.add( "eventsOrRequests", eventsOrRequests );
      tmpl.add( "actions"         , actions );
      write( CRenderer.cname( compName ) + ".h", tmpl );
   }

   private void generateComponentImplementation( ComponentType component ) throws IOException {
      final String                          compName        = component.getName();
      final List<InstanceType>              instances       = _model.getInstancesOf( component );
      final Map<String, List<RequiresType>> requires        = _model.getRequiredInstancesOf( component );
      final Map<String, InstanceType>       instancesByName = _model.getInstancesByName();
      final ST tmpl = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "prefix"         , _moduleName );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , requires );
      tmpl.add( "instancesByName", instancesByName );
      tmpl.add( "instances"      , instances );
      write( CRenderer.cname( compName ) + ".c", tmpl );
   }

   private void generateAutomaton( ComponentType component ) throws IOException {
      if( component.getAutomaton() != null ) {
         final ST header = _group.getInstanceOf( "/automatonHeader" );
         header.add( "prefix"   , _moduleName );
         header.add( "component", component );
         write( "automaton.h", header );
         final ST body = _group.getInstanceOf( "/automatonBody" );
         body.add( "prefix"   , _moduleName );
         body.add( "component", component );
         write( "automaton.c", body );
      }
   }

   void generateComponent( ComponentType component, ImplementationType implementation ) throws IOException {
      _genDir     = implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      generateTypesUsedBy             ( component );
      generateTypesUsedBy             ( component );
      generateTypesUsedBy             ( component );
      generateRequiredInterfaces      ( component );
      generateRequiredImplementations ( component );
      generateDispatcherInterface     ( component );
      generateDispatcherImplementation( component );
      generateComponentInterface      ( component );
      generateComponentImplementation ( component );
      generateAutomaton               ( component );
      generateMakefileSourcesList( ".h", ".c" );
   }
}
