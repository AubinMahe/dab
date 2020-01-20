package disapp.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import disapp.generator.model.InstanceType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.ProcessType;
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.StructType;
import disapp.generator.model.TimeoutType;

public class CppGenerator extends BaseGenerator {

   public CppGenerator( Model model ) {
      super( model, Model.CPP_LANGUAGE, BaseGenerator.class.getResource( "/resources/cpp" ), new BaseRenderer() );
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
      final TypesType         internal   = _model.getGenInterfaceSettings();
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
      final TypesType         internal   = _model.getGenInterfaceSettings();
      final TypeImplType      impl       = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final ST                tmpl       = _group.getInstanceOf( "/interfacesEnumBody" );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      tmpl.add( "namespace" , _moduleName );
      tmpl.add( "interfaces", interfaces );
      write( "Interfaces.cpp", tmpl );
   }

   private void interfacesHeader() throws IOException {
      final TypesType           internal = _model.getGenInterfaceSettings();
      final TypeImplType        impl     = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final Map<String, String> types    = _model.getTypes( Model.CPP_LANGUAGE );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      for( final InterfaceType iface : _model.getApplication().getInterface()) {
         final List<Object>      facets    = iface.getEventOrRequestOrData();
         final SortedSet<String> includes  = new TreeSet<>();
         Model.getIncludes( types, facets, includes );
         final ST tmpl = _group.getInstanceOf( "/interfacesHeader" );
         tmpl.add( "namespace", _moduleName );
         tmpl.add( "iface"    , iface );
         tmpl.add( "includes" , includes );
         tmpl.add( "facets"   , facets );
         tmpl.add( "types"    , types );
         write( iface.getName() + "Interface.hpp", tmpl );
      }
      final Set<String> timeouts = _model.getTimeouts();
      if( ! timeouts.isEmpty()) {
         final ST tmpl = _group.getInstanceOf( "/timeoutInterfaceHeader" );
         tmpl.add( "namespace", _moduleName );
         tmpl.add( "timeouts" , timeouts );
         write( "TimeoutInterface.hpp", tmpl );
      }
   }

   private void interfacesBody() throws IOException {
      final TypesType           internal = _model.getGenInterfaceSettings();
      final TypeImplType        impl     = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final Map<String, String> types    = _model.getTypes( Model.CPP_LANGUAGE );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      for( final InterfaceType iface : _model.getApplication().getInterface()) {
         final List<Object> facets = iface.getEventOrRequestOrData();
         final Map<RequestType, Integer> requestResponseSizes = _model.getAsynchronousRequestResponseMessageSizes( facets );
         final ST tmpl = _group.getInstanceOf( "/interfacesBody" );
         tmpl.add( "namespace"           , _moduleName );
         tmpl.add( "iface"               , iface );
         tmpl.add( "facets"              , facets );
         tmpl.add( "types"               , types );
         tmpl.add( "requestResponseSizes", requestResponseSizes );
         write( iface.getName() + "Interface.cpp", tmpl );
      }
      final Set<String> timeouts = new LinkedHashSet<>();
      for( final ComponentType component : _model.getApplication().getComponent()) {
         for( final TimeoutType timeout : component.getTimeout()) {
            timeouts.add( BaseRenderer.toID( component.getName() + '_' + timeout.getName()));
         }
      }
      if( ! timeouts.isEmpty()) {
         final ST tmpl = _group.getInstanceOf( "/timeoutInterfaceBody" );
         tmpl.add( "namespace", _moduleName );
         tmpl.add( "timeouts" , timeouts );
         write( "TimeoutInterface.cpp", tmpl );
      }
   }

   protected void internalTypes() throws IOException {
      interfacesEnumHeader();
      interfacesEnumBody();
      interfacesHeader();
      interfacesBody();
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
         final Map<String, String> types     = _model.getTypes( Model.CPP_LANGUAGE );
         final ST                  tmpl      = _group.getInstanceOf( "/requiredInterfaceHeader" );
         tmpl.add( "namespace", _moduleName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "rawSize"  , rawSize );
         tmpl.add( "iface"    , iface );
         tmpl.add( "types"    , types );
         write( iface.getName() + ".hpp", tmpl );
      }
   }

   private void requiredImplementationsBody( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType iface    = (InterfaceType)required.getInterface();
         final TypesType     internal = _model.getGenInterfaceSettings();
         final TypeImplType  impl     = Model.getModuleName( internal, Model.CPP_LANGUAGE );
         final String        ifacesNS = impl.getModuleName();
         final ST tmpl = _group.getInstanceOf( "/requiredImplementationBody" );
         tmpl.add( "namespace"      , _moduleName  );
         tmpl.add( "ifacesNamespace", ifacesNS );
         tmpl.add( "iface"          , iface );
         write( iface.getName() + ".cpp", tmpl );
      }
   }

   private void offeredInterfaceHeader( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType       iface            = (InterfaceType)offered.getInterface();
         final String              ifaceName        = iface.getName();
         final SortedSet<String>   usedTypes        = _model.getUsedTypesBy( ifaceName );
         final List<Object>        eventsOrRequests = _model.getFacets().get( ifaceName );
         final Map<String, String> types            = _model.getTypes( Model.CPP_LANGUAGE );
         final ST tmpl = _group.getInstanceOf( "/offeredInterfaceHeader" );
         tmpl.add( "namespace"       , _moduleName );
         tmpl.add( "ifaceName"       , ifaceName );
         tmpl.add( "usedTypes"       , usedTypes );
         tmpl.add( "eventsOrRequests", eventsOrRequests );
         tmpl.add( "types"           , types );
         write( 'I' + ifaceName + ".hpp", tmpl );
      }
   }

   private void dispatcherHeader( ComponentType component ) throws IOException {
      final Map<String, Byte>                  required    = _model.getRequiredInterfaceIDs( component.getRequires());
      final Map<String, List<Object>>          offered     = _model.getOfferedFacets( component );
      final Map<String, List<Object>>          reqEvents   = _model.getRequiredFacets( component );
      final int                                respRawSize = _model.getBufferResponseCapacity( offered );
      final Map<InterfaceType, List<DataType>> data        = _model.getRequiredDataOf( component );
      final Map<String, List<RequestType>>     offRequests = Model.getRequestMap( offered );
      final Map<String, List<RequestType>>     reqRequests = Model.getRequestMap( reqEvents );
      final TypesType                          internal    = _model.getGenInterfaceSettings();
      final TypeImplType                       impl        = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final String                             ifacesNS    = impl.getModuleName();
      final ST                                 tmpl        = _group.getInstanceOf( "/dispatcherHeader" );
      tmpl.add( "namespace"      , _moduleName );
      tmpl.add( "ifacesNamespace", ifacesNS );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , required );
      tmpl.add( "offered"        , offered );
      tmpl.add( "hasResponse"    , respRawSize > 0 );
      tmpl.add( "respRawSize"    , respRawSize );
      tmpl.add( "data"           , data );
      tmpl.add( "offRequests"    , offRequests );
      tmpl.add( "reqRequests"    , reqRequests );
      write( component.getName() + "Dispatcher.hpp", tmpl );
   }

   private void dispatcherBody( ComponentType component ) throws IOException {
      final Map<String, List<Object>>          offered     = _model.getOfferedFacets( component );
      final Map<String, List<Object>>          reqEvents   = _model.getRequiredFacets( component );
      final Map<InterfaceType, List<DataType>> data        = _model.getRequiredDataOf( component );
      final Map<String, List<RequestType>>     offRequests = Model.getRequestMap( offered );
      final Map<String, List<RequestType>>     reqRequests = Model.getRequestMap( reqEvents );
      final int                                respRawSize = _model.getBufferResponseCapacity( offered );
      final TypesType                          internal    = _model.getGenInterfaceSettings();
      final TypeImplType                       impl        = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final String                             ifacesNS    = impl.getModuleName();
      final Map<String, String>                types       = _model.getTypes( Model.CPP_LANGUAGE );
      final ST tmpl = _group.getInstanceOf( "/dispatcherBody" );
      tmpl.add( "namespace"      , _moduleName );
      tmpl.add( "ifacesNamespace", ifacesNS );
      tmpl.add( "component"      , component );
      tmpl.add( "offered"        , offered );
      tmpl.add( "data"           , data );
      tmpl.add( "offRequests"    , offRequests );
      tmpl.add( "reqRequests"    , reqRequests );
      tmpl.add( "hasResponse"    , respRawSize > 0 );
      tmpl.add( "types"          , types );
      write( component.getName() + "Dispatcher.cpp", tmpl );
   }

   private void componentHeader( ComponentType component ) throws IOException {
      final Set<InterfaceType>                   requires  = Model.getRequiredInterfacesBy( component );
      final Set<String>                           actions   = _model.getAutomatonActions( component );
      final Map<InterfaceType, List<DataType>>    offData   = _model.getOfferedDataOf   ( component );
      final Map<InterfaceType, List<DataType>>    reqData   = _model.getRequiredDataOf  ( component );
      final Map<InterfaceType, List<RequestType>> responses = Model.getResponses( component );
      final TypesType                             internal  = _model.getGenInterfaceSettings();
      final TypeImplType                          impl      = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final String                                ifacesNS  = impl.getModuleName();
      final Map<String, String>                   types     = _model.getTypes( Model.CPP_LANGUAGE );
      final ST tmpl = _group.getInstanceOf( "/componentHeader" );
      tmpl.add( "namespace"      , _moduleName );
      tmpl.add( "ifacesNamespace", ifacesNS );
      tmpl.add( "component"      , component );
      tmpl.add( "requires"       , requires );
      tmpl.add( "actions"        , actions );
      tmpl.add( "offData"        , offData );
      tmpl.add( "reqData"        , reqData );
      tmpl.add( "responses"      , responses );
      tmpl.add( "types"          , types );
      write( component.getName() + "Component.hpp", tmpl );
   }

   private void componentImplementation( ComponentType component ) throws IOException {
      final TypesType    internal = _model.getGenInterfaceSettings();
      final TypeImplType impl     = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final String       ifacesNS = impl.getModuleName();
      final ST tmpl = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "namespace"      , _moduleName );
      tmpl.add( "ifacesNamespace", ifacesNS );
      tmpl.add( "component"      , component );
      write( component.getName() + "Component.cpp", tmpl );
   }

   private void publisher( ComponentType component ) throws IOException {
      final Map<InterfaceType, List<DataType>> compData = _model.getOfferedDataOf( component );
      if( compData != null ) {
         final Map<String, String> types = _model.getTypes( Model.CPP_LANGUAGE );
         for( final OfferedInterfaceUsageType offered : component.getOffers()) {
            final InterfaceType  iface = (InterfaceType)offered.getInterface();
            final List<DataType> data  = compData.get( iface );
            if( data != null ) {
               final TypesType internal  = _model.getGenInterfaceSettings();
               final String    ifaceName = iface.getName();
               final ST header = _group.getInstanceOf( "/publisherHeader" );
               header.add( "namespace", _moduleName );
               header.add( "iface"    , offered.getInterface());
               header.add( "data"     , data );
               header.add( "types"    , types );
               write( ifaceName + "Publisher.hpp", header );
               final int          rawSize  = _model.getDataBufferOutCapacity( data );
               final TypeImplType impl     = Model.getModuleName( internal, Model.CPP_LANGUAGE );
               final String       ifacesNS = impl.getModuleName();
               final ST body = _group.getInstanceOf( "/publisherBody" );
               body.add( "namespace"      , _moduleName );
               body.add( "ifacesNamespace", ifacesNS );
               body.add( "iface"          , offered.getInterface());
               body.add( "data"           , data );
               body.add( "rawSize"        , rawSize );
               body.add( "types"          , types );
               write( ifaceName + "Publisher.cpp", body );
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
      typesUsedBy                ( component );
      responsesHeader            ( component );
      requiredInterfacesHeader   ( component );
      requiredImplementationsBody( component );
      offeredInterfaceHeader     ( component );
      dispatcherHeader           ( component );
      dispatcherBody             ( component );
      componentHeader            ( component );
      componentImplementation    ( component );
      publisher                 ( component );
      dataReaderHeader           ( component );
      automaton                  ( component );
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
      final Set<InterfaceType>               queues         = new LinkedHashSet<>();
      final Set<Proxy>                       proxies        = new LinkedHashSet<>();
      final Set<Proxy>                       dataPublishers = new TreeSet<>();
      final Map<InstanceType, Set<DataType>> consumedData   = new LinkedHashMap<>();
      final Map<ComponentType, String>       modules        = new LinkedHashMap<>();
      final Map<String, String>              types          = _model.getTypes( Model.CPP_LANGUAGE );
      final TypesType                        internal       = _model.getGenInterfaceSettings();
      final TypeImplType                     impl           = Model.getModuleName( internal, Model.CPP_LANGUAGE );
      final String                           ifacesNS       = impl.getModuleName();
      final Set<disapp.generator.genmodel.ProcessType> processesImpl  = new LinkedHashSet<>();
      _model.getFactoryConnections( factory, dep, process, queues, proxies, processesImpl, dataPublishers, consumedData, modules );
      boolean hasTimeout = false;
      for( final ComponentType component : _model.getApplication().getComponent()) {
         if( ! component.getTimeout().isEmpty()) {
            hasTimeout = true;
            break;
         }
      }
      _moduleName = factory.getModuleName();
      _genDir     = factory.getSrcDir();
      {
         final ST tmpl = _group.getInstanceOf( "/componentFactoryHeader" );
         tmpl.add( "namespace"      , _moduleName );
         tmpl.add( "ifacesNamespace", ifacesNS );
         tmpl.add( "process"        , process );
         tmpl.add( "queues"         , queues );
         tmpl.add( "proxies"        , proxies );
         tmpl.add( "dataPublishers" , dataPublishers );
         tmpl.add( "consumedData"   , consumedData );
         tmpl.add( "hasTimeout"     , hasTimeout );
         tmpl.add( "modules"        , modules );
         tmpl.add( "types"          , types );
         write( "ComponentFactory.hpp", tmpl );
      }
      {
         final Map<String, Byte>              ids       = _model.getIDs( dep );
         final Map<InstanceType, ProcessType> processes = _model.getProcessByInstance( dep );
         final ST tmpl = _group.getInstanceOf( "/componentFactoryBody" );
         tmpl.add( "namespace"     , _moduleName );
         tmpl.add( "ifacesNamespace", ifacesNS );
         tmpl.add( "deployment"    , deployment );
         tmpl.add( "deploymentImpl", deploymentImpl );
         tmpl.add( "process"       , process );
         tmpl.add( "processImpl"   , processImpl );
         tmpl.add( "processes"     , processes );
         tmpl.add( "processesImpl" , processesImpl );
         tmpl.add( "queues"        , queues );
         tmpl.add( "hasTimeout"    , hasTimeout );
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
