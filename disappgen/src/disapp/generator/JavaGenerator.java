package disapp.generator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.AutomatonType;
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
import disapp.generator.model.RequiresType;
import disapp.generator.model.StructType;

public class JavaGenerator extends BaseGenerator {

   public JavaGenerator( Model model, String deployment ) {
      super( model, deployment, "java.stg", new BaseRenderer());
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enum" );
      tmpl.add( "package", _moduleNameTypes );
      tmpl.add( "enum"   , enm );
      setRendererMaxWidth( enm );
      writeType( name + ".java", tmpl );
   }

   @Override
   protected void generateStruct( String name ) throws IOException {
      final StructType struct = _model.getStruct( name );
      final ST         tmpl   = _group.getInstanceOf( "/struct" );
      tmpl.add( "package", _moduleNameTypes );
      tmpl.add( "struct" , struct );
      setRendererFieldsMaxWidth( struct );
      writeType( name + ".java", tmpl );
   }

   private void generateRequiredInterfaces( ComponentType component ) throws IOException {
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType iface     = (InterfaceType)required.getInterface();
         final String        ifaceName = iface.getName();
         final ST            tmpl      = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "typesPackage", _moduleNameTypes );
         tmpl.add( "package"     , _moduleName );
         tmpl.add( "ifaceName"   , ifaceName );
         tmpl.add( "iface"       , iface );
         write( 'I' + ifaceName + ".java", tmpl );
      }
      for( final String ifaceName : Model.getReponses( component )) {
         final InterfaceType iface = _model.getInterface( ifaceName );
         final ST            tmpl  = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "typesPackage", _moduleNameTypes );
         tmpl.add( "package"     , _moduleName );
         tmpl.add( "ifaceName"   , ifaceName );
         tmpl.add( "iface"       , iface );
         write( 'I' + ifaceName + ".java", tmpl );
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
         tmpl.add( "typesPackage", _moduleNameTypes );
         tmpl.add( "package"     , _moduleName );
         tmpl.add( "usedTypes"   , usedTypes );
         tmpl.add( "ifaceName"   , ifaceName );
         tmpl.add( "rawSize"     , rawSize   );
         tmpl.add( "iface"       , iface     );
         tmpl.add( "ifaceID"     , ifaceID   );
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
         tmpl.add( "typesPackage", _moduleNameTypes );
         tmpl.add( "package"     , _moduleName );
         tmpl.add( "name"        , ifaceName );
         tmpl.add( "usedTypes"   , usedTypes );
         tmpl.add( "facets"      , facets );
         write( 'I' + ifaceName + ".java", tmpl );
      }
   }

   private void generateDispatcherImplementation( ComponentType component ) throws IOException {
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
      final int rawSize = Math.max( _model.getBufferInCapacity( component ), _model.getBufferResponseCapacity( reqEvents ));
      final int                                respRawSize = _model.getBufferResponseCapacity( offEvents );
      final Map<InterfaceType, List<DataType>> data        = _model.getRequiredDataOf( component );
      final ST                                 tmpl        = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "typesPackage"  , _moduleNameTypes );
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
      setRendererInterfaceMaxWidth( "width", offers );
      write( component.getName() + "Dispatcher.java", tmpl );
   }

   private void generateComponentImplementation( ComponentType component ) throws IOException {
      final Map<String, List<RequiresType>>    requires  = _model.getRequiredInstancesOf( _deployment, component );
      final Set<String>                        actions   = _model.getAutomatonActions( component );
      final Map<InterfaceType, List<DataType>> offData   = _model.getOfferedDataOf   ( component );
      final Map<InterfaceType, List<DataType>> reqData   = _model.getRequiredDataOf  ( component );
      final Set<String>                        responses = Model.getReponses( component );
      final ST                                 tmpl      = _group.getInstanceOf( "/componentImplementation" );
      tmpl.add( "typesPackage", _moduleNameTypes );
      tmpl.add( "package"     , _moduleName );
      tmpl.add( "component"   , component );
      tmpl.add( "requires"    , requires );
      tmpl.add( "actions"     , actions );
      tmpl.add( "data"        , offData );
      tmpl.add( "reqData"     , reqData );
      tmpl.add( "responses"   , responses );
      write( component.getName() + "Component.java", tmpl );
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
               final ST     tmpl      = _group.getInstanceOf( "/dataWriter" );
               tmpl.add( "typesPackage", _moduleNameTypes );
               tmpl.add( "package"     , _moduleName );
               tmpl.add( "interface"   , offered.getInterface());
               tmpl.add( "ifaceID"     , ID );
               tmpl.add( "data"        , data );
               tmpl.add( "dataID"      , _model.getEventIDs().get( ifaceName ));
               tmpl.add( "rawSize"     , rawSize );
               write( ifaceName + "Data.java", tmpl );
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
               tmpl.add( "package"  , _moduleName );
               tmpl.add( "interface", required.getInterface());
               tmpl.add( "data"     , data );
               write( 'I' + ifaceName + "Data.java", tmpl );
            }
         }
      }
   }

   private void generateAutomaton( ComponentType component ) throws IOException {
      final AutomatonType automaton = component.getAutomaton();
      if( automaton != null ) {
         final ST tmpl = _group.getInstanceOf( "/automaton" );
         tmpl.add( "typesPackage", _moduleNameTypes );
         tmpl.add( "package"     , _moduleName );
         tmpl.add( "component"   , component );
         write( "Automaton.java", tmpl );
      }
   }

   void generateComponent( ComponentType component, ImplementationType implementation ) throws IOException {
      _genDir     = _deployment + '/' + implementation.getSrcDir();
      _moduleName = implementation.getModuleName();
      for( final ImplementationType impl : _model.getApplication().getTypes().getImplementation()) {
         if( impl.getLanguage().equals( "Java" )) {
            _genDirTypes     = _deployment + '/' + impl.getSrcDir();
            _moduleNameTypes = impl.getModuleName();
            break;
         }
      }
      generateTypesUsedBy             ( component );
      generateRequiredInterfaces      ( component );
      generateRequiredImplementations ( component );
      generateOfferedInterfaces       ( component );
      generateDispatcherImplementation( component );
      generateComponentImplementation ( component );
      generateDataWriter              ( component );
      generateDataReader              ( component );
      generateAutomaton               ( component );
   }

   void generateFactory( ProcessType process ) throws IOException {
      for( final InstanceType instance : process.getInstance()) {
         final ComponentType component = (ComponentType)instance.getComponent();
         for( final ImplementationType implementation : component.getImplementation()) {
            if( implementation.getLanguage().equals( "Java" )) {
               _genDir     = _deployment + '/' + implementation.getSrcDir();
               _moduleName = implementation.getModuleName();
               final ST tmplIDispatcher = _group.getInstanceOf( "/IDispatcher" );
               tmplIDispatcher.add( "package", _moduleName );
               write( "IDispatcher.java", tmplIDispatcher );
               final Map<InstanceType, ProcessType>       processes       = _model.getProcessByInstance();
               final Map<InterfaceType, List<DataType>>   offData         = _model.getOfferedDataOf ( component );
               final Map<InterfaceType, List<DataType>>   reqData         = _model.getRequiredDataOf( component );
               final Map<String, InstanceType>            instancesByName = _model.getInstancesByName( _deployment );
               final Map<InterfaceType, Map<String, Set<ProcessType>>> dataConsumer =
                  _model.getDataConsumer( _deployment, component );
               final ST tmpl = _group.getInstanceOf( "/componentFactory" );
               tmpl.add( "typesPackage"   , _moduleNameTypes );
               tmpl.add( "package"        , _moduleName );
               tmpl.add( "process"        , process );
               tmpl.add( "processes"      , processes );
               tmpl.add( "offData"        , offData );
               tmpl.add( "reqData"        , reqData );
               tmpl.add( "instancesByName", instancesByName );
               tmpl.add( "dataConsumer"   , dataConsumer );
               write( "ComponentFactory_" + process.getName() + ".java", tmpl );
               return;
            }
         }
      }
   }
}
