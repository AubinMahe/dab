package disapp.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.ComponentType;
import disapp.generator.model.DataType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.ImplementationType;
import disapp.generator.model.InstanceType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.ProcessType;
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.StructType;

public class CppGenerator extends BaseGenerator {

   public CppGenerator( Model model ) {
      super( model, "cpp.stg", new BaseRenderer() );
   }

   private void generateEnumHeader( EnumerationType enm ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "namespace", _moduleNameTypes );
      tmpl.add( "enum"     , enm );
      setRendererMaxWidth( enm );
      writeType( enm.getName() + ".hpp", tmpl );
   }

   private void generateEnumBody( EnumerationType enm ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "namespace", _moduleNameTypes );
      tmpl.add( "enum"     , enm );
      setRendererMaxWidth( enm );
      writeType( enm.getName() + ".cpp", tmpl );
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      generateEnumHeader( enm );
      generateEnumBody  ( enm );
   }

   private void generateStructHeader( StructType struct ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "namespace", _moduleNameTypes );
      tmpl.add( "struct"   , struct   );
      writeType( struct.getName() + ".hpp", tmpl );
   }

   private void generateStructBody( StructType struct ) throws IOException {
      final ST tmpl   = _group.getInstanceOf( "/structBody" );
      tmpl.add( "namespace", _moduleNameTypes );
      tmpl.add( "struct"   , struct   );
      setRendererFieldsMaxWidth( struct );
      writeType( struct.getName() + ".cpp", tmpl );
   }

   @Override
   protected void generateStruct( String name ) throws IOException {
      final StructType struct = _model.getStruct( name );
      generateStructHeader( struct );
      generateStructBody  ( struct );
   }

   private void generateRequiredInterfaces( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface      = (InterfaceType)required.getInterface();
         final String            ifaceName  = iface.getName();
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final int               ifaceID    = _model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "typesNamespace", _moduleNameTypes );
         tmpl.add( "namespace"     , _moduleName );
         tmpl.add( "ifaceName"     , ifaceName );
         tmpl.add( "usedTypes"     , usedTypes );
         tmpl.add( "rawSize"       , rawSize );
         tmpl.add( "iface"         , iface );
         tmpl.add( "ifaceID"       , ifaceID );
         write( ifaceName + ".hpp", tmpl );
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
         tmpl.add( "typesNamespace", _moduleNameTypes );
         tmpl.add( "namespace"     , _moduleName  );
         tmpl.add( "usedTypes"     , usedTypes );
         tmpl.add( "ifaceName"     , ifaceName );
         tmpl.add( "rawSize"       , rawSize );
         tmpl.add( "iface"         , iface );
         tmpl.add( "ifaceID"       , ifaceID );
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
         tmpl.add( "typesNamespace"  , _moduleNameTypes );
         tmpl.add( "namespace"       , _moduleName );
         tmpl.add( "name"            , ifaceName );
         tmpl.add( "usedTypes"       , usedTypes );
         tmpl.add( "eventsOrRequests", eventsOrRequests );
         write( 'I' + ifaceName + ".hpp", tmpl );
      }
      for( final String ifaceName : Model.getReponses( component )) {
         final SortedSet<String> usedTypes        = _model.getUsedTypesBy( ifaceName );
         final List<Object>      eventsOrRequests = _model.getFacets().get( ifaceName );
         final ST            tmpl  = _group.getInstanceOf( "/offeredInterface" );
         tmpl.add( "typesNamespace"  , _moduleNameTypes );
         tmpl.add( "namespace"       , _moduleName );
         tmpl.add( "name"            , ifaceName );
         tmpl.add( "usedTypes"       , usedTypes );
         tmpl.add( "eventsOrRequests", eventsOrRequests );
         write( 'I' + ifaceName + ".hpp", tmpl );
      }
   }

   private void generateDispatcherInterface( ComponentType component ) throws IOException {
      final List<OfferedInterfaceUsageType>    ifaces       = component.getOffers();
      final Map<String, Byte>                  interfaceIDs = _model.getOfferedInterfaceIDs( ifaces );
      final Map<String, Byte>                  ifacesIDs    = _model.getInterfacesID();
      final Map<String, Byte>                  required     = _model.getRequiredInterfaceIDs( component.getRequires());
      final Map<String, List<Object>>          offEvents    = _model.getOfferedEventsOrRequests( component );
      final Map<String, List<Object>>          reqEvents    = _model.getRequiredEventsOrRequests( component );
      final int rawSize = Math.max( _model.getBufferInCapacity( component ), _model.getBufferResponseCapacity( reqEvents ));
      final int                                respRawSize  = _model.getBufferResponseCapacity( offEvents );
      final Map<InterfaceType, List<DataType>> data         = _model.getRequiredDataOf( component );
      final Map<String, List<RequestType>>     offRequests  = Model.getRequestMap( offEvents );
      final Map<String, List<RequestType>>     reqRequests  = Model.getRequestMap( reqEvents );
      final ST                                 tmpl         = _group.getInstanceOf( "/dispatcherInterface" );
      tmpl.add( "namespace"  , _moduleName );
      tmpl.add( "component"  , component );
      tmpl.add( "ifaces"     , interfaceIDs );
      tmpl.add( "ifacesIDs"  , ifacesIDs );
      tmpl.add( "requires"   , required );
      tmpl.add( "events"     , offEvents );
      tmpl.add( "rawSize"    , rawSize );
      tmpl.add( "hasResponse", respRawSize > 0 );
      tmpl.add( "respRawSize", respRawSize );
      tmpl.add( "data"       , data );
      tmpl.add( "offRequests", offRequests );
      tmpl.add( "reqRequests", reqRequests );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( component.getName() + "Dispatcher.hpp", tmpl );
   }

   private void generateDispatcherImplementation( ComponentType component ) throws IOException {
      final List<OfferedInterfaceUsageType>    ifaces       = component.getOffers();
      final Map<String, Byte>                  interfaceIDs = _model.getOfferedInterfaceIDs( ifaces );
      final Map<String, List<Object>>          offEvents    = _model.getOfferedEventsOrRequests( component );
      final Map<String, List<Object>>          reqEvents    = _model.getRequiredEventsOrRequests( component );
      final Map<String, Map<String, Byte>>     eventIDs     = _model.getEventIDs();
      final SortedSet<String>                  usedTypes    = _model.getUsedTypesBy( ifaces );
      final Map<InterfaceType, List<DataType>> data         = _model.getRequiredDataOf( component );
      final Map<String, List<RequestType>>     offRequests  = Model.getRequestMap( offEvents );
      final Map<String, List<RequestType>>     reqRequests  = Model.getRequestMap( reqEvents );
      final int                                respRawSize  = _model.getBufferResponseCapacity( offEvents );
      final ST                                 tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "typesNamespace", _moduleNameTypes );
      tmpl.add( "namespace"     , _moduleName );
      tmpl.add( "component"     , component );
      tmpl.add( "ifaces"        , interfaceIDs );
      tmpl.add( "events"        , offEvents );
      tmpl.add( "eventIDs"      , eventIDs );
      tmpl.add( "usedTypes"     , usedTypes );
      tmpl.add( "data"          , data );
      tmpl.add( "offRequests"   , offRequests );
      tmpl.add( "reqRequests"   , reqRequests );
      tmpl.add( "hasResponse"   , respRawSize > 0 );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( component.getName() + "Dispatcher.cpp", tmpl );
   }

   private void generateComponentInterface( ComponentType component ) throws IOException {
      final List<InstanceType>                 instances       = _model.getInstancesOf( "TODO", component ); // TODO
      final List<InterfaceType>                requires        = Model.getRequiredInterfacesBy( component );
      final Map<String, InstanceType>          instancesByName = _model.getInstancesByName( "TODO" ); // TODO
      final Set<String>                        actions         = _model.getAutomatonActions( component );
      final Map<InterfaceType, List<DataType>> offData         = _model.getOfferedDataOf( component );
      final Set<String>                        responses       = Model.getReponses( component );
      final Map<InterfaceType, List<DataType>> reqData         = _model.getRequiredDataOf  ( component );
      final ST                                 tmpl            = _group.getInstanceOf( "/componentInterface" );
      tmpl.add( "typesNamespace" , _moduleNameTypes );
      tmpl.add( "namespace"      , _moduleName );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , requires );
      tmpl.add( "instancesByName", instancesByName );
      tmpl.add( "instances"      , instances );
      tmpl.add( "actions"        , actions );
      tmpl.add( "data"           , offData );
      tmpl.add( "responses"      , responses );
      tmpl.add( "reqData"        , reqData );
      write( component.getName() + "Component.hpp", tmpl );
   }

   private void generateComponentImplementation( ComponentType component ) throws IOException {
      final List<InstanceType>                     instances         = _model.getInstancesOf( "TODO", component ); // TODO
      final List<InterfaceType>                    requires          = Model.getRequiredInterfacesBy( component );
      final Map<String, InstanceType>              instancesByName   = _model.getInstancesByName( "TODO" ); // TODO
      final Map<InstanceType, ProcessType>         processByInstance = _model.getProcessByInstance();
      final Map<InterfaceType, List<DataType>>     offData           = _model.getOfferedDataOf( component );
      final Map<InterfaceType, List<DataType>>     reqData           = _model.getRequiredDataOf  ( component );
      final ST                                     tmpl              = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "namespace"      , _moduleName );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , requires );
      tmpl.add( "instancesByName", instancesByName );
      tmpl.add( "instances"      , instances );
      tmpl.add( "processes"      , processByInstance );
      tmpl.add( "data"           , offData );
      tmpl.add( "reqData"        , reqData );
      write( component.getName() + "Component.cpp", tmpl );
   }

   private void generateDataWriter( ComponentType component ) throws IOException {
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
               header.add( "typesNamespace", _moduleNameTypes );
               header.add( "namespace"     , _moduleName );
               header.add( "interface"     , offered.getInterface());
               header.add( "ifaceID"       , ID );
               header.add( "data"          , data );
               header.add( "rawSize"       , rawSize );
               write( ifaceName + "Data.hpp", header );
               final ST body = _group.getInstanceOf( "/dataWriterBody" );
               body.add( "typesNamespace", _moduleNameTypes );
               body.add( "namespace"     , _moduleName );
               body.add( "interface"     , offered.getInterface());
               body.add( "ifaceID"       , ID );
               body.add( "data"          , data );
               body.add( "dataID"        , _model.getEventIDs().get( ifaceName ));
               body.add( "rawSize"       , rawSize );
               write( ifaceName + "Data.cpp", body );
            }
         }
      }
   }

   private void generateDataReader( ComponentType component ) throws IOException {
      final Map<InterfaceType, List<DataType>> compData = _model.getRequiredDataOf( component );
      if( compData != null ) {
         for( final RequiredInterfaceUsageType required : component.getRequires()) {
            final InterfaceType  iface = (InterfaceType)required.getInterface();
            final List<DataType> data  = compData.get( iface );
            if( data != null ) {
               final String ifaceName = iface.getName();
               final ST     tmpl      = _group.getInstanceOf( "/dataReader" );
               tmpl.add( "namespace", _moduleName );
               tmpl.add( "interface", required.getInterface());
               tmpl.add( "data"     , data );
               write( 'I' + ifaceName + "Data.hpp", tmpl );
            }
         }
      }
   }

   private void generateAutomaton( ComponentType component ) throws IOException {
      if( component.getAutomaton() != null ) {
         final ST header = _group.getInstanceOf( "/automatonHeader" );
         header.add( "typesNamespace", _moduleNameTypes );
         header.add( "namespace", _moduleName );
         header.add( "component", component );
         write( "Automaton.hpp", header );
         final ST body = _group.getInstanceOf( "/automatonBody" );
         body.add( "typesNamespace", _moduleNameTypes );
         body.add( "namespace"     , _moduleName  );
         body.add( "component"     , component );
         write( "Automaton.cpp", body );
      }
   }

   void generateComponent( ComponentType component, ImplementationType implementation ) throws IOException {
      _genDir     = implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      for( final ImplementationType impl : _model.getApplication().getTypes().getImplementation()) {
         if( impl.getLanguage().equals( "C++" )) {
            _genDirTypes     = impl.getSrcDir();
            _moduleNameTypes = impl.getModuleName();
            break;
         }
      }
      generateTypesUsedBy             ( component );
      generateRequiredInterfaces      ( component );
      generateRequiredImplementations ( component );
      generateOfferedInterface        ( component );
      generateDispatcherInterface     ( component );
      generateDispatcherImplementation( component );
      generateComponentInterface      ( component );
      generateComponentImplementation ( component );
      generateDataWriter              ( component );
      generateDataReader              ( component );
      generateAutomaton               ( component );
      generateMakefileSourcesList( _generatedFiles, _genDir, _moduleName, ".hpp", ".cpp" );
   }

   public void generateTypesMakefileSourcesList() throws FileNotFoundException {
      generateMakefileSourcesList( _generatedTypes, _genDirTypes + "/src-gen", _moduleNameTypes, ".hpp", ".cpp" );
   }
}
