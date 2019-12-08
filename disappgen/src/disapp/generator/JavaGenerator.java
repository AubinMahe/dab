package disapp.generator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.AutomatonType;
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

public class JavaGenerator extends BaseGenerator {

   public JavaGenerator( Model model ) {
      super( model, Model.JAVA_LANGUAGE, "java.stg", new BaseRenderer());
   }

   @Override
   protected void gEnum( String name ) throws IOException {
      final EnumerationType enm = _model.getEnum( name );
      final String          modelModuleName = name.substring( 0, name.lastIndexOf( '.' ));
      final String          implModuleName  = _model.getModuleName( modelModuleName, Model.JAVA_LANGUAGE );
      final ST              tmpl = _group.getInstanceOf( "/enum" );
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
         final InterfaceType     iface     = (InterfaceType)required.getInterface();
         final String            ifaceName = iface.getName();
         final SortedSet<String> usedTypes = _model.getUsedTypesBy( ifaceName );
         final int               rawSize   = _model.getBufferOutCapacity((InterfaceType)required.getInterface());
         final int               ifaceID   = _model.getInterfaceID( ifaceName );
         final SortedSet<String> imports   = new TreeSet<>();
         _model.addImports( usedTypes, imports );
         final ST tmpl = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "package"  , _moduleName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize );
         tmpl.add( "iface"    , iface );
         tmpl.add( "ifaceID"  , ifaceID );
         tmpl.add( "imports"  , imports );
         setRendererFieldsMaxWidth( iface );
         write( ifaceName + ".java", tmpl );
      }
   }

   private void offeredInterfaces( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType       iface     = (InterfaceType)offered.getInterface();
         final String              ifaceName = iface.getName();
         final SortedSet<String>   usedTypes = _model.getUsedTypesBy( ifaceName );
         final List<Object>        facets    = _model.getFacets().get( ifaceName );
         final Map<String, String> types     = _model.getTypes( Model.JAVA_LANGUAGE );
         final ST                  tmpl      = _group.getInstanceOf( "/offeredInterface" );
         tmpl.add( "package"  , _moduleName );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "facets"   , facets );
         tmpl.add( "types"    , types );
         write( 'I' + ifaceName + ".java", tmpl );
      }
   }

   private void dispatcherImplementation( ComponentType component ) throws IOException {
      final List<OfferedInterfaceUsageType>    offers      = component.getOffers();
      final Map<String, Byte>                  offered     = _model.getOfferedInterfaceIDs( offers );
      final Map<String, Byte>                  required    = _model.getRequiredInterfaceIDs( component.getRequires());
      final Map<String, Map<String, Byte>>     eventIDs    = _model.getEventIDs();
      final Map<String, List<Object>>          offEvents   = _model.getOfferedEventsOrRequests( component );
      final Map<String, List<Object>>          reqEvents   = _model.getRequiredEventsOrRequests( component );
      final Map<String, Byte>                  ifacesIDs   = _model.getInterfacesID();
      final SortedSet<String>                  usedTypes   = _model.getUsedTypesBy( offers );
      final Map<String, List<RequestType>>     offRequests = Model.getRequestMap( offEvents );
      final Map<String, List<RequestType>>     reqRequests = Model.getRequestMap( reqEvents );
      final int                                respRawSize = _model.getBufferResponseCapacity( offEvents );
      final Map<InterfaceType, List<DataType>> data        = _model.getRequiredDataOf( component );
      final Map<String, String>                types       = _model.getTypes( Model.JAVA_LANGUAGE );
      final int rawSize = Math.max( _model.getBufferInCapacity( component ), _model.getBufferResponseCapacity( reqEvents ));
      final ST  tmpl    = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "package"      , _moduleName );
      tmpl.add( "component"    , component );
      tmpl.add( "offers"       , offered );
      tmpl.add( "requires"     , required );
      tmpl.add( "events"       , offEvents );
      tmpl.add( "eventIDs"     , eventIDs );
      tmpl.add( "ifacesIDs"    , ifacesIDs );
      tmpl.add( "usedTypes"    , usedTypes );
      tmpl.add( "rawSize"      , rawSize );
      tmpl.add( "respRawSize"  , respRawSize );
      tmpl.add( "offRequests"  , offRequests );
      tmpl.add( "reqRequests"  , reqRequests );
      tmpl.add( "data"         , data );
      tmpl.add( "types"        , types );
      setRendererInterfaceMaxWidth( "width", offers );
      write( component.getName() + "Dispatcher.java", tmpl );
   }

   private void componentImplementation( ComponentType component ) throws IOException {
      final List<InterfaceType>                   requires  = Model.getRequiredInterfacesBy( component );
      final Set<String>                           actions   = _model.getAutomatonActions( component );
      final Map<InterfaceType, List<DataType>>    offData   = _model.getOfferedDataOf   ( component );
      final Map<InterfaceType, List<DataType>>    reqData   = _model.getRequiredDataOf  ( component );
      final Map<InterfaceType, List<RequestType>> responses = Model.getResponses( component );
      final SortedSet<String>                     imports   = new TreeSet<>();
      _model.addImports( offData, imports );
      _model.addImports( reqData, imports );
      final ST tmpl = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "package"  , _moduleName );
      tmpl.add( "component", component );
      tmpl.add( "requires" , requires );
      tmpl.add( "actions"  , actions );
      tmpl.add( "offData"  , offData );
      tmpl.add( "reqData"  , reqData );
      tmpl.add( "responses", responses );
      tmpl.add( "imports"  , imports );
      write( component.getName() + "Component.java", tmpl );
   }

   private void dataWriter( ComponentType component ) throws IOException {
      final Map<InterfaceType, List<DataType>> compData = _model.getOfferedDataOf( component );
      if( compData != null ) {
         for( final OfferedInterfaceUsageType offered : component.getOffers()) {
            final InterfaceType       iface = (InterfaceType)offered.getInterface();
            final List<DataType>      data  = compData.get( iface );
            final Map<String, String> types = _model.getTypes( Model.JAVA_LANGUAGE );
            if( data != null ) {
               final String ifaceName = iface.getName();
               final int    ID        = _model.getInterfaceID( ifaceName );
               final int    rawSize   = _model.getDataBufferOutCapacity( data );
               final ST     tmpl      = _group.getInstanceOf( "/dataWriter" );
               tmpl.add( "package"  , _moduleName );
               tmpl.add( "interface", offered.getInterface());
               tmpl.add( "ifaceID"  , ID );
               tmpl.add( "data"     , data );
               tmpl.add( "dataID"   , _model.getEventIDs().get( ifaceName ));
               tmpl.add( "rawSize"  , rawSize );
               tmpl.add( "types"    , types );
               write( ifaceName + "Data.java", tmpl );
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

   void component( ComponentType component, ComponentImplType implementation ) throws IOException {
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

   void factory( String deployment, ProcessType process ) throws IOException {
      final Map<String, InstanceType>      instancesByName = _model.getInstancesByName( deployment );
      final Map<InstanceType, ProcessType> processes       = _model.getProcessByInstance();
      final Map<String, String>            types           = _model.getTypes( Model.JAVA_LANGUAGE );
      final Map<ComponentType, String>     modules         = _model.getModules( Model.JAVA_LANGUAGE );
      for( final InstanceType instance : process.getInstance()) {
         final ComponentType component = (ComponentType)instance.getComponent();
         for( final ComponentImplType implementation : component.getImplementation()) {
            if( implementation.getLanguage().equals( Model.JAVA_LANGUAGE )) {
               _moduleName = deployment + '.' + process.getName();
               _genDir     = "factories/" + deployment + '/' + process.getName() + "/src-gen";
               final Map<InterfaceType,
                  Map<String, Set<ProcessType>>>        dataConsumer = _model.getDataConsumer( deployment, component );
               final Map<InterfaceType, List<DataType>> offData      = _model.getOfferedDataOf ( component );
               final Map<InterfaceType, List<DataType>> reqData      = _model.getRequiredDataOf( component );
               final ST tmpl = _group.getInstanceOf( "/componentFactory" );
               tmpl.add( "package"        , _moduleName );
               tmpl.add( "process"        , process );
               tmpl.add( "processes"      , processes );
               tmpl.add( "offData"        , offData );
               tmpl.add( "reqData"        , reqData );
               tmpl.add( "instancesByName", instancesByName );
               tmpl.add( "dataConsumer"   , dataConsumer );
               tmpl.add( "modules"        , modules );
               tmpl.add( "types"          , types );
               write( "ComponentFactory.java", tmpl );
               return;
            }
         }
      }
   }
}
