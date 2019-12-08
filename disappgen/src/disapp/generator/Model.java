package disapp.generator;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import disapp.generator.model.AutomatonType;
import disapp.generator.model.ComponentImplType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.DataType;
import disapp.generator.model.DeploymentType;
import disapp.generator.model.DisappType;
import disapp.generator.model.EnumType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.FieldtypeType;
import disapp.generator.model.InstanceType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.LiteralType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.ProcessType;
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.RequiresType;
import disapp.generator.model.ShortcutType;
import disapp.generator.model.StructType;
import disapp.generator.model.TransitionType;
import disapp.generator.model.TypesImplType;
import disapp.generator.model.TypesType;

final class Model {

   static protected final String JAVA_LANGUAGE = "Java";
   static protected final String CPP_LANGUAGE  = "C++";
   static protected final String C_LANGUAGE    = "C";

   private final Map<String, InterfaceType>             _interfaces         = new LinkedHashMap<>();
   private final Map<String, EnumerationType>           _enums              = new LinkedHashMap<>();
   private final Map<String, StructType>                _structs            = new LinkedHashMap<>();
   private final Map<String, SortedSet<String>>         _usedTypes          = new LinkedHashMap<>();
   private final Map<String, List<Object>>              _facetsByName       = new LinkedHashMap<>();
   private final Map<String, Map<String, InstanceType>> _instancesByName    = new LinkedHashMap<>();
   private final Map<InstanceType, ProcessType>         _processByInstance  = new LinkedHashMap<>();
   private final Map<String, Byte>                      _interfacesID       = new LinkedHashMap<>();
   private final Map<String, Map<String, Byte>>         _eventIDs           = new LinkedHashMap<>();
   private final Map<ComponentType, Set<String>>        _actions            = new LinkedHashMap<>();
   private final Map<String, SortedMap<String, String>> _typesModel2Impl    = new HashMap<>();
   private final Map<ComponentType,
      Map<InterfaceType,
         List<DataType>>>                               _offeredData        = new LinkedHashMap<>();
   private final Map<ComponentType,
      Map<InterfaceType,
         List<DataType>>>                               _requiredData       = new LinkedHashMap<>();
   private final File                                   _source;
   private final boolean                                _force;
   private final DisappType                             _application;
   private final long                                   _lastModified;

   @SuppressWarnings("unchecked")
   private static JAXBElement<DisappType> readXIncludeAwareModel( File source ) throws Exception {
      JAXBElement<DisappType> elt = null;
      final SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setXIncludeAware( true );
      spf.setNamespaceAware( true );
      spf.setValidating( true );
      final XMLReader xr = spf.newSAXParser().getXMLReader();
      try( final Reader reader = new FileReader( source )) {
         final SAXSource src = new SAXSource( xr, new InputSource( reader ));
         final JAXBContext jaxbContext = JAXBContext.newInstance( "disapp.generator.model" );
         final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
         elt = (JAXBElement<DisappType>)unmarshaller.unmarshal( src );
      }
      return elt;
   }

   Model( File source, boolean force ) throws Exception {
      final JAXBElement<DisappType> elt = readXIncludeAwareModel( source );
      _source       = source;
      _force        = force;
      _application  = elt.getValue();
      _lastModified = _source.lastModified();
      for( final TypesType types : _application.getTypes()) {
         for( final EnumerationType enm : types.getEnumeration()) {
            _enums.put( types.getModuleName() + '.' + enm.getName(), enm );
         }
         for( final StructType struct : types.getStruct()) {
            _structs.put( types.getModuleName() + '.' + struct.getName(), struct );
         }
      }
      for( final InterfaceType iface : _application.getInterface()) {
         _interfaces.put( iface.getName(), iface );
         _facetsByName.put( iface.getName(), iface.getEventOrRequestOrData());
      }
      for( final DeploymentType deployment : _application.getDeployment()) {
         final Map<String, InstanceType> instancesByName = new LinkedHashMap<>();
         _instancesByName.put( deployment.getName(), instancesByName );
         for( final ProcessType process : deployment.getProcess()) {
            for( final InstanceType instance : process.getInstance()) {
               _processByInstance.put( instance, process );
               instancesByName.put( instance.getName(), instance );
            }
         }
      }
      for( final ComponentType component : _application.getComponent()) {
         final AutomatonType automaton = component.getAutomaton();
         if( automaton == null ) {
            _actions.put( component, null );
         }
         else {
            createEventAndStateIfNecessary( automaton );
            _actions.put(
               component,
               automaton
                  .getOnEntryOrOnExit()
                  .stream()
                  .map( e -> e.getValue())
                  .map( action -> action.getAction())
                  .collect( Collectors.toCollection( LinkedHashSet::new )));
         }
      }
      for( final ComponentType component : _application.getComponent()) {
         typesUsedBy( component );
      }
      for( final InterfaceType iface : _application.getInterface()) {
         final String            ifaceName = iface.getName();
         final Map<String, Byte> eventIDs  = new LinkedHashMap<>();
         _eventIDs.put( ifaceName, eventIDs );
         byte id = 0;
         for( final Object facet : iface.getEventOrRequestOrData()) {
            if( facet instanceof EventType ) {
               final EventType event = (EventType)facet;
               eventIDs.put( event.getName(), ++id );
            }
            else if( facet instanceof RequestType ) {
               final RequestType request = (RequestType)facet;
               eventIDs.put( request.getName(), ++id );
            }
            else if( facet instanceof DataType ) {
               final DataType data = (DataType)facet;
               eventIDs.put( data.getName(), ++id );
            }
            else {
               throw new IllegalStateException( "Unexpected class: " + facet.getClass());
            }
         }
      }
      for( final ComponentType component : _application.getComponent()) {
         final Map<InterfaceType, List<DataType>> allData = new LinkedHashMap<>();
         for( final OfferedInterfaceUsageType offered : component.getOffers()) {
            final InterfaceType iface = (InterfaceType)offered.getInterface();
            final List<DataType> data = new LinkedList<>();
            for( final Object facet : iface.getEventOrRequestOrData()) {
               if( facet instanceof DataType ) {
                  data.add((DataType)facet );
               }
            }
            if( ! data.isEmpty()) {
               allData.put( iface, data );
            }
         }
         if( ! allData.isEmpty()) {
            _offeredData.put( component, allData );
         }
      }
      for( final ComponentType component : _application.getComponent()) {
         final Map<InterfaceType, List<DataType>> allData = new LinkedHashMap<>();
         for( final RequiredInterfaceUsageType required : component.getRequires()) {
            final InterfaceType iface = (InterfaceType)required.getInterface();
            final List<DataType> data = new LinkedList<>();
            for( final Object facet : iface.getEventOrRequestOrData()) {
               if( facet instanceof DataType ) {
                  data.add((DataType)facet );
               }
            }
            if( ! data.isEmpty()) {
               allData.put( iface, data );
            }
         }
         if( ! allData.isEmpty()) {
            _requiredData.put( component, allData );
         }
      }
      byte ifaceID = 0;
      for( final Entry<String, InterfaceType> e : _interfaces.entrySet()) {
         final String name = e.getKey();
         _interfacesID.put( name, ++ifaceID );
         for( final Object facet : e.getValue().getEventOrRequestOrData()) {
            if( facet instanceof RequestType ) {
               _interfacesID.put( name + "Response", ++ifaceID );
            }
         }
      }
      for( final TypesType types : _application.getTypes()) {
         for( final EnumerationType type : types.getEnumeration()) {
            for( final TypesImplType impl : types.getImplementation()) {
               SortedMap<String, String> typesModel2Impl = _typesModel2Impl.get( impl.getLanguage());
               if( typesModel2Impl == null ) {
                  _typesModel2Impl.put( impl.getLanguage(), typesModel2Impl = new TreeMap<>());
               }
               final String language = impl.getLanguage();
               final String model    = types.getModuleName() + '.' + type.getName();
               final String lang;
               switch( language ) {
               case JAVA_LANGUAGE: lang = impl.getModuleName() + '.'  + type.getName(); break;
               case CPP_LANGUAGE : lang = impl.getModuleName() + "::" + type.getName(); break;
               case C_LANGUAGE   : lang = impl.getModuleName() + '_'  + type.getName(); break;
               default: throw new IllegalStateException( language );
               }
               typesModel2Impl.put( model, lang );
            }
         }
         for( final StructType type : types.getStruct()) {
            for( final TypesImplType impl : types.getImplementation()) {
               SortedMap<String, String> typesModel2Impl = _typesModel2Impl.get( impl.getLanguage());
               if( typesModel2Impl == null ) {
                  _typesModel2Impl.put( impl.getLanguage(), typesModel2Impl = new TreeMap<>());
               }
               final String language = impl.getLanguage();
               final String model    = types.getModuleName() + '.' + type.getName();
               final String lang;
               switch( language ) {
               case JAVA_LANGUAGE: lang = impl.getModuleName() + '.'  + type.getName(); break;
               case CPP_LANGUAGE : lang = impl.getModuleName() + "::" + type.getName(); break;
               case C_LANGUAGE   : lang = impl.getModuleName() + '_'  + type.getName(); break;
               default: throw new IllegalStateException( language );
               }
               typesModel2Impl.put( model, lang );
            }
         }
      }
   }

   static String toString( FieldType field ) {
      return "Name: "      + field.getName() +
         ", description: " + field.getDescription() +
         ", value: "       + field.getValue() +
         ", type: "        + field.getType() +
         ", userType: "    + field.getUserType();
   }

   private void createEventAndStateIfNecessary( AutomatonType automaton ) {
      final String event = automaton.getEventEnum().getName();
      if( ! _enums.containsKey( event )) {
         final EnumerationType enumeration = new EnumerationType();
         enumeration.setName( event.substring( event.lastIndexOf( '.' ) + 1 ));
         final SortedSet<LiteralType> literals = new TreeSet<>(( l, r ) -> l.getName().compareTo( r.getName()));
         for( final TransitionType transition : automaton.getTransition()) {
            final LiteralType literal = new LiteralType();
            literal.setName( transition.getEvent());
            literals.add( literal );
         }
         for( final ShortcutType shortcut : automaton.getShortcut()) {
            final LiteralType literal = new LiteralType();
            literal.setName( shortcut.getEvent());
            literals.add( literal );
         }
         enumeration.getLiteral().addAll( literals );
         if( literals.size() == 2 ) {
            enumeration.setType( EnumType.BOOLEAN );
         }
         else if( literals.size() < ( 2 << Byte.SIZE )) {
            enumeration.setType( EnumType.BYTE );
         }
         else if( literals.size() < ( 2 << Short.SIZE )) {
            enumeration.setType( EnumType.USHORT );
         }
         else {
            enumeration.setType( EnumType.UINT );
         }
         _enums.put( event, enumeration );
         final String moduleName = event.substring( 0, event.lastIndexOf( '.' ));
         for( final TypesType types : _application.getTypes()) {
            if( types.getModuleName().equals( moduleName )) {
               types.getEnumeration().add( enumeration );
               break;
            }
         }
      }
      final String state = automaton.getStateEnum().getName();
      if( ! _enums.containsKey( state )) {
         final EnumerationType enumeration = new EnumerationType();
         enumeration.setName( state.substring( event.lastIndexOf( '.' ) + 1 ));
         final SortedSet<LiteralType> literals = new TreeSet<>(( l, r ) -> l.getName().compareTo( r.getName()));
         for( final TransitionType transition : automaton.getTransition()) {
            LiteralType literal = new LiteralType();
            literal.setName( transition.getFrom());
            literals.add( literal );
            literal = new LiteralType();
            literal.setName( transition.getFutur());
            literals.add( literal );
         }
         for( final ShortcutType shortcut : automaton.getShortcut()) {
            final LiteralType literal = new LiteralType();
            literal.setName( shortcut.getFutur());
            literals.add( literal );
         }
         enumeration.getLiteral().addAll( literals );
         if( literals.size() == 2 ) {
            enumeration.setType( EnumType.BOOLEAN );
         }
         else if( literals.size() < ( 2 << Byte.SIZE )) {
            enumeration.setType( EnumType.BYTE );
         }
         else if( literals.size() < ( 2 << Short.SIZE )) {
            enumeration.setType( EnumType.USHORT );
         }
         else {
            enumeration.setType( EnumType.UINT );
         }
         _enums.put( state, enumeration );
         final String moduleName = state.substring( 0, state.lastIndexOf( '.' ));
         for( final TypesType types : _application.getTypes()) {
            if( types.getModuleName().equals( moduleName )) {
               types.getEnumeration().add( enumeration );
               break;
            }
         }
      }
   }

   Map<String, Map<String, Byte>> getEventIDs() {
      return _eventIDs;
   }

   static List<RequestType> getRequestsOf( InterfaceType iface ) {
      final List<RequestType> requests = new ArrayList<>( iface.getEventOrRequestOrData().size());
      for( final Object facet : iface.getEventOrRequestOrData()) {
         if( facet instanceof RequestType ) {
            requests.add((RequestType)facet );
         }
      }
      return requests;
   }

   private void typesUsedBy( String ifaceName, String typeName ) {
      SortedSet<String> types = _usedTypes.get( ifaceName );
      if( types == null ) {
         _usedTypes.put( ifaceName, types = new TreeSet<>());
      }
      else if( types.contains( typeName )) {
         return;
      }
      types.add( typeName );
      if( isStruct( typeName )) {
         final StructType struct = _structs.get( typeName );
         for( final FieldType field : struct.getField()) {
            typesUsedBy( ifaceName, field );
         }
      }
   }

   private void typesUsedBy( String ifaceName, FieldType field ) {
      final String typeName = field.getUserType();
      if( typeName != null ) {
         typesUsedBy( ifaceName, typeName );
         if( field.getType() == FieldtypeType.STRUCT ) {
            final StructType struct = _structs.get( typeName );
            for( final FieldType field2 : struct.getField()) {
               typesUsedBy( ifaceName, field2 );
            }
         }
      }
   }

   private void typesUsedBy( String ifaceName, List<FieldType> fields ) {
      for( final FieldType field : fields ) {
         typesUsedBy( ifaceName, field );
      }
   }

   private void typesUsedBy( InterfaceType iface ) {
      final String ifaceName = iface.getName();
      for( final Object facet : iface.getEventOrRequestOrData()) {
         if( facet instanceof EventType ) {
            final EventType event = (EventType)facet;
            typesUsedBy( ifaceName, event.getField());
         }
         else if( facet instanceof RequestType) {
            final RequestType request = (RequestType)facet;
            typesUsedBy( ifaceName, request.getArguments().getField());
            typesUsedBy( ifaceName, request.getType());
         }
         else if( facet instanceof DataType ) {
            final DataType data = (DataType)facet;
            typesUsedBy( ifaceName, data.getType());
         }
         else {
            throw new IllegalStateException( "unexpected class: " + facet.getClass());
         }
      }
      for( final RequestType request : getRequestsOf( iface )) {
         typesUsedBy( ifaceName, request.getArguments().getField());
      }
   }

   private void typesUsedBy( ComponentType component ) {
      for( final OfferedInterfaceUsageType facet : component.getOffers()) {
         typesUsedBy((InterfaceType)facet.getInterface());
      }
      for( final RequiredInterfaceUsageType facet : component.getRequires()) {
         typesUsedBy((InterfaceType)facet.getInterface());
      }
   }

   boolean isUpToDate( File target ) {
      return ( ! _force )&&( target.lastModified() > _lastModified );
   }

   DisappType getApplication() {
      return _application;
   }

   InterfaceType getInterface( String name ) {
      return _interfaces.get( name );
   }

   Map<String, Byte> getInterfacesID() {
      return _interfacesID;
   }

   byte getInterfaceID( String ifaceName ) {
      final Byte id = _interfacesID.get( ifaceName );
      if( id == null ) {
         throw new IllegalStateException( ifaceName + " isn't an interface" );
      }
      return id.byteValue();
   }

   Map<String, Byte> getRequiredInterfaceIDs( List<RequiredInterfaceUsageType> requires ) {
      final Map<String, Byte> ifaces = new LinkedHashMap<>();
      for( final RequiredInterfaceUsageType ifaceUsage : requires ) {
         final InterfaceType iface     = (InterfaceType)ifaceUsage.getInterface();
         final String        ifaceName = iface.getName();
         ifaces.put( ifaceName, getInterfaceID( ifaceName ));
      }
      return ifaces;
   }

   Map<String, Byte> getOfferedInterfaceIDs( List<OfferedInterfaceUsageType> offers ) {
      final Map<String, Byte> ifaces = new LinkedHashMap<>();
      for( final OfferedInterfaceUsageType ifaceUsage : offers ) {
         final InterfaceType iface     = (InterfaceType)ifaceUsage.getInterface();
         final String        ifaceName = iface.getName();
         ifaces.put( ifaceName, getInterfaceID( ifaceName ));
      }
      return ifaces;
   }

   boolean isEnum( String name ) {
      return _enums.containsKey( name );
   }

   EnumerationType getEnum( String name ) {
      final EnumerationType enm = _enums.get( name );
      if( enm == null ) {
         throw new IllegalStateException( "'" + name + "' is not an enumeration." );
      }
      return enm;
   }

   EnumerationType getEnum( FieldType field ) {
      return getEnum( field.getUserType());
   }

   boolean isStruct( String name ) {
      return _structs.containsKey( name );
   }

   StructType getStruct( FieldType field ) {
      return getStruct( field.getUserType());
   }

   StructType getStruct( String name ) {
      final StructType struct = _structs.get( name );
      if( struct == null ) {
         throw new IllegalStateException( "'" + name + "' is not a struct." );
      }
      return struct;
   }

   SortedSet<String> getUsedTypesBy( String ifaceName ) {
      return _usedTypes.get( ifaceName );
   }

   SortedSet<String> getUsedTypesBy( List<OfferedInterfaceUsageType> allOffered ) {
      final SortedSet<String> usedTypes = new TreeSet<>();
      for( final OfferedInterfaceUsageType offered : allOffered ) {
         final InterfaceType     iface     = (InterfaceType)offered.getInterface();
         final String            ifaceName = iface.getName();
         final SortedSet<String> used      = _usedTypes.get( ifaceName );
         if( used != null ) {
            usedTypes.addAll( used );
         }
      }
      return usedTypes;
   }

   Map<String, List<Object>> getFacets() {
      return _facetsByName;
   }

   Map<String, List<Object>> getOfferedEventsOrRequests( ComponentType component ) {
      final Map<String, List<Object>> eventsOrRequestsForOneComponent = new LinkedHashMap<>();
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType iface            = (InterfaceType)offered.getInterface();
         final String        ifaceName        = iface.getName();
         final List<Object>  eventsOrRequests = _facetsByName.get( ifaceName );
         eventsOrRequestsForOneComponent.put( ifaceName, eventsOrRequests );
      }
      return eventsOrRequestsForOneComponent;
   }

   Map<String, List<Object>> getRequiredEventsOrRequests( ComponentType component ) {
      final Map<String, List<Object>> eventsOrRequestsForOneComponent = new LinkedHashMap<>();
      for( final RequiredInterfaceUsageType required : component.getRequires()) {
         final InterfaceType iface            = (InterfaceType)required.getInterface();
         final String        ifaceName        = iface.getName();
         final List<Object>  eventsOrRequests = _facetsByName.get( ifaceName );
         eventsOrRequestsForOneComponent.put( ifaceName, eventsOrRequests );
      }
      return eventsOrRequestsForOneComponent;
   }

   private static int getEnumSize( EnumerationType enm ) {
      switch( enm.getType()) {
      case BOOLEAN  : return 1;
      case BYTE     : return 1;
      case SHORT    : return 2;
      case USHORT   : return 2;
      case INT      : return 4;
      case UINT     : return 4;
      default       : throw new IllegalStateException();
      }
   }

   int getFieldSize( FieldType field ) {
      final FieldtypeType type = field.getType();
      switch( type ) {
      case BOOLEAN: return 1;
      case BYTE   : return 1;
      case SHORT  : return 2;
      case USHORT : return 2;
      case INT    : return 4;
      case UINT   : return 4;
      case LONG   : return 8;
      case ULONG  : return 8;
      case FLOAT  : return 4;
      case DOUBLE : return 8;
      case STRING : return 4 + field.getLength().intValue();
      case ENUM   : return getEnumSize  ( getEnum( field ));
      case STRUCT : return getStructSize( getStruct( field ));
      default     : throw new IllegalStateException();
      }
   }

   int getStructSize( StructType struct ) {
      int msgSize = 0;
      for( final FieldType field : struct.getField()) {
         msgSize += getFieldSize( field );
      }
      return msgSize;
   }

   /**
    * Compute the size of an "event" or a "request" message
    * @param fields the list of fields
    * @return the size, with the header.
    */
   int getMessageSize( List<FieldType> fields ) {
      int msgSize = 1 + 1; // INTERFACE + EVENT
      for( final FieldType field : fields ) {
         msgSize += getFieldSize( field );
      }
      return msgSize;
   }

   /**
    * Compute the size of a "data" message (publish/subscribe)
    * @param field the field
    * @return the size, with the header.
    */
   int getMessageSize( DataType data ) {
      int msgSize = 1 + 1; // INTERFACE + EVENT
      msgSize += getStructSize( _structs.get( data.getType()));
      return msgSize;
   }

   int getBufferInCapacity( ComponentType component ) {
      int capacity = 0;
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType iface     = (InterfaceType)offered.getInterface();
         final String        ifaceName = iface.getName();
         final List<Object>  facets    = _facetsByName.get( ifaceName );
         for( final Object facet : facets ) {
            if( facet instanceof EventType ) {
               final EventType event = (EventType)facet;
               capacity = Math.max( capacity, getMessageSize( event.getField()));
            }
            else if( facet instanceof RequestType) {
               final RequestType request = (RequestType)facet;
               capacity = Math.max( capacity, getMessageSize( request.getArguments().getField()));
            }
            else if( facet instanceof DataType) {
               final DataType data = (DataType)facet;
               capacity = Math.max( capacity, getMessageSize( data ));
            }
            else {
               throw new IllegalStateException( "Unexpected class: " + facet.getClass());
            }
         }
      }
      final Map<InterfaceType, List<DataType>> allData = getRequiredDataOf( component );
      if( allData != null ) {
         for( final List<DataType> dataList : allData.values()) {
            for( final DataType data : dataList ) {
               capacity = Math.max( capacity, getMessageSize( data ));
            }
         }
      }
      return capacity;
   }

   int getBufferOutCapacity( InterfaceType required ) {
      int capacity = 0;
      final String        ifaceName = required.getName();
      final List<Object> facets    = _facetsByName.get( ifaceName );
      for( final Object facet : facets ) {
         if( facet instanceof EventType ) {
            final EventType event = (EventType)facet;
            capacity = Math.max( capacity, getMessageSize( event.getField()));
         }
         else if( facet instanceof RequestType ) {
            final RequestType request = (RequestType)facet;
            capacity = Math.max( capacity, getMessageSize( request.getArguments().getField()));
         }
         else if( facet instanceof DataType ) {
            // Les data sont émises par le publisher et non par le consumer
         }
         else {
            throw new IllegalStateException( "Unexpected class: " + facet.getClass());
         }
      }
      return capacity;
   }

   int getBufferResponseCapacity( Map<String, List<Object>> events ) {
      int capacity = 0;
      for( final List<Object> facets : events.values()) {
         for( final Object facet : facets ) {
            if( facet instanceof RequestType ) {
               final RequestType request  = (RequestType)facet;
               final StructType  response = _structs.get( request.getType());
               capacity = Math.max( capacity, getMessageSize( response.getField()));
            }
         }
      }
      return capacity;
   }

   int getDataBufferOutCapacity( List<DataType> data ) {
      int rawSize = 0;
      for( final DataType d : data ) {
         rawSize = Math.max( rawSize, getMessageSize( d ));
      }
      return rawSize;
   }

   List<InstanceType> getInstancesOf( String deployment, ComponentType component ) {
      final List<InstanceType> instances = new LinkedList<>();
      for( final InstanceType instance : _instancesByName.get( deployment ).values()) {
         if( instance.getComponent() == component ) {
            instances.add( instance );
         }
      }
      return instances;
   }

   StructType getResponse( RequestType request ) {
      return _structs.get( request.getType());
   }

   static Map<InterfaceType, List<RequestType>> getResponses( ComponentType component ) {
      final Map<InterfaceType, List<RequestType>> responsesByIface = new HashMap<>();
      for( final RequiredInterfaceUsageType riut : component.getRequires()) {
         final InterfaceType req = (InterfaceType)riut.getInterface();
         for( final Object facet : req.getEventOrRequestOrData()) {
            if( facet instanceof RequestType ) {
               final RequestType request  = (RequestType)facet;
               List<RequestType> responses = responsesByIface.get( req );
               if( responses == null ) {
                  responsesByIface.put( req, responses = new LinkedList<>());
               }
               responses.add( request );
            }
         }
      }
      return responsesByIface;
   }

   static List<InterfaceType> getRequiredInterfacesBy( ComponentType component ) {
      final List<InterfaceType> interfaces = new LinkedList<>();
      for( final RequiredInterfaceUsageType instance : component.getRequires()) {
         final InterfaceType intrfc = (InterfaceType)instance.getInterface();
         interfaces.add( intrfc );
      }
      return interfaces;
   }

   Map<String, Map<String, RequiresType>> getRequiredInstancesMapOf( String deployment, ComponentType component ) {
      final Map<String, Map<String, RequiresType>> instances = new LinkedHashMap<>();
      for( final InstanceType instance : _instancesByName.get( deployment ).values()) {
         if( instance.getComponent() == component ) {
            if( ! instance.getRequires().isEmpty()) {
               Map<String, RequiresType> requires = instances.get( instance.getName());
               if( requires == null ) {
                  instances.put( instance.getName(), requires = new LinkedHashMap<>());
               }
               for( final RequiresType req : instance.getRequires()) {
                  requires.put(((InterfaceType)req.getInterface()).getName(), req );
               }
            }
         }
      }
      return instances;
   }

   DeploymentType getDeployment( String name ) {
      for( final DeploymentType deployment : _application.getDeployment()) {
         if( deployment.getName().equals( name )) {
            return deployment;
         }
      }
      return null;
   }

   private Set<InstanceType> getReaderOf( String targetDir, InstanceType dataWriter ) {
      final Set<InstanceType> instances = new HashSet<>();
      final DeploymentType deployment = getDeployment( targetDir );
      for( final ProcessType process : deployment.getProcess()) {
         for( final InstanceType dataReader : process.getInstance()) {
            for( final RequiresType requires : dataReader.getRequires()) {
               if( dataWriter.getName().equals( requires.getToInstance())) {
                  instances.add( dataReader );
               }
            }
         }
      }
      return instances;
   }

   Map<String, Set<InstanceType>> getDataWriterOf( String deployment, ComponentType component ) {
      final Map<String, Set<InstanceType>> retVal = new LinkedHashMap<>();
      for( final InstanceType dataWriter : getInstancesOf( deployment, component )) { // udt1, udt2
         final String writerName = dataWriter.getName();
         for( final OfferedInterfaceUsageType offered : component.getOffers()) {
            final InterfaceType offIface = (InterfaceType)offered.getInterface();
            final String ifaceName = offIface.getName();
            for( final Object offFacet : offIface.getEventOrRequestOrData()) {
               if( offFacet instanceof DataType ) {
                  final Set<InstanceType> instances = new HashSet<>();
                  retVal.put( writerName + '/' + ifaceName, instances );
                  for( final InstanceType dataReader : getReaderOf( deployment, dataWriter )) {
                     final String                    readerName   = dataReader.getName();
                     final Map<String, InstanceType> instancesMap = _instancesByName.get( deployment );
                     final InstanceType              instance     = instancesMap.get( readerName );
                     instances.add( instance );
                  }
               }
            }
         }
      }
      return retVal;
   }

   Set<String> getDeployments() {
      return _instancesByName.keySet();
   }

   Map<InstanceType, ProcessType> getProcessByInstance() {
      return _processByInstance;
   }

   Map<String, InstanceType> getInstancesByName( String deployment ) {
      return _instancesByName.get( deployment );
   }

   Map<InterfaceType, List<DataType>> getOfferedDataOf( ComponentType component ) {
      return _offeredData.get( component );
   }

   Map<InterfaceType, List<DataType>> getRequiredDataOf( ComponentType component ) {
      return _requiredData.get( component );
   }

   static Map<String, List<RequestType>> getRequestMap( Map<String, List<Object>> eventOrRequestPerInterface ) {
      final Map<String, List<RequestType>> requestsMap = new LinkedHashMap<>();
      for( final Entry<String, List<Object>> e : eventOrRequestPerInterface.entrySet()) {
         for( final Object facet : e.getValue()) {
            if( facet instanceof RequestType ) {
               final String ifaceName = e.getKey();
               List<RequestType> requests = requestsMap.get( ifaceName );
               if( requests == null ) {
                  requestsMap.put( ifaceName, requests = new LinkedList<>());
               }
               requests.add((RequestType)facet );
            }
         }
      }
      return requestsMap;
   }

   Set<String> getAutomatonActions( ComponentType component ) {
      return _actions.get( component );
   }

   Map<InterfaceType, Map<String, Set<ProcessType>>> getDataConsumer( String deploymentName, ComponentType component ) {
      final DeploymentType deployment = getDeployment( deploymentName );
      final Map<InterfaceType, Map<String, Set<ProcessType>>> processes = new HashMap<>();
      for( final OfferedInterfaceUsageType offers : component.getOffers()) {
         final InterfaceType intrfc = (InterfaceType)offers.getInterface();
         for( final ProcessType process : deployment.getProcess()) {
            for( final InstanceType instance : process.getInstance()) {
               for( final RequiresType req : instance.getRequires()) {
                  if( req.getInterface() == intrfc ) {
                     Map<String, Set<ProcessType>> m = processes.get( intrfc );
                     if( m == null ) {
                        processes.put( intrfc, m = new HashMap<>());
                     }
                     final String instanceName = req.getToInstance();
                     Set<ProcessType> p = m.get( instanceName );
                     if( p == null ) {
                        m.put( instanceName, p = new HashSet<>());
                     }
                     p.add( process );
                  }
               }
            }
         }
      }
      return processes;
   }

   Map<ComponentType, String> getModules( String language ) {
      final Map<ComponentType, String> modules = new HashMap<>();
      for( final ComponentType component : _application.getComponent()) {
         for( final ComponentImplType implementation : component.getImplementation()) {
            if( implementation.getLanguage().equals( language )) {
               modules.put( component, implementation.getModuleName());
               break;
            }
         }
      }
      return modules;
   }

   String getModuleName( String modelModuleName, String language ) {
      for( final TypesType types : _application.getTypes()) {
         if( types.getModuleName().equals( modelModuleName )) {
            for( final TypesImplType impl : types.getImplementation()) {
               if( impl.getLanguage().equals( language )) {
                  return impl.getModuleName();
               }
            }
         }
      }
      throw new IllegalStateException( modelModuleName + " isn't defined for the language " + language );
   }

   void addImports( Map<InterfaceType, List<DataType>> dataMap, SortedSet<String> imports ) {
      if( dataMap != null ) {
         for( final List<DataType> lst : dataMap.values()) {
            for( final DataType data : lst ) {
               final int    lastDot         = data.getType().lastIndexOf( '.' );
               final String modelModuleName = data.getType().substring( 0, lastDot );
               final String typeName        = data.getType().substring( lastDot + 1 );
               final String implModuleName  = getModuleName( modelModuleName, "Java" );
               imports.add( implModuleName + "." + typeName );
            }
         }
      }
   }

   void addImports( SortedSet<String> usedTypes, SortedSet<String> imports ) {
      if( usedTypes != null ) {
         for( final String type : usedTypes ) {
            final int    lastDot         = type.lastIndexOf( '.' );
            final String modelModuleName = type.substring( 0, lastDot );
            final String typeName        = type.substring( lastDot + 1 );
            final String implModuleName  = getModuleName( modelModuleName, "Java" );
            imports.add( implModuleName + "." + typeName );
         }
      }
   }

   Map<String, String> getTypes( String language ) {
      return _typesModel2Impl.get( language );
   }

   public static SortedSet<String> getUserTypesRequiredBy( InterfaceType iface ) {
      final SortedSet<String> types = new TreeSet<>();
      for( final Object facet : iface.getEventOrRequestOrData()) {
         if( facet instanceof EventType ) {
            final EventType event = (EventType)facet;
            for( final FieldType field : event.getField()) {
               if( field.getUserType() != null ) {
                  types.add( field.getUserType());
               }
            }
         }
         else if( facet instanceof RequestType ) {
            final RequestType request = (RequestType)facet;
            for( final FieldType field : request.getArguments().getField()) {
               if( field.getUserType() != null ) {
                  types.add( field.getUserType());
               }
            }
         }
         else if( facet instanceof DataType ) {
            final DataType data = (DataType)facet;
            types.add( data.getType());
         }
      }
      return types;
   }
}
