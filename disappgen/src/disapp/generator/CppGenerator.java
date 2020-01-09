package disapp.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.stringtemplate.v4.ST;

import disapp.generator.genmodel.CompImplType;
import disapp.generator.genmodel.FactoryType;
import disapp.generator.genmodel.TypeImplType;
import disapp.generator.genmodel.TypesType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.DataType;
import disapp.generator.model.DeploymentType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.InstanceType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.ProcessType;
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.StructType;

public class CppGenerator extends BaseGenerator {

   public CppGenerator( Model model ) {
      super( model, Model.CPP_LANGUAGE, "cpp.stg", new BaseRenderer() );
   }

   private void enumHeader( String name ) throws IOException {
      final EnumerationType enm             = _model.getEnum( name );
      final String          modelModuleName = name.substring( 0, name.lastIndexOf( '.' ));
      final String          implModuleName  = _model.getModuleName( modelModuleName, Model.CPP_LANGUAGE );
      final ST              tmpl            = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "namespace", implModuleName );
      tmpl.add( "enum"   , enm );
      setRendererMaxWidth( enm );
      final String filename = name.substring( name.lastIndexOf( '.' ) + 1 ) + ".hpp";
      final String subPath  = implModuleName.replaceAll( "::", "/" );
      writeType( modelModuleName, subPath, filename, tmpl );
   }

   private void enumBody( String name ) throws IOException {
      final EnumerationType enm             = _model.getEnum( name );
      final String          modelModuleName = name.substring( 0, name.lastIndexOf( '.' ));
      final String          implModuleName  = _model.getModuleName( modelModuleName, Model.CPP_LANGUAGE );
      final ST              tmpl            = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "namespace", implModuleName );
      tmpl.add( "enum"     , enm );
      setRendererMaxWidth( enm );
      final String filename = name.substring( name.lastIndexOf( '.' ) + 1 ) + ".cpp";
      final String subPath  = implModuleName.replaceAll( "::", "/" );
      writeType( modelModuleName, subPath, filename, tmpl );
   }

   @Override
   protected void enumGen( String name ) throws IOException {
      enumHeader( name );
      enumBody  ( name );
   }

   private void structHeader( String name ) throws IOException {
      final StructType          struct          = _model.getStruct( name );
      final String              modelModuleName = name.substring( 0, name.lastIndexOf( '.' ));
      final String              implModuleName  = _model.getModuleName( modelModuleName, Model.CPP_LANGUAGE );
      final Map<String, String> types           = _model.getTypes( Model.CPP_LANGUAGE );
      final ST                  tmpl            = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "namespace", implModuleName );
      tmpl.add( "struct"   , struct );
      tmpl.add( "types"    , types );
      setRendererFieldsMaxWidth( struct );
      final String filename = name.substring( name.lastIndexOf( '.' ) + 1 ) + ".hpp";
      final String subPath  = implModuleName.replaceAll( "::", "/" );
      writeType( modelModuleName, subPath, filename, tmpl );
   }

   private void structBody( String modelName ) throws IOException {
      final StructType          struct          = _model.getStruct( modelName );
      final String              modelModuleName = modelName.substring( 0, modelName.lastIndexOf( '.' ));
      final String              implModuleName  = _model.getModuleName( modelModuleName, Model.CPP_LANGUAGE );
      final Map<String, String> types           = _model.getTypes( Model.CPP_LANGUAGE );
      final ST                  tmpl            = _group.getInstanceOf( "/structBody" );
      tmpl.add( "namespace", implModuleName );
      tmpl.add( "struct"   , struct );
      tmpl.add( "modelName", modelName );
      tmpl.add( "types"    , types );
      setRendererFieldsMaxWidth( struct );
      final String filename = modelName.substring( modelName.lastIndexOf( '.' ) + 1 ) + ".cpp";
      final String subPath  = implModuleName.replaceAll( "::", "/" );
      writeType( modelModuleName, subPath, filename, tmpl );
   }

   @Override
   protected void struct( String name ) throws IOException {
      structHeader( name );
      structBody  ( name );
   }

   private void interfacesEnumHeader() throws IOException {
      final Map<String, Byte> interfaces = _model.getInterfacesID();
      final TypesType         internal   = _model.getGenInternalTypes();
      final TypeImplType      impl       = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final ST                tmpl       = _group.getInstanceOf( "/interfacesEnumHeader" );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      tmpl.add( "namespace" , _moduleName );
      tmpl.add( "interfaces", interfaces );
      write( "Interfaces.hpp", tmpl );
   }

   private void interfacesEnumBody() throws IOException {
      final Map<String, Byte> interfaces = _model.getInterfacesID();
      final TypesType         internal   = _model.getGenInternalTypes();
      final TypeImplType      impl       = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final ST                tmpl       = _group.getInstanceOf( "/interfacesEnumBody" );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      tmpl.add( "namespace" , _moduleName );
      tmpl.add( "interfaces", interfaces );
      write( "Interfaces.cpp", tmpl );
   }

   private void eventsEnumHeader() throws IOException {
      final TypesType           internal = _model.getGenInternalTypes();
      final TypeImplType        impl     = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final Map<String, String> types    = _model.getTypes( Model.CPP_LANGUAGE );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      for( final InterfaceType iface : _model.getApplication().getInterface()) {
         final String            ifaceName = iface.getName();
         final List<EventType>   events    = _model.getEventsOf( iface );
         final List<RequestType> requests  = _model.getRequestsOf( iface );
         if( ! events.isEmpty() || ! requests.isEmpty()) {
            final List<Object> facets = new ArrayList<>( events.size() + requests.size());
            final SortedSet<String> includes = new TreeSet<>();
            facets.addAll( events );
            facets.addAll( requests );
            Model.getEventIncludes( types, events, includes );
            Model.getRequestIncludes( types, requests, includes );
            final ST tmpl = _group.getInstanceOf( "/eventsEnumHeader" );
            tmpl.add( "namespace"  , _moduleName );
            tmpl.add( "ifaceName", ifaceName );
            tmpl.add( "includes" , includes );
            tmpl.add( "className", ifaceName + Model.IFACE_CLASSIFICATION_EVENT );
            tmpl.add( "facets"   , facets );
            tmpl.add( "types"    , types );
            tmpl.add( "isEvent"  , true );
            write( ifaceName + Model.IFACE_CLASSIFICATION_EVENT + ".hpp", tmpl );
         }
         if( ! requests.isEmpty()) {
            final SortedSet<String> includes = new TreeSet<>();
            Model.getResponseIncludes( types, requests, includes );
            final ST tmpl = _group.getInstanceOf( "/eventsEnumHeader" );
            tmpl.add( "namespace"  , _moduleName );
            tmpl.add( "ifaceName", ifaceName );
            tmpl.add( "includes" , includes );
            tmpl.add( "className", ifaceName + Model.IFACE_CLASSIFICATION_REQUEST );
            tmpl.add( "facets"   , requests );
            tmpl.add( "types"    , types );
            tmpl.add( "isEvent"  , false );
            write( ifaceName + Model.IFACE_CLASSIFICATION_REQUEST + ".hpp", tmpl );
         }
         final List<DataType> data = _model.getDataOf( iface );
         if( ! data.isEmpty()) {
            final SortedSet<String> includes = new TreeSet<>();
            Model.getDataIncludes( types, data, includes );
            final ST tmpl = _group.getInstanceOf( "/eventsEnumHeader" );
            tmpl.add( "namespace"  , _moduleName );
            tmpl.add( "ifaceName", ifaceName );
            tmpl.add( "includes" , includes );
            tmpl.add( "className", ifaceName + Model.IFACE_CLASSIFICATION_DATA );
            tmpl.add( "facets"   , data );
            tmpl.add( "types"    , types );
            tmpl.add( "isEvent"  , false );
            write( ifaceName + Model.IFACE_CLASSIFICATION_DATA + ".hpp", tmpl );
         }
      }
   }

   private void eventsEnumBody() throws IOException {
      final TypesType           internal = _model.getGenInternalTypes();
      final TypeImplType        impl     = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final Map<String, String> types    = _model.getTypes( Model.CPP_LANGUAGE );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      for( final InterfaceType iface : _model.getApplication().getInterface()) {
         final String            ifaceName = iface.getName();
         final List<EventType>   events    = _model.getEventsOf( iface );
         final List<RequestType> requests  = _model.getRequestsOf( iface );
         final List<DataType>    data      = _model.getDataOf( iface );
         if( ! events.isEmpty() || ! requests.isEmpty()) {
            final List<Object> facets = new ArrayList<>( events.size() + requests.size());
            facets.addAll( events );
            facets.addAll( requests );
            final ST tmpl = _group.getInstanceOf( "/eventsEnumBody" );
            tmpl.add( "namespace", _moduleName );
            tmpl.add( "ifaceName", ifaceName );
            tmpl.add( "className", ifaceName + Model.IFACE_CLASSIFICATION_EVENT );
            tmpl.add( "facets"   , facets );
            tmpl.add( "types"    , types );
            tmpl.add( "isEvent"  , true );
            write( ifaceName + Model.IFACE_CLASSIFICATION_EVENT + ".cpp", tmpl );
         }
         if( ! requests.isEmpty()) {
            final ST tmpl = _group.getInstanceOf( "/eventsEnumBody" );
            tmpl.add( "namespace", _moduleName );
            tmpl.add( "ifaceName", ifaceName );
            tmpl.add( "className", ifaceName + Model.IFACE_CLASSIFICATION_REQUEST );
            tmpl.add( "facets"   , requests );
            tmpl.add( "types"    , types );
            tmpl.add( "isEvent"  , false );
            write( ifaceName + Model.IFACE_CLASSIFICATION_REQUEST + ".cpp", tmpl );
         }
         if( ! data.isEmpty()) {
            final ST tmpl = _group.getInstanceOf( "/eventsEnumBody" );
            tmpl.add( "namespace", _moduleName );
            tmpl.add( "ifaceName", ifaceName );
            tmpl.add( "className", ifaceName + Model.IFACE_CLASSIFICATION_DATA );
            tmpl.add( "facets"   , data );
            tmpl.add( "types"    , types );
            tmpl.add( "isEvent"  , false );
            write( ifaceName + Model.IFACE_CLASSIFICATION_DATA + ".cpp", tmpl );
         }
      }
   }

   protected void internalTypes() throws IOException {
      interfacesEnumHeader();
      interfacesEnumBody();
      eventsEnumHeader();
      eventsEnumBody();
      generateMakefileSourcesList( _generatedFiles, _genDir, true );
      _generatedFiles.clear();
   }

   private void responsesHeader( ComponentType component ) throws IOException {
      for( final Entry<InterfaceType, List<RequestType>> e : Model.getResponses( component ).entrySet()) {
         final InterfaceType       iface     = e.getKey();
         final String              ifaceName = iface.getName();
         final List<RequestType>   requests  = e.getValue();
         final Map<String, String> types     = _model.getTypes( Model.CPP_LANGUAGE );
         final ST tmpl = _group.getInstanceOf( "/responsesHeader" );
         tmpl.add( "namespace"  , _moduleName );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "requests" , requests );
         tmpl.add( "types"    , types );
         write( 'I' + ifaceName + "Responses.hpp", tmpl );
      }
   }

   private void requiredInterfacesHeader( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType       iface     = (InterfaceType)required.getInterface();
         final SortedSet<String>   usedTypes = Model.getUserTypesRequiredBy( iface );
         final int                 rawSize   = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final int                 ifaceID   = 12;//_model.getInterfaceID( iface.getName());
         final Map<String, String> types     = _model.getTypes( Model.CPP_LANGUAGE );
         final ST                  tmpl      = _group.getInstanceOf( "/requiredInterfaceHeader" );
         tmpl.add( "namespace", _moduleName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "rawSize"  , rawSize );
         tmpl.add( "iface"    , iface );
         tmpl.add( "ifaceID"  , ifaceID );
         tmpl.add( "types"    , types );
         write( iface.getName() + ".hpp", tmpl );
      }
   }

   private void requiredImplementationsBody( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface      = (InterfaceType)required.getInterface();
         final String            ifaceName  = iface.getName();
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final int               ifaceID    = 12;//_model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredImplementationBody" );
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

   private void offeredInterfaceHeader( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType       iface            = (InterfaceType)offered.getInterface();
         final String              ifaceName        = iface.getName();
         final SortedSet<String>   usedTypes        = _model.getUsedTypesBy( ifaceName );
         final List<Object>        eventsOrRequests = _model.getFacets().get( ifaceName );
         final Map<String, String> types            = _model.getTypes( Model.CPP_LANGUAGE );
         final ST                  tmpl             = _group.getInstanceOf( "/offeredInterfaceHeader" );
         tmpl.add( "namespace"       , _moduleName );
         tmpl.add( "ifaceName"       , ifaceName );
         tmpl.add( "usedTypes"       , usedTypes );
         tmpl.add( "eventsOrRequests", eventsOrRequests );
         tmpl.add( "types"           , types );
         write( 'I' + ifaceName + ".hpp", tmpl );
      }
   }

   private void dispatcherInterfaceHeader( ComponentType component ) throws IOException {
      final List<OfferedInterfaceUsageType>    ifaces       = component.getOffers();
      final Map<String, Byte>                  interfaceIDs = new HashMap<>();//_model.getOfferedInterfaceIDs( ifaces );
      final Map<String, Byte>                  ifacesIDs    = _model.getInterfacesID();
      final Map<String, Byte>                  required     = _model.getRequiredInterfaceIDs( component.getRequires());
      final Map<String, List<Object>>          offEvents    = _model.getOfferedFacets( component );
      final Map<String, List<Object>>          reqEvents    = _model.getRequiredFacets( component );
      final int                                respRawSize  = _model.getBufferResponseCapacity( offEvents );
      final Map<InterfaceType, List<DataType>> data         = _model.getRequiredDataOf( component );
      final Map<String, List<RequestType>>     offRequests  = Model.getRequestMap( offEvents );
      final Map<String, List<RequestType>>     reqRequests  = Model.getRequestMap( reqEvents );
      final ST                                 tmpl         = _group.getInstanceOf( "/dispatcherInterfaceHeader" );
      tmpl.add( "namespace"  , _moduleName );
      tmpl.add( "component"  , component );
      tmpl.add( "ifaces"     , interfaceIDs );
      tmpl.add( "ifacesIDs"  , ifacesIDs );
      tmpl.add( "requires"   , required );
      tmpl.add( "events"     , offEvents );
      tmpl.add( "hasResponse", respRawSize > 0 );
      tmpl.add( "respRawSize", respRawSize );
      tmpl.add( "data"       , data );
      tmpl.add( "offRequests", offRequests );
      tmpl.add( "reqRequests", reqRequests );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( component.getName() + "Dispatcher.hpp", tmpl );
   }

   private void dispatcherImplementation( ComponentType component ) throws IOException {
      final List<OfferedInterfaceUsageType>    ifaces       = component.getOffers();
      final Map<String, Byte>                  interfaceIDs = new HashMap<>();//_model.getOfferedInterfaceIDs( ifaces );
      final Map<String, List<Object>>          offEvents    = _model.getOfferedFacets( component );
      final Map<String, List<Object>>          reqEvents    = _model.getRequiredFacets( component );
      final Map<String, Map<String, Byte>>     eventIDs     = _model.getEventIDs();
      final SortedSet<String>                  usedTypes    = _model.getUsedTypesBy( ifaces );
      final Map<InterfaceType, List<DataType>> data         = _model.getRequiredDataOf( component );
      final Map<String, List<RequestType>>     offRequests  = Model.getRequestMap( offEvents );
      final Map<String, List<RequestType>>     reqRequests  = Model.getRequestMap( reqEvents );
      final int                                respRawSize  = _model.getBufferResponseCapacity( offEvents );
      final Map<String, String>                types        = _model.getTypes( Model.CPP_LANGUAGE );
      final ST                                 tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
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
      tmpl.add( "types"         , types );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( component.getName() + "Dispatcher.cpp", tmpl );
   }

   private void componentHeader( ComponentType component ) throws IOException {
      final Set<InterfaceType>                   requires  = Model.getRequiredInterfacesBy( component );
      final Set<String>                           actions   = _model.getAutomatonActions( component );
      final Map<InterfaceType, List<DataType>>    offData   = _model.getOfferedDataOf   ( component );
      final Map<InterfaceType, List<DataType>>    reqData   = _model.getRequiredDataOf  ( component );
      final Map<InterfaceType, List<RequestType>> responses = Model.getResponses( component );
      final Map<String, String>                   types     = _model.getTypes( Model.CPP_LANGUAGE );
      final ST tmpl = _group.getInstanceOf( "/componentHeader" );
      tmpl.add( "namespace", _moduleName );
      tmpl.add( "component", component );
      tmpl.add( "requires" , requires );
      tmpl.add( "actions"  , actions );
      tmpl.add( "offData"  , offData );
      tmpl.add( "reqData"  , reqData );
      tmpl.add( "responses", responses );
      tmpl.add( "types"    , types );
      write( component.getName() + "Component.hpp", tmpl );
   }

   private void componentImplementation( ComponentType component ) throws IOException {
      final ST tmpl = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "namespace", _moduleName );
      tmpl.add( "component", component );
      write( component.getName() + "Component.cpp", tmpl );
   }

   private void dataWriter( ComponentType component ) throws IOException {
      final Map<InterfaceType, List<DataType>> compData = _model.getOfferedDataOf( component );
      if( compData != null ) {
         final Map<String, String> types = _model.getTypes( Model.CPP_LANGUAGE );
         for( final OfferedInterfaceUsageType offered : component.getOffers()) {
            final InterfaceType  iface = (InterfaceType)offered.getInterface();
            final List<DataType> data  = compData.get( iface );
            if( data != null ) {
               final String ifaceName = iface.getName();
               final int    ID        = 42;//_model.getInterfaceID( ifaceName );
               final int    rawSize   = _model.getDataBufferOutCapacity( data );
               final ST     header    = _group.getInstanceOf( "/dataWriterHeader" );
               header.add( "namespace", _moduleName );
               header.add( "interface", offered.getInterface());
               header.add( "ifaceID"  , ID );
               header.add( "data"     , data );
               header.add( "rawSize"  , rawSize );
               header.add( "types"    , types );
               write( ifaceName + "Data.hpp", header );
               final ST body = _group.getInstanceOf( "/dataWriterBody" );
               body.add( "namespace", _moduleName );
               body.add( "interface", offered.getInterface());
               body.add( "ifaceID"  , ID );
               body.add( "data"     , data );
               body.add( "dataID"   , _model.getEventIDs().get( ifaceName ));
               body.add( "rawSize"  , rawSize );
               body.add( "types"    , types );
               write( ifaceName + "Data.cpp", body );
            }
         }
      }
   }

   private void dataReaderHeader( ComponentType component ) throws IOException {
      final Map<InterfaceType, List<DataType>> compData = _model.getRequiredDataOf( component );
      if( compData != null ) {
         for( final RequiredInterfaceUsageType required : component.getRequires()) {
            final InterfaceType  iface = (InterfaceType)required.getInterface();
            final List<DataType> data  = compData.get( iface );
            if( data != null ) {
               final String ifaceName = iface.getName();
               final ST     tmpl      = _group.getInstanceOf( "/dataReaderHeader" );
               tmpl.add( "namespace", _moduleName );
               tmpl.add( "interface", required.getInterface());
               tmpl.add( "data"     , data );
               write( 'I' + ifaceName + "Data.hpp", tmpl );
            }
         }
      }
   }

   private void automaton( ComponentType component ) throws IOException {
      if( component.getAutomaton() != null ) {
         final Map<String, String> types = _model.getTypes( Model.CPP_LANGUAGE );
         final ST header = _group.getInstanceOf( "/automatonHeader" );
         header.add( "namespace", _moduleName );
         header.add( "component", component );
         header.add( "types"    , types );
         write( "Automaton.hpp", header );
         final ST body = _group.getInstanceOf( "/automatonBody" );
         body.add( "namespace", _moduleName  );
         body.add( "component", component );
         body.add( "types"    , types );
         write( "Automaton.cpp", body );
      }
   }

   void component( ComponentType component, CompImplType implementation ) throws IOException {
      _generatedFiles.clear();
      _genDir     = implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      typesUsedBy             ( component );
      responsesHeader         ( component );
      requiredInterfacesHeader      ( component );
      requiredImplementationsBody ( component );
      offeredInterfaceHeader        ( component );
      dispatcherInterfaceHeader     ( component );
      dispatcherImplementation( component );
      componentHeader         ( component );
      componentImplementation ( component );
      dataWriter              ( component );
      dataReaderHeader              ( component );
      automaton               ( component );
      generateMakefileSourcesList( _generatedFiles, _genDir, true );
   }

   void factory(
      DeploymentType                           deployment,
      disapp.generator.genmodel.DeploymentType deploymentImpl,
      ProcessType                              process,
      disapp.generator.genmodel.ProcessType    processImpl,
      FactoryType                              factory      ) throws IOException
   {
      final String                           dep            = deployment.getName();
      final Set<Proxy>                       proxies        = new LinkedHashSet<>();
      final Set<Proxy>                       dataPublishers = new LinkedHashSet<>();
      final Map<InstanceType, Set<DataType>> consumedData   = new LinkedHashMap<>();
      final Map<ComponentType, String>       modules        = new LinkedHashMap<>();
      final Map<String, String>              types          = _model.getTypes( Model.CPP_LANGUAGE );
      final Set<disapp.generator.genmodel.ProcessType> processesImpl  = new LinkedHashSet<>();
      _model.getFactoryConnections( factory, dep, process, proxies, processesImpl, dataPublishers, consumedData, modules );
      _moduleName = factory.getModuleName();
      _genDir     = factory.getSrcDir();
      {
         final ST tmpl = _group.getInstanceOf( "/componentFactoryHeader" );
         tmpl.add( "namespace"   , _moduleName );
         tmpl.add( "process"     , process );
         tmpl.add( "proxies"     , proxies );
         tmpl.add( "consumedData", consumedData );
         tmpl.add( "modules"     , modules );
         tmpl.add( "types"       , types );
         write( "ComponentFactory.hpp", tmpl );
      }
      {
         final Map<String, Byte>              ids       = _model.getIDs( dep );
         final Map<InstanceType, ProcessType> processes = _model.getProcessByInstance( dep );
         final ST tmpl = _group.getInstanceOf( "/componentFactoryBody" );
         tmpl.add( "namespace"     , _moduleName );
         tmpl.add( "deployment"    , deployment );
         tmpl.add( "deploymentImpl", deploymentImpl );
         tmpl.add( "process"       , process );
         tmpl.add( "processImpl"   , processImpl );
         tmpl.add( "processes"     , processes );
         tmpl.add( "processesImpl" , processesImpl );
         tmpl.add( "proxies"       , proxies );
         tmpl.add( "dataPublishers", dataPublishers );
         tmpl.add( "consumedData"  , consumedData );
         tmpl.add( "types"         , types );
         tmpl.add( "modules"       , modules );
         tmpl.add( "ids"           , ids );
         write( "ComponentFactory.cpp", tmpl );
      }
   }

   public void typesMakefileSourcesList() throws FileNotFoundException {
      for( final String genDir : _genDirTypes.values()) {
         generateMakefileSourcesList( _generatedTypes, genDir, true );
      }
   }
}
