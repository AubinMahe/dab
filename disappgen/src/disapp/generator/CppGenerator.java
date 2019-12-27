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
   protected void gEnum( String name ) throws IOException {
      enumHeader( name );
      enumBody  ( name );
   }

   private void generateStructHeader( String name ) throws IOException {
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

   private void generateStructBody( String modelName ) throws IOException {
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
      generateStructHeader( name );
      generateStructBody  ( name );
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

   private void requiredInterfaces( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType       iface     = (InterfaceType)required.getInterface();
         final SortedSet<String>   usedTypes = Model.getUserTypesRequiredBy( iface );
         final int                 rawSize   = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final int                 ifaceID   = _model.getInterfaceID( iface.getName());
         final Map<String, String> types     = _model.getTypes( Model.CPP_LANGUAGE );
         final ST                  tmpl      = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "namespace", _moduleName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "rawSize"  , rawSize );
         tmpl.add( "iface"    , iface );
         tmpl.add( "ifaceID"  , ifaceID );
         tmpl.add( "types"    , types );
         write( iface.getName() + ".hpp", tmpl );
      }
   }

   private void requiredImplementations( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface      = (InterfaceType)required.getInterface();
         final String            ifaceName  = iface.getName();
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final int               ifaceID    = _model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredImplementation" );
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

   private void offeredInterface( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType       iface            = (InterfaceType)offered.getInterface();
         final String              ifaceName        = iface.getName();
         final SortedSet<String>   usedTypes        = _model.getUsedTypesBy( ifaceName );
         final List<Object>        eventsOrRequests = _model.getFacets().get( ifaceName );
         final Map<String, String> types            = _model.getTypes( Model.CPP_LANGUAGE );
         final ST                  tmpl             = _group.getInstanceOf( "/offeredInterface" );
         tmpl.add( "namespace"       , _moduleName );
         tmpl.add( "ifaceName"       , ifaceName );
         tmpl.add( "usedTypes"       , usedTypes );
         tmpl.add( "eventsOrRequests", eventsOrRequests );
         tmpl.add( "types"           , types );
         write( 'I' + ifaceName + ".hpp", tmpl );
      }
   }

   private void dispatcherInterface( ComponentType component ) throws IOException {
      final List<OfferedInterfaceUsageType>    ifaces       = component.getOffers();
      final Map<String, Byte>                  interfaceIDs = _model.getOfferedInterfaceIDs( ifaces );
      final Map<String, Byte>                  ifacesIDs    = _model.getInterfacesID();
      final Map<String, Byte>                  required     = _model.getRequiredInterfaceIDs( component.getRequires());
      final Map<String, List<Object>>          offEvents    = _model.getOfferedFacets( component );
      final Map<String, List<Object>>          reqEvents    = _model.getRequiredFacets( component );
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
      final Map<String, Byte>                  interfaceIDs = _model.getOfferedInterfaceIDs( ifaces );
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
      final List<InterfaceType>                   requires  = Model.getRequiredInterfacesBy( component );
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
               final int    ID        = _model.getInterfaceID( ifaceName );
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

   private void dataReader( ComponentType component ) throws IOException {
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

   void generateComponent( ComponentType component, ComponentImplType implementation ) throws IOException {
      _generatedFiles.clear();
      _genDir     = implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      typesUsedBy             ( component );
      responsesHeader         ( component );
      requiredInterfaces      ( component );
      requiredImplementations ( component );
      offeredInterface        ( component );
      dispatcherInterface     ( component );
      dispatcherImplementation( component );
      componentHeader         ( component );
      componentImplementation ( component );
      dataWriter              ( component );
      dataReader              ( component );
      automaton               ( component );
      generateMakefileSourcesList( _generatedFiles, _genDir, true );
   }

   void factory( String deployment, ProcessType process ) throws IOException {
      _moduleName = deployment + "::" + process.getName();
      _genDir     = deployment + '-' + process.getName() + "-cpp/src-gen";
      final Set<Proxy>                       proxies        = new LinkedHashSet<>();
      final Set<Proxy>                       dataPublishers = new LinkedHashSet<>();
      final Map<InstanceType, Set<DataType>> consumedData   = new LinkedHashMap<>();
      final Map<ComponentType, String>       modules        = _model.getModules( Model.CPP_LANGUAGE );
      final Map<String, String>              types          = _model.getTypes( Model.CPP_LANGUAGE );
      _model.getFactoryConnections( Model.CPP_LANGUAGE, deployment, process, proxies, dataPublishers, consumedData );
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
         final Map<String, Byte>              ids       = _model.getIDs( deployment );
         final Map<InstanceType, ProcessType> processes = _model.getProcessByInstance( deployment );
         final ST tmpl = _group.getInstanceOf( "/componentFactoryBody" );
         tmpl.add( "namespace"      , _moduleName );
         tmpl.add( "deployment"    , _model.getDeployment( deployment ));
         tmpl.add( "process"       , process );
         tmpl.add( "processes"     , processes );
         tmpl.add( "proxies"       , proxies );
         tmpl.add( "dataPublishers", dataPublishers );
         tmpl.add( "consumedData"  , consumedData );
         tmpl.add( "types"         , types );
         tmpl.add( "modules"       , modules );
         tmpl.add( "ids"           , ids );
         write( "ComponentFactory.cpp", tmpl );
      }
   }

   public void generateTypesMakefileSourcesList() throws FileNotFoundException {
      for( final String genDir : _genDirTypes.values()) {
         generateMakefileSourcesList( _generatedTypes, genDir, true );
      }
   }
}
