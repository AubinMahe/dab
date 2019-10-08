package disapp.generator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.AutomatonType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.FieldType;
import disapp.generator.model.ImplementationType;
import disapp.generator.model.InstanceType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.RequiresType;
import disapp.generator.model.StructType;

public class JavaGenerator extends BaseGenerator {

   public JavaGenerator( Model model ) {
      super( model, "java.stg", new BaseRenderer());
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enum" );
      tmpl.add( "package", _moduleName );
      tmpl.add( "enum"   , enm );
      write( name + ".java", tmpl );
   }

   @Override
   protected void generateStruct( String name ) throws IOException {
      final StructType      struct = _model.getStruct( name );
      final List<FieldType> fields = struct.getField();
      final ST              tmpl   = _group.getInstanceOf( "/struct" );
      tmpl.add( "package", _moduleName );
      tmpl.add( "struct" , struct );
      setRendererFieldsMaxWidth( fields );
      write( name + ".java", tmpl );
   }

   private void generateRequiredInterfaces( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface      = (InterfaceType)required.getInterface();
         final String            ifaceName  = iface.getName();
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferOutCapacity( required );
         _model.getInterface( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "package"  , _moduleName   );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "rawSize"  , rawSize );
         tmpl.add( "iface"    , iface );
         write( 'I' + ifaceName + ".java", tmpl );
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
         tmpl.add( "package"  , _moduleName  );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize   );
         tmpl.add( "iface"    , iface     );
         tmpl.add( "ifaceID"  , ifaceID   );
         setRendererFieldsMaxWidth( iface );
         write( ifaceName + ".java", tmpl );
      }
   }

   private void generateOfferedInterfaces( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType     iface     = (InterfaceType)offered.getInterface();
         final String            ifaceName = iface.getName();
         final SortedSet<String> usedTypes = _model.getUsedTypesBy( ifaceName );
         final List<Object>      facets    = _model.getFacets().get( ifaceName );
         final ST                tmpl      = _group.getInstanceOf( "/offeredInterface" );
         tmpl.add( "package"  , _moduleName );
         tmpl.add( "name"     , ifaceName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "facets"   , facets );
         write( 'I' + ifaceName + ".java", tmpl );
      }
   }

   private void generateDispatcherImplementation( ComponentType component ) throws IOException {
      final String                          compName    = component.getName();
      final List<OfferedInterfaceUsageType> offers      = component.getOffers();
      final Map<String, Integer>            ifaces      = _model.getInterfaceIDs( offers );
      final Map<String, Map<String, Byte>>  eventIDs    = _model.getEventIDs();
      final Map<String, List<Object>>       events      = _model.getOfferedEventsOrRequests( component );
      final SortedSet<String>               usedTypes   = _model.getUsedTypesBy( offers );
      final Map<String, List<RequestType>>  requests    = Model.getRequestMap( events );
      final int                             rawSize     = _model.getBufferInCapacity( component );
      final int                             respRawSize = _model.getBufferResponseCapacity( events );
      final ST                              tmpl        = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "package"    , _moduleName );
      tmpl.add( "compName"   , compName );
      tmpl.add( "ifaces"     , ifaces );
      tmpl.add( "events"     , events );
      tmpl.add( "eventIDs"   , eventIDs );
      tmpl.add( "usedTypes"  , usedTypes );
      tmpl.add( "rawSize"    , rawSize );
      tmpl.add( "respRawSize", respRawSize );
      tmpl.add( "requests"   , requests );
      setRendererInterfaceMaxWidth( "width", offers );
      write( component.getName() + "Dispatcher.java", tmpl );
   }

   private void generateComponentImplementation( ComponentType component ) throws IOException {
      final List<InstanceType>              instances       = _model.getInstancesOf( component );
      final Map<String, List<RequiresType>> requires        = _model.getRequiredInstancesOf( component );
      final Map<String, InstanceType>       instancesByName = _model.getInstancesByName();
      final Map<String, List<FieldType>>    data            = Model.getOfferedDataOf( component );
      final Set<String>                     actions         = _model.getAutomatonActions( component );
      final ST                              tmpl            = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "package"        , _moduleName );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , requires );
      tmpl.add( "instancesByName", instancesByName );
      tmpl.add( "instances"      , instances );
      tmpl.add( "actions"        , actions );
      tmpl.add( "data"           , data );
      write( component.getName() + "Component.java", tmpl );
   }

   private void generateAutomaton( ComponentType component ) throws IOException {
      final AutomatonType automaton = component.getAutomaton();
      if( automaton != null ) {
         final ST tmpl = _group.getInstanceOf( "/automaton" );
         tmpl.add( "package"  , _moduleName );
         tmpl.add( "component", component );
         write( "Automaton.java", tmpl );
      }
   }

   void generateComponent( ComponentType component, ImplementationType implementation ) throws IOException {
      _genDir     = implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      generateTypesUsedBy             ( component );
      generateRequiredInterfaces      ( component );
      generateRequiredImplementations ( component );
      generateOfferedInterfaces       ( component );
      generateDispatcherImplementation( component );
      generateComponentImplementation ( component );
      generateAutomaton               ( component );
   }
}
