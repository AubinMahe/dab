package disapp.generator;

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
import disapp.generator.model.AutomatonType;
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

public class JavaGenerator extends BaseGenerator {

   public JavaGenerator( Model model ) {
      super( model, Model.JAVA_LANGUAGE, BaseGenerator.class.getResource( "/resources/java" ), new BaseRenderer());
   }

   @Override
   protected void enumGen( String name ) throws IOException {
      final EnumerationType enm             = _model.getEnum( name );
      final String          modelModuleName = name.substring( 0, name.lastIndexOf( '.' ));
      final String          implModuleName  = _model.getModuleName( modelModuleName, Model.JAVA_LANGUAGE );
      final ST              tmpl            = _group.getInstanceOf( "/enum" );
      tmpl.add( "package", implModuleName );
      tmpl.add( "enum"   , enm );
      setRendererMaxWidth( enm );
      final String filename = name.substring( name.lastIndexOf( '.' ) + 1 ) + ".java";
      final String subPath  = implModuleName.replaceAll( "\\.", "/" );
      writeType( modelModuleName, subPath, filename, tmpl );
   }

   @Override
   protected void struct( String name ) throws IOException {
      final StructType          struct          = _model.getStruct( name );
      final String              modelModuleName = name.substring( 0, name.lastIndexOf( '.' ));
      final String              implModuleName  = _model.getModuleName( modelModuleName, Model.JAVA_LANGUAGE );
      final Map<String, String> types           = _model.getTypes( Model.JAVA_LANGUAGE );
      final ST                  tmpl            = _group.getInstanceOf( "/struct" );
      tmpl.add( "package", implModuleName );
      tmpl.add( "struct" , struct );
      tmpl.add( "types"  , types );
      setRendererFieldsMaxWidth( struct );
      final String filename = name.substring( name.lastIndexOf( '.' ) + 1 ) + ".java";
      final String subPath  = implModuleName.replaceAll( "\\.", "/" );
      writeType( modelModuleName, subPath, filename, tmpl );
   }

   private void interfacesEnum() throws IOException {
      final Map<String, Byte> interfaces = _model.getInterfacesID();
      final TypesType         internal   = _model.getGenInterfaceSettings();
      final TypeImplType      impl       = Model.getModuleName( internal, Model.JAVA_LANGUAGE );
      final ST                tmpl       = _group.getInstanceOf( "/interfacesEnum" );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      tmpl.add( "package"   , _moduleName );
      tmpl.add( "interfaces", interfaces );
      write( "Interfaces.java", tmpl );
   }

   private void interfaces() throws IOException {
      final TypesType           internal = _model.getGenInterfaceSettings();
      final TypeImplType        impl     = Model.getModuleName( internal, Model.JAVA_LANGUAGE );
      final Map<String, String> types    = _model.getTypes( Model.JAVA_LANGUAGE );
      _genDir     = impl.getSrcDir();
      _moduleName = impl.getModuleName();
      for( final InterfaceType iface : _model.getApplication().getInterface()) {
         final String       ifaceName = iface.getName();
         final List<Object> facets    = iface.getEventOrRequestOrData();
         final ST tmpl = _group.getInstanceOf( "/interfaces" );
         tmpl.add( "package"  , _moduleName );
         tmpl.add( "iface"    , iface );
         tmpl.add( "facets"   , facets );
         tmpl.add( "types"    , types );
         write( ifaceName + "Interface.java", tmpl );
      }
   }

   protected void internalTypes() throws IOException {
      interfacesEnum();
      interfaces();
   }

   private void responses( ComponentType component ) throws IOException {
      for( final Entry<InterfaceType, List<RequestType>> e : Model.getResponses( component ).entrySet()) {
         final InterfaceType       iface     = e.getKey();
         final String              ifaceName = iface.getName();
         final List<RequestType>   requests  = e.getValue();
         final Map<String, String> types     = _model.getTypes( Model.JAVA_LANGUAGE );
         final ST tmpl = _group.getInstanceOf( "/responses" );
         tmpl.add( "package"  , _moduleName );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "requests" , requests );
         tmpl.add( "types"    , types );
         write( 'I' + ifaceName + "Responses.java", tmpl );
      }
   }

   private void requiredInterfaces( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType iface = (InterfaceType)required.getInterface();
         final ST            tmpl  = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "package", _moduleName );
         tmpl.add( "iface"  , iface );
         write( 'I' + iface.getName() + ".java", tmpl );
      }
   }

   private void requiredImplementations( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType     iface        = (InterfaceType)required.getInterface();
         final int               rawSize      = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final TypesType         internal     = _model.getGenInterfaceSettings();
         final TypeImplType      internalImpl = Model.getModuleName( internal, Model.JAVA_LANGUAGE );
         final String            intrfcPckg   = internalImpl.getModuleName();
         final ST tmpl = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "package"   , _moduleName );
         tmpl.add( "intrfcPckg", intrfcPckg );
         tmpl.add( "iface"     , iface );
         tmpl.add( "rawSize"   , rawSize );
         setRendererFieldsMaxWidth( iface );
         write( iface.getName() + ".java", tmpl );
      }
   }

   private void offeredInterfaces( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType       iface     = (InterfaceType)offered.getInterface();
         final String              ifaceName = iface.getName();
         final List<Object>        facets    = _model.getFacets().get( ifaceName );
         final Map<String, String> types     = _model.getTypes( Model.JAVA_LANGUAGE );
         final ST                  tmpl      = _group.getInstanceOf( "/offeredInterface" );
         tmpl.add( "package"  , _moduleName );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "facets"   , facets );
         tmpl.add( "types"    , types );
         write( 'I' + ifaceName + ".java", tmpl );
      }
   }

   private void dispatcherImplementation( ComponentType component ) throws IOException {
      final List<InterfaceType>                offered      = Model.getOfferedInterfaces( component );
      final Map<String, List<Object>>          offEvents    = _model.getOfferedFacets( component );
      final Map<String, List<Object>>          reqEvents    = _model.getRequiredFacets( component );
      final Map<String, List<RequestType>>     offRequests  = Model.getRequestMap( offEvents );
      final Map<String, List<RequestType>>     reqRequests  = Model.getRequestMap( reqEvents );
      final int                                respRawSize  = _model.getBufferResponseCapacity( offEvents );
      final Map<InterfaceType, List<DataType>> data         = _model.getRequiredDataOf( component );
      final Map<String, String>                types        = _model.getTypes( Model.JAVA_LANGUAGE );
      final TypesType                          internal     = _model.getGenInterfaceSettings();
      final TypeImplType                       internalImpl = Model.getModuleName( internal, Model.JAVA_LANGUAGE );
      final String                             intrfcPckg   = internalImpl.getModuleName();
      final int rawSize = Math.max( _model.getBufferInCapacity( component ), _model.getBufferResponseCapacity( reqEvents ));
      final ST  tmpl    = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "package"    , _moduleName );
      tmpl.add( "intrfcPckg" , intrfcPckg );
      tmpl.add( "component"  , component );
      tmpl.add( "offers"     , offered );
      tmpl.add( "events"     , offEvents );
      tmpl.add( "rawSize"    , rawSize );
      tmpl.add( "respRawSize", respRawSize );
      tmpl.add( "offRequests", offRequests );
      tmpl.add( "reqRequests", reqRequests );
      tmpl.add( "data"       , data );
      tmpl.add( "types"      , types );
      write( component.getName() + "Dispatcher.java", tmpl );
   }

   private void componentImplementation( ComponentType component ) throws IOException {
      final Set<InterfaceType>                    requires     = Model.getRequiredInterfacesBy( component );
      final Set<String>                           actions      = _model.getAutomatonActions( component );
      final Map<InterfaceType, List<DataType>>    offData      = _model.getOfferedDataOf   ( component );
      final Map<InterfaceType, List<DataType>>    reqData      = _model.getRequiredDataOf  ( component );
      final Map<InterfaceType, List<RequestType>> responses    = Model.getResponses( component );
      final SortedSet<String>                     imports      = new TreeSet<>();
      final Set<String>                           ifaces       = new LinkedHashSet<>();
      final Map<String, String>                   types        = _model.getTypes( Model.JAVA_LANGUAGE );
      final TypesType                             internal     = _model.getGenInterfaceSettings();
      final TypeImplType                          internalImpl = Model.getModuleName( internal, Model.JAVA_LANGUAGE );
      final String                                intrfcPckg   = internalImpl.getModuleName();
      for( final OfferedInterfaceUsageType oiut : component.getOffers()) {
         ifaces.add(((InterfaceType)oiut.getInterface()).getName());
      }
      for( final InterfaceType response : responses.keySet()) {
         ifaces.add( response.getName() + "Responses" );
      }
      if( reqData != null ) {
         for( final InterfaceType data : reqData.keySet()) {
            ifaces.add( data.getName() + "Data" );
         }
      }
      _model.getImports( offData, imports );
      _model.getImports( reqData, imports );
      final ST tmpl = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "package"   , _moduleName );
      tmpl.add( "intrfcPckg", intrfcPckg );
      tmpl.add( "component" , component );
      tmpl.add( "ifaces"    , ifaces );
      tmpl.add( "requires"  , requires );
      tmpl.add( "actions"   , actions );
      tmpl.add( "offData"   , offData );
      tmpl.add( "reqData"   , reqData );
      tmpl.add( "responses" , responses );
      tmpl.add( "imports"   , imports );
      tmpl.add( "types"     , types );
      write( component.getName() + "Component.java", tmpl );
   }

   private void dataWriter( ComponentType component ) throws IOException {
      final Map<InterfaceType, List<DataType>> compData = _model.getOfferedDataOf( component );
      if( compData != null ) {
         for( final OfferedInterfaceUsageType offered : component.getOffers()) {
            final InterfaceType  iface = (InterfaceType)offered.getInterface();
            final List<DataType> data  = compData.get( iface );
            if( data != null ) {
               final String              ifaceName    = iface.getName();
               final int                 rawSize      = _model.getDataBufferOutCapacity( data );
               final TypesType           internal     = _model.getGenInterfaceSettings();
               final TypeImplType        internalImpl = Model.getModuleName( internal, Model.JAVA_LANGUAGE );
               final String              intrfcPckg   = internalImpl.getModuleName();
               final Map<String, String> types        = _model.getTypes( Model.JAVA_LANGUAGE );
               final ST                  tmpl         = _group.getInstanceOf( "/dataWriter" );
               tmpl.add( "package"   , _moduleName );
               tmpl.add( "intrfcPckg", intrfcPckg );
               tmpl.add( "interface" , offered.getInterface());
               tmpl.add( "data"      , data );
               tmpl.add( "rawSize"   , rawSize );
               tmpl.add( "types"     , types );
               write( ifaceName + "Publisher.java", tmpl );
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
               tmpl.add( "package"  , _moduleName );
               tmpl.add( "interface", required.getInterface());
               tmpl.add( "data"     , data );
               write( 'I' + ifaceName + "Data.java", tmpl );
            }
         }
      }
   }

   private void automaton( ComponentType component ) throws IOException {
      final AutomatonType automaton = component.getAutomaton();
      if( automaton != null ) {
         final Map<String, String> types = _model.getTypes( Model.JAVA_LANGUAGE );
         final String stateModelName = automaton.getStateEnum().getName();
         final String stateFullName  = types.get( stateModelName );
         final String stateShortName = stateModelName.substring( stateModelName.lastIndexOf( '.' ) + 1 );
         final String eventModelName = automaton.getEventEnum().getName();
         final String eventFullName  = types.get( eventModelName );
         final String eventShortName = eventModelName.substring( eventModelName.lastIndexOf( '.' ) + 1 );
         final ST tmpl = _group.getInstanceOf( "/automaton" );
         tmpl.add( "package"       , _moduleName );
         tmpl.add( "component"     , component );
         tmpl.add( "stateFullName" , stateFullName );
         tmpl.add( "stateShortName", stateShortName );
         tmpl.add( "eventFullName" , eventFullName );
         tmpl.add( "eventShortName", eventShortName );
         write( "Automaton.java", tmpl );
      }
   }

   void component( ComponentType component, CompImplType implementation ) throws IOException {
      _genDir     = implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      typesUsedBy             ( component );
      responses               ( component );
      requiredInterfaces      ( component );
      requiredImplementations ( component );
      offeredInterfaces       ( component );
      dispatcherImplementation( component );
      componentImplementation ( component );
      dataWriter              ( component );
      dataReader              ( component );
      automaton               ( component );
   }

   private void instancesEnum( String deployment ) throws IOException {
      final Set<InstanceType> instances = _model.getInstances( deployment );
      final ST                tmpl      = _group.getInstanceOf( "/instancesEnum" );
      tmpl.add( "package"  , _moduleName );
      tmpl.add( "instances", instances );
      write( "Instances.java", tmpl );
   }

   void factory(
      DeploymentType                           deployment,
      disapp.generator.genmodel.DeploymentType deploymentImpl,
      ProcessType                              process,
      disapp.generator.genmodel.ProcessType    processImpl,
      FactoryType                              factory      ) throws IOException
   {
      final String                                     dep            = deployment.getName();
      final Map<InstanceType, ProcessType>             processes      = _model.getProcessByInstance( dep );
      final Map<String, String>                        types          = _model.getTypes( Model.JAVA_LANGUAGE );
      final Map<String, Byte>                          ids            = _model.getIDs( dep );
      final Set<InterfaceType>                         offered        = new LinkedHashSet<>();
      final Set<Proxy>                                 proxies        = new LinkedHashSet<>();
      final Set<Proxy>                                 dataPublishers = new LinkedHashSet<>();
      final Map<InstanceType, Set<DataType>>           consumedData   = new LinkedHashMap<>();
      final Map<ComponentType, String>                 modules        = new LinkedHashMap<>();
      final Set<disapp.generator.genmodel.ProcessType> processesImpl  = new LinkedHashSet<>();
      final Set<InterfaceType>                         interfaces     = Model.getOfferedInterfacesBy( process );
      final Set<InterfaceType>                         data           = _model.getRequiredDataInterfacesBy( process );
      final Set<InterfaceType>                         requests       = Model.getRequiredRequestInterfacesBy( process );
      final Set<ComponentType>                         components     = Model.getComponentsOf( process );
      final TypesType                                  internal       = _model.getGenInterfaceSettings();
      final TypeImplType                               internalImpl   = Model.getModuleName( internal, Model.JAVA_LANGUAGE );
      final String                                     intrfcPckg     = internalImpl.getModuleName();
      _model.getFactoryConnections( factory, dep, process, offered, proxies, processesImpl, dataPublishers, consumedData, modules );
      final Set<TimeoutType> timers = new LinkedHashSet<>();
      for( final ComponentType component : components ) {
         timers.addAll( component.getTimeout());
      }
      _moduleName = factory.getModuleName();
      _genDir     = factory.getSrcDir();
      final ST tmpl = _group.getInstanceOf( "/componentFactory" );
      tmpl.add( "package"       , _moduleName );
      tmpl.add( "intrfcPckg"    , intrfcPckg );
      tmpl.add( "deployment"    , deployment );
      tmpl.add( "deploymentImpl", deploymentImpl );
      tmpl.add( "process"       , process );
      tmpl.add( "processImpl"   , processImpl );
      tmpl.add( "processes"     , processes );
      tmpl.add( "processesImpl" , processesImpl );
      tmpl.add( "interfaces"    , interfaces );
      tmpl.add( "data"          , data );
      tmpl.add( "requests"      , requests );
      tmpl.add( "components"    , components );
      tmpl.add( "proxies"       , proxies );
      tmpl.add( "dataPublishers", dataPublishers );
      tmpl.add( "consumedData"  , consumedData );
      tmpl.add( "types"         , types );
      tmpl.add( "modules"       , modules );
      tmpl.add( "ids"           , ids );
      tmpl.add( "timers"        , timers );
      write( "ComponentFactory.java", tmpl );
      instancesEnum( dep );
   }
}
