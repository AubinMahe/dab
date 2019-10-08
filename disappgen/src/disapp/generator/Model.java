package disapp.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import disapp.generator.model.AutomatonType;
import disapp.generator.model.ComponentType;
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
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.RequiresType;
import disapp.generator.model.StateEnumType;
import disapp.generator.model.StructType;

final class Model {

   private static final String RESPONSES_INTERFACE_SUFFIX = "Responses";
   private static final String RESPONSES_STRUCT_SUFFIX    = "Response";

   private final Map<String, InterfaceType>      _interfaces      = new LinkedHashMap<>();
   private final Map<String, InterfaceType>      _responses       = new LinkedHashMap<>();
   private final Map<String, EnumerationType>    _enums           = new LinkedHashMap<>();
   private final Map<String, StructType>         _structs         = new LinkedHashMap<>();
   private final Map<String, SortedSet<String>>  _usedTypes       = new LinkedHashMap<>();
   private final Map<String, List<Object>>       _facetsByName    = new LinkedHashMap<>();
   private final Map<String, InstanceType>       _instancesByName = new LinkedHashMap<>();
   private final Map<String, Map<String, Byte>>  _eventIDs        = new LinkedHashMap<>();
   private final Map<ComponentType, Set<String>> _actions         = new LinkedHashMap<>();
   private final File                            _source;
   private final boolean                         _force;
   private final DisappType                      _application;
   private final long                            _lastModified;

   Model( File source, boolean force ) throws JAXBException {
      final JAXBContext             jaxbContext  = JAXBContext.newInstance( "disapp.generator.model" );
      final Unmarshaller            unmarshaller = jaxbContext.createUnmarshaller();
      @SuppressWarnings("unchecked")
      final JAXBElement<DisappType> elt = (JAXBElement<DisappType>)unmarshaller.unmarshal( source );
      _source      = source;
      _force       = force;
      _application = elt.getValue();
      _lastModified = _source.lastModified();
      final Set<InterfaceType> responseSet = new LinkedHashSet<>();
      for( final InterfaceType iface : _application.getInterface()) {
         final String ifaceName = iface.getName();
         for( final Object facet : iface.getEventOrRequestOrData()) {
            if( facet instanceof RequestType ) {
               final RequestType request       = (RequestType)facet;
               final String      respIfaceName = ifaceName + RESPONSES_INTERFACE_SUFFIX;
               InterfaceType responses = _responses.get( respIfaceName );
               if( responses == null ) {
                  _responses.put( respIfaceName, responses = new InterfaceType() );
                  responses.setName( respIfaceName );
               }
               _interfaces.put( respIfaceName, responses );
               responseSet.add( responses );
               final EventType event = new EventType();
               event.setName( request.getName());
               event.getField().addAll( request.getResponse().getField());
               responses.getEventOrRequestOrData().add( event );
               final StructType struct = new StructType();
               struct.setName( ifaceName + BaseRenderer.cap( request.getName()) + RESPONSES_STRUCT_SUFFIX );
               struct.getField().addAll( request.getResponse().getField());
               _application.getStruct().add( struct );
               for( final ComponentType component : _application.getComponent()) {
                  for( final RequiredInterfaceUsageType ifaceUsage : component.getRequires()) {
                     final InterfaceType ifaceUsed = (InterfaceType)ifaceUsage.getInterface();
                     if( ifaceUsed == iface ) {
                        final OfferedInterfaceUsageType offered = new OfferedInterfaceUsageType();
                        offered.setInterface( responses );
                        component.getOffers().add( offered );
                     }
                  }
                  for( final OfferedInterfaceUsageType ifaceUsage : component.getOffers()) {
                     final InterfaceType ifaceUsed = (InterfaceType)ifaceUsage.getInterface();
                     if( ifaceUsed == iface ) {
                        final RequiredInterfaceUsageType required = new RequiredInterfaceUsageType();
                        required.setInterface( responses );
                        component.getRequires().add( required );
                     }
                  }
               }
            }
         }
      }
      _application.getInterface().addAll( responseSet );
      for( final EnumerationType enm : _application.getEnumeration()) {
         _enums.put( enm.getName(), enm );
      }
      for( final StructType struct : _application.getStruct()) {
         _structs.put( struct.getName(), struct );
      }
      for( final InterfaceType iface : _application.getInterface()) {
         _interfaces.put( iface.getName(), iface );
         _facetsByName.put( iface.getName(), iface.getEventOrRequestOrData());
      }
      for( final InstanceType instance : _application.getDeployment().getInstance()) {
         _instancesByName.put( instance.getName(), instance );
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
            else if( facet instanceof FieldType ) {
               final FieldType data = (FieldType)facet;
               eventIDs.put( data.getName(), ++id );
            }
            else {
               throw new IllegalStateException();
            }
         }
      }
      for( final ComponentType component : _application.getComponent()) {
         final AutomatonType automaton = component.getAutomaton();
         if( automaton != null ) {
            createEventAndStateIfNecessary( automaton );
         }
         _actions.put( component, ( component.getAutomaton() == null ) ? null :
            component.getAutomaton()
               .getOnEntryOrOnExit()
               .stream()
               .map( e -> e.getValue())
               .map( action -> action.getAction())
               .collect( Collectors.toCollection( LinkedHashSet::new )));
      }
   }

   static String getUserType( FieldType field ) {
      final FieldtypeType type = field.getType();
      final String        typeName;
      if( type == FieldtypeType.ENUM ) {
         final Object enumType = field.getUserType();
         if( enumType instanceof StateEnumType ) {
            typeName = ((StateEnumType)enumType).getName();
         }
         else if( enumType instanceof EnumerationType ) {
            typeName = ((EnumerationType)enumType).getName();
         }
         else {
            throw new IllegalStateException();
         }
      }
      else if( type == FieldtypeType.STRUCT ) {
         typeName = ((StructType)field.getUserType()).getName();
      }
      else {
         typeName = null;
      }
      return typeName;
   }

   EnumerationType getEnum( FieldType field ) {
      final FieldtypeType type = field.getType();
      if( type == FieldtypeType.ENUM ) {
         final Object enumType = field.getUserType();
         if( enumType instanceof StateEnumType ) {
            return _enums.get(((StateEnumType)enumType).getName());
         }
         return (EnumerationType)enumType;
      }
      throw new IllegalStateException( field.getName() + " is'nt an enum" );
   }

   private void createEventAndStateIfNecessary( AutomatonType automaton ) {
      final String event = automaton.getEventEnum().getName();
      if( ! _enums.containsKey( event )) {
         final EnumerationType enumeration = new EnumerationType();
         enumeration.setName( event );
         final SortedSet<LiteralType> literals = new TreeSet<>(( l, r ) -> l.getName().compareTo( r.getName()));
         for( final var transition : automaton.getTransition()) {
            final LiteralType literal = new LiteralType();
            literal.setName( transition.getEvent());
            literals.add( literal );
         }
         for( final var shortcut : automaton.getShortcut()) {
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
         _application.getEnumeration().add( enumeration );
         _enums.put( enumeration.getName(), enumeration );
      }
      final String state = automaton.getStateEnum().getName();
      if( ! _enums.containsKey( state )) {
         final EnumerationType enumeration = new EnumerationType();
         enumeration.setName( state );
         final SortedSet<LiteralType> literals = new TreeSet<>(( l, r ) -> l.getName().compareTo( r.getName()));
         for( final var transition : automaton.getTransition()) {
            LiteralType literal = new LiteralType();
            literal.setName( transition.getFrom());
            literals.add( literal );
            literal = new LiteralType();
            literal.setName( transition.getFutur());
            literals.add( literal );
         }
         for( final var shortcut : automaton.getShortcut()) {
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
         _application.getEnumeration().add( enumeration );
         _enums.put( enumeration.getName(), enumeration );
      }
   }

   public Map<String, Map<String, Byte>> getEventIDs() {
      return _eventIDs;
   }

   static List<RequestType> getRequestsOf( InterfaceType iface ) {
      final List<RequestType> requests = new ArrayList<>( iface.getEventOrRequestOrData().size());
      for( final Object element : iface.getEventOrRequestOrData()) {
         if( element instanceof RequestType ) {
            requests.add((RequestType)element );
         }
      }
      return requests;
   }

   private void typesUsedBy( String ifaceName, FieldType field ) {
      final String typeName = getUserType( field );
      if( typeName != null ) {
         SortedSet<String> types = _usedTypes.get( ifaceName );
         if( types == null ) {
            _usedTypes.put( ifaceName, types = new TreeSet<>());
         }
         else if( types.contains( typeName )) {
            return;
         }
         types.add( typeName );
         if( field.getType() == FieldtypeType.STRUCT ) {
            final StructType struct = (StructType)field.getUserType();
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
      for( final Object o : iface.getEventOrRequestOrData()) {
         if( o instanceof EventType ) {
            final EventType event = (EventType)o;
            typesUsedBy( ifaceName, event.getField());
         }
         else if( o instanceof RequestType) {
            final RequestType request = (RequestType)o;
            typesUsedBy( ifaceName, request.getArguments().getField());
            typesUsedBy( ifaceName, request.getResponse().getField());
            SortedSet<String> types = _usedTypes.get( ifaceName );
            if( types == null ) {
               _usedTypes.put( ifaceName, types = new TreeSet<>());
            }
            types.add( ifaceName + BaseRenderer.cap( request.getName()) + RESPONSES_STRUCT_SUFFIX );
         }
         else if( o instanceof FieldType ) {
            final FieldType field = (FieldType)o;
            typesUsedBy( ifaceName, field );
         }
         else {
            throw new IllegalStateException();
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

   int getInterfaceID( String ifaceName ) {
      int ifaceID = 1;
      for( final String name : _interfaces.keySet()) {
         if( name.equals( ifaceName )) {
            return ifaceID;
         }
         ++ifaceID;
      }
      throw new IllegalStateException( ifaceName + " isn't an interface" );
   }

   Map<String, Integer> getInterfaceIDs( List<OfferedInterfaceUsageType> offers ) {
      final Map<String, Integer> ifaces = new LinkedHashMap<>();
      for( final OfferedInterfaceUsageType ifaceUsage : offers ) {
         final InterfaceType iface     = (InterfaceType)ifaceUsage.getInterface();
         final String        ifaceName = iface.getName();
         ifaces.put( ifaceName, getInterfaceID( ifaceName ));
         for( final Object o : iface.getEventOrRequestOrData()) {
            if( o instanceof RequestType ) {
               ifaces.put( ifaceName + RESPONSES_INTERFACE_SUFFIX, getInterfaceID( ifaceName + RESPONSES_INTERFACE_SUFFIX ));
            }
         }
      }
      return ifaces;
   }

   EnumerationType getEnum( String name ) {
      final EnumerationType enm = _enums.get( name );
      if( enm == null ) {
         throw new IllegalStateException( "'" + name + "' is not an enumeration." );
      }
      return enm;
   }

   boolean enumIsDefined( String name ) {
      return _enums.containsKey( name );
   }

   StructType getStruct( String name ) {
      final StructType struct = _structs.get( name );
      if( struct == null ) {
         throw new IllegalStateException( "'" + name + "' is not a struct." );
      }
      return struct;
   }

   boolean structIsDefined( String name ) {
      return _structs.containsKey( name );
   }

   SortedSet<String> getUsedTypesBy( String ifaceName ) {
      return _usedTypes.get( ifaceName );
   }

   public SortedSet<String> getUsedTypesBy( List<OfferedInterfaceUsageType> allOffered ) {
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

   public Map<String, List<Object>> getFacets() {
      return _facetsByName;
   }

   public Map<String, List<Object>> getOfferedEventsOrRequests( ComponentType component ) {
      final Map<String, List<Object>> eventsOrRequestsForOneComponent = new LinkedHashMap<>();
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType iface            = (InterfaceType)offered.getInterface();
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

   public int getFieldSize( FieldType field ) {
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
      case ENUM   : return getEnumSize  (getEnum( field ));
      case STRUCT : return getStructSize((StructType)field.getUserType());
      default     : throw new IllegalStateException();
      }
   }

   protected int getStructSize( StructType struct ) {
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
   public int getMessageSize( List<FieldType> fields ) {
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
   public int getMessageSize( FieldType field ) {
      int msgSize = 1 + 1; // INTERFACE + EVENT
      msgSize += getFieldSize( field );
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
            else if( facet instanceof FieldType) {
               final FieldType data = (FieldType)facet;
               capacity = Math.max( capacity, getMessageSize( data ));
            }
            else {
               throw new IllegalStateException();
            }
         }
      }
      return capacity;
   }

   public int getBufferOutCapacity( RequiredInterfaceUsageType required ) {
      int capacity = 0;
      final InterfaceType iface     = (InterfaceType)required.getInterface();
      final String        ifaceName = iface.getName();
      final List<Object> facets    = _facetsByName.get( ifaceName );
      for( final Object facet : facets ) {
         if( facet instanceof EventType ) {
            final EventType event = (EventType)facet;
            capacity = Math.max( capacity, getMessageSize( event.getField()));
         }
         else if( facet instanceof FieldType ) {
            final FieldType data = (FieldType)facet;
            capacity = Math.max( capacity, getMessageSize( data ));
         }
      }
      return capacity;
   }

   public int getBufferResponseCapacity( Map<String, List<Object>> events ) {
      int capacity = 0;
      for( final List<Object> facets : events.values()) {
         for( final Object facet : facets ) {
            if( facet instanceof RequestType ) {
               final RequestType request = (RequestType)facet;
               capacity = Math.max( capacity, getMessageSize( request.getResponse().getField()));
            }
         }
      }
      return capacity;
   }

   List<InstanceType> getInstancesOf( ComponentType component ) {
      final List<InstanceType> instances = new LinkedList<>();
      for( final InstanceType instance : _application.getDeployment().getInstance()) {
         if( instance.getComponent() == component ) {
            instances.add( instance );
         }
      }
      return instances;
   }

   Map<String, List<RequiresType>> getRequiredInstancesOf( ComponentType component ) {
      final Map<String, List<RequiresType>> instances = new LinkedHashMap<>();
      for( final InstanceType instance : _application.getDeployment().getInstance()) {
         if( instance.getComponent() == component ) {
            final List<RequiresType> requires = instance.getRequires();
            if( ! requires.isEmpty()) {
               instances.put( instance.getName(), requires );
            }
         }
      }
      return instances;
   }

   Map<String, InstanceType> getInstancesByName() {
      return _instancesByName;
   }

   public static Map<String, List<FieldType>> getOfferedDataOf( ComponentType component ) {
      final Map<String, List<FieldType>> allData = new LinkedHashMap<>();
      for( final OfferedInterfaceUsageType offered : component.getOffers()) {
         final InterfaceType iface     = (InterfaceType)offered.getInterface();
         final String        ifaceName = iface.getName();
         for( final Object o : iface.getEventOrRequestOrData()) {
            if( o instanceof FieldType ) {
               List<FieldType> data = allData.get( ifaceName );
               if( data == null ) {
                  allData.put( ifaceName, data = new LinkedList<>());
               }
               data.add((FieldType)o );
            }
         }
      }
      return allData;
   }

   static Map<String, List<RequestType>> getRequestMap( Map<String, List<Object>> eventOrRequestPerInterface ) {
      final Map<String, List<RequestType>> requestsMap = new LinkedHashMap<>();
      for( final Entry<String, List<Object>> e : eventOrRequestPerInterface.entrySet()) {
         for( final Object eventOrRequest : e.getValue()) {
            if( eventOrRequest instanceof RequestType ) {
               final String ifaceName = e.getKey();
               List<RequestType> requests = requestsMap.get( ifaceName );
               if( requests == null ) {
                  requestsMap.put( ifaceName, requests = new LinkedList<>());
               }
               requests.add((RequestType)eventOrRequest );
            }
         }
      }
      return requestsMap;
   }

   public Set<String> getAutomatonActions( ComponentType component ) {
      return _actions.get( component );
   }
}
