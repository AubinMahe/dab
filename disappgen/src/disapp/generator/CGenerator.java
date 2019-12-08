package disapp.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.ComponentImplType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.DataType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.InstanceType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.ProcessType;
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.StructType;

public class CGenerator extends BaseGenerator {

   public CGenerator( Model model ) {
      super( model, "c.stg", new CRenderer());
   }

   private void generateEnumHeader( String name ) throws IOException {
      final String moduleName = getModuleName( name, "C" );
      final EnumerationType enm = _model.getEnum( name );
      final ST tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "prefix", moduleName );
      tmpl.add( "enum"  , enm );
      setRendererMaxWidth( enm );
      writeType( moduleName, CRenderer.cname( name ) + ".h", tmpl );
   }

   private void generateEnumBody( String name ) throws IOException {
      final String moduleName = getModuleName( name, "C" );
      final EnumerationType enm = _model.getEnum( name );
      final ST tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "prefix", moduleName );
      tmpl.add( "enum"  , enm );
      setRendererMaxWidth( enm );
      writeType( moduleName, CRenderer.cname( name ) + ".c", tmpl );
   }

   @Override
   protected void gEnum( String name ) throws IOException {
      generateEnumHeader( name );
      generateEnumBody  ( name );
   }

   private void generateStructHeader( String name ) throws IOException {
      final String moduleName = getModuleName( name, "C" );
      final StructType struct = _model.getStruct( name );
      final ST tmpl = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "prefix", moduleName );
      tmpl.add( "struct", struct   );
      setRendererFieldsMaxWidth( struct );
      writeType( moduleName, CRenderer.cname( name ) + ".h", tmpl );
   }

   private void generateStructBody( String name ) throws IOException {
      final String moduleName = getModuleName( name, "C" );
      final StructType struct = _model.getStruct( name );
      final ST tmpl = _group.getInstanceOf( "/structBody" );
      tmpl.add( "prefix", moduleName );
      tmpl.add( "struct", struct   );
      setRendererFieldsMaxWidth( struct );
      writeType( moduleName, CRenderer.cname( name ) + ".c", tmpl );
   }

   @Override
   protected void struct( String name ) throws IOException {
      generateStructHeader( name );
      generateStructBody  ( name );
   }

   private void generateRequiredInterfaces( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface     = (InterfaceType)required.getInterface();
         final String            ifaceName = iface.getName();
         final SortedSet<String> usedTypes = _model.getUsedTypesBy( ifaceName );
         final int               rawSize   = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final ST                tmpl      = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "prefix"     , _moduleName );
         tmpl.add( "usedTypes"  , usedTypes  );
         tmpl.add( "ifaceName"  , ifaceName );
         tmpl.add( "rawSize"    , rawSize );
         tmpl.add( "iface"      , iface );
         write( CRenderer.cname( ifaceName ) + ".h", tmpl );
      }
   }

   private void generateRequiredImplementations( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface      = (InterfaceType)required.getInterface();
         final String            ifaceName  = iface.getName();
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final int               ifaceID    = _model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "prefix"     , _moduleName );
         tmpl.add( "usedTypes"  , usedTypes );
         tmpl.add( "ifaceName"  , ifaceName );
         tmpl.add( "rawSize"    , rawSize );
         tmpl.add( "iface"      , iface );
         tmpl.add( "ifaceID"    , ifaceID );
         setRendererFieldsMaxWidth( iface );
         write( CRenderer.cname( ifaceName ) + ".c", tmpl );
      }
   }

   private void generateDispatcherInterface( ComponentType component ) throws IOException {
      final Map<String, List<Object>>      reqEvents   = _model.getRequiredEventsOrRequests( component );
      final int rawSize = Math.max( _model.getBufferInCapacity( component ), _model.getBufferResponseCapacity( reqEvents ));
      final Map<String, List<Object>>      events      = _model.getOfferedEventsOrRequests( component );
      final int                            respRawSize = _model.getBufferResponseCapacity( events );
      final ST                             tmpl        = _group.getInstanceOf( "/dispatcherInterface" );
      tmpl.add( "prefix"     , _moduleName );
      tmpl.add( "component"  , component );
      tmpl.add( "rawSize"    , rawSize );
      tmpl.add( "hasResponse", respRawSize > 0 );
      tmpl.add( "respRawSize", respRawSize );
      write( CRenderer.cname( component.getName()) + "_dispatcher.h", tmpl );
   }

   private void generateDispatcherImplementation( ComponentType component ) throws IOException {
      final List<OfferedInterfaceUsageType>    ifaces       = component.getOffers();
      final Map<String, Byte>                  interfaceIDs = _model.getOfferedInterfaceIDs( ifaces );
      final Map<String, Byte>                  ifacesIDs    = _model.getInterfacesID();
      final Map<String, Byte>                  required     = _model.getRequiredInterfaceIDs( component.getRequires());
      final Map<String, List<Object>>          offEvents    = _model.getOfferedEventsOrRequests( component );
      final Map<String, List<Object>>          reqEvents    = _model.getRequiredEventsOrRequests( component );
      final Map<String, Map<String, Byte>>     eventIDs     = _model.getEventIDs();
      final SortedSet<String>                  usedTypes    = _model.getUsedTypesBy( ifaces );
      final Map<InterfaceType, List<DataType>> data         = _model.getRequiredDataOf( component );
      final Map<String, List<RequestType>>     offRequests  = Model.getRequestMap( offEvents );
      final Map<String, List<RequestType>>     reqRequests  = Model.getRequestMap( reqEvents );
      final ST                                 tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "prefix"     , _moduleName );
      tmpl.add( "component"  , component );
      tmpl.add( "ifaces"     , interfaceIDs );
      tmpl.add( "ifacesIDs"  , ifacesIDs );
      tmpl.add( "requires"   , required );
      tmpl.add( "events"     , offEvents );
      tmpl.add( "eventIDs"   , eventIDs );
      tmpl.add( "usedTypes"  , usedTypes );
      tmpl.add( "hasResponse", ! offRequests.isEmpty());
      tmpl.add( "offRequests", offRequests );
      tmpl.add( "reqRequests", reqRequests );
      tmpl.add( "data"       , data );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( CRenderer.cname( component.getName()) + "_dispatcher.c", tmpl );
   }

   private void generateComponentInterface( ComponentType component ) throws IOException {
      final SortedSet<String> usedTypes        = new TreeSet<>();
      final Map<String,
         List<Object>>        eventsOrRequests = new LinkedHashMap<>();
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType     iface     = (InterfaceType)offered.getInterface();
         final String            ifaceName = iface.getName();
         final SortedSet<String> ut        = _model.getUsedTypesBy( ifaceName );
         final List<Object>      eor       = _model.getFacets().get( ifaceName );
         if( ut != null ) {
            usedTypes.addAll( ut );
         }
         if( eor != null ) {
            eventsOrRequests.put( ifaceName, eor );
         }
      }
      final String                             compName        = component.getName();
      final List<InstanceType>                 instances       = _model.getInstancesOf( "TODO", component );// TODO
      final List<InterfaceType>                requires        = Model.getRequiredInterfacesBy( component );
      final Map<String, InstanceType>          instancesByName = _model.getInstancesByName( "TODO" );       // TODO
      final Map<String, List<Object>>          reqEvents       = _model.getRequiredEventsOrRequests( component );
      final Map<String, List<RequestType>>     reqRequests     = Model.getRequestMap( reqEvents );
      final Set<String>                        actions         = _model.getAutomatonActions( component );
      final Map<InterfaceType, List<DataType>> offData         = _model.getOfferedDataOf   ( component );
      final Map<InterfaceType, List<DataType>> reqData         = _model.getRequiredDataOf  ( component );
      final ST tmpl = _group.getInstanceOf( "/componentInterface" );
      tmpl.add( "prefix"          , _moduleName );
      tmpl.add( "component"       , component );
      tmpl.add( "requires"        , requires );
      tmpl.add( "instancesByName" , instancesByName );
      tmpl.add( "instances"       , instances );
      tmpl.add( "usedTypes"       , usedTypes );
      tmpl.add( "eventsOrRequests", eventsOrRequests );
      tmpl.add( "reqRequests"     , reqRequests );
      tmpl.add( "actions"         , actions );
      tmpl.add( "data"            , offData );
      tmpl.add( "reqData"         , reqData );
      write( CRenderer.cname( compName ) + ".h", tmpl );
   }

   private void generateComponentImplementation( ComponentType component ) throws IOException {
      final String                             compName          = component.getName();
      final List<InstanceType>                 instances         = _model.getInstancesOf( "TODO", component );// TODO
      final List<InterfaceType>                requires          = Model.getRequiredInterfacesBy( component );
      final Map<String, Set<InstanceType>>     dataWriter        = _model.getDataWriterOf       ( "TODO", component );// TODO
      final Map<String, InstanceType>          instancesByName   = _model.getInstancesByName    ( "TODO" );// TODO
      final Map<InterfaceType, List<DataType>> offData           = _model.getOfferedDataOf( component );
      final Map<InstanceType, ProcessType>     processByInstance = _model.getProcessByInstance();
      final ST tmpl = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "prefix"         , _moduleName );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , requires );
      tmpl.add( "dataWriter"     , dataWriter );
      tmpl.add( "instancesByName", instancesByName );
      tmpl.add( "instances"      , instances );
      tmpl.add( "data"           , offData );
      tmpl.add( "processes"      , processByInstance );
      write( CRenderer.cname( compName ) + ".c", tmpl );
   }

   private void generateDataWriters( ComponentType component ) throws IOException {
      final Map<InterfaceType, List<DataType>> compData = _model.getOfferedDataOf( component );
      if( compData != null ) {
         for( final OfferedInterfaceUsageType offered : component.getOffers()) {
            final InterfaceType  iface = (InterfaceType)offered.getInterface();
            final List<DataType> data  = compData.get( iface );
            if( data != null ) {
               final String ifaceName = iface.getName();
               final int    ID        = _model.getInterfaceID( ifaceName );
               final int    rawSize   = _model.getDataBufferOutCapacity( data );
               final ST     header    = _group.getInstanceOf( "/dataWriterHeader" );
               header.add( "prefix"     , _moduleName );
               header.add( "interface"  , offered.getInterface());
               header.add( "ifaceID"    , ID );
               header.add( "data"       , data );
               header.add( "rawSize"    , rawSize );
               write( CRenderer.cname( ifaceName ) + "_data.h", header );
               final ST body = _group.getInstanceOf( "/dataWriterBody" );
               body.add( "prefix"     , _moduleName );
               body.add( "interface"  , offered.getInterface());
               body.add( "ifaceID"    , ID );
               body.add( "data"       , data );
               body.add( "dataID"     , _model.getEventIDs().get( ifaceName ));
               body.add( "rawSize"    , rawSize );
               write( CRenderer.cname( ifaceName ) + "_data.c", body );
            }
         }
      }
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

   void generateComponent( ComponentType component, ComponentImplType implementation ) throws IOException {
      _genDir     = implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      typesUsedBy             ( component );
      typesUsedBy             ( component );
      typesUsedBy             ( component );
      generateRequiredInterfaces      ( component );
      generateRequiredImplementations ( component );
      generateDispatcherInterface     ( component );
      generateDispatcherImplementation( component );
      generateComponentInterface      ( component );
      generateComponentImplementation ( component );
      generateDataWriters             ( component );
      generateAutomaton               ( component );
      generateMakefileSourcesList( _generatedFiles, _genDir, _moduleName, ".h", ".c" );
   }

   public void generateTypesMakefileSourcesList() throws FileNotFoundException {
      for( final Entry<String, String> e : _genDirTypes.entrySet()) {
         final String moduleName = e.getKey();
         final String genDir     = e.getValue();
         generateMakefileSourcesList( _generatedTypes, moduleName, genDir + "/src-gen", ".h", ".c" );
      }
   }
}
