package disapp.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import disapp.generator.model.ComponentType;
import disapp.generator.model.DisappType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.FieldtypeType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.InterfaceUsageType;
import disapp.generator.model.StructType;

final class Model {

   private final Map<String, InterfaceType>     _interfaces     = new LinkedHashMap<>();
   private final Map<String, EnumerationType>   _enums          = new HashMap<>();
   private final Map<String, StructType>        _structs        = new HashMap<>();
   private final Map<String, SortedSet<String>> _usedTypes         = new HashMap<>();
   private final Map<String, List<EventType>>   _eventsPerIface = new HashMap<>();
   private final Map<ComponentType,
      List<InterfaceUsageType>>                 _offers         = new HashMap<>();
   private final Map<ComponentType,
      List<InterfaceUsageType>>                 _requires       = new HashMap<>();
   private final File                           _source;
   private final boolean                        _force;
   private final DisappType                     _application;
   private final long                           _lastModified;

   Model( File source, boolean force ) throws JAXBException {
      final JAXBContext             jaxbContext  = JAXBContext.newInstance( "disapp.generator.model" );
      final Unmarshaller            unmarshaller = jaxbContext.createUnmarshaller();
      @SuppressWarnings("unchecked")
      final JAXBElement<DisappType> elt = (JAXBElement<DisappType>)unmarshaller.unmarshal( source );
      _source      = source;
      _force       = force;
      _application = elt.getValue();
      _lastModified = _source.lastModified();
      for( final InterfaceType intrfc : _application.getInterface()) {
         _interfaces.put( intrfc.getName(), intrfc );
      }
      for( final EnumerationType enm : _application.getEnumeration()) {
         _enums.put( enm.getName(), enm );
      }
      for( final StructType struct : _application.getStruct()) {
         _structs.put( struct.getName(), struct );
      }
      for( final ComponentType component : _application.getComponent()) {
         final List<InterfaceUsageType> offers   = get( "offers"  , component );
         final List<InterfaceUsageType> requires = get( "requires", component );
         _offers  .put( component, offers );
         _requires.put( component, requires );
      }
      for( final ComponentType component : _application.getComponent()) {
         final List<InterfaceUsageType> offers   = getOffersOf  ( component );
         final List<InterfaceUsageType> requires = getRequiresOf( component );
         typesUsedBy( offers   );
         typesUsedBy( requires );
         eventsFor  ( offers   );
         eventsFor  ( requires );
      }
   }

   private static List<InterfaceUsageType> get( String tagName, ComponentType component ) {
      final List<InterfaceUsageType> facets = new ArrayList<>( 3 );
      for( final JAXBElement<InterfaceUsageType> element : component.getOffersOrRequires()) {
         final String candidateTagName = element.getName().getLocalPart();
         if( candidateTagName.equals( tagName )) {
            facets.add( element.getValue());
         }
      }
      return facets;
   }

   private void eventsFor( List<InterfaceUsageType> facets ) {
      for( final InterfaceUsageType facet : facets ) {
         final String        ifaceName = facet.getInterface();
         final InterfaceType iface     = _interfaces.get( ifaceName );
         _eventsPerIface.put( ifaceName, iface.getEvent());
      }
   }

   private void typesUsedBy( List<InterfaceUsageType> facets ) {
      for( final InterfaceUsageType facet : facets ) {
         final String        interfaceName = facet.getInterface();
         final InterfaceType iface         = _interfaces.get( interfaceName );
         for( final EventType event : iface.getEvent()) {
            for( final FieldType field : event.getField()) {
               final FieldtypeType type  = field.getType();
               if(( type == FieldtypeType.ENUM )||( type == FieldtypeType.STRUCT )) {
                  final String typeName = field.getUserTypeName();
                  SortedSet<String> types = _usedTypes.get( interfaceName );
                  if( types == null ) {
                     _usedTypes.put( interfaceName, types = new TreeSet<>());
                  }
                  types.add( typeName );
               }
            }
         }
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

   Map<String, Integer> getInterfaceIDs( List<InterfaceUsageType> usedInterfaces ) {
      final Map<String, Integer> ifaces = new LinkedHashMap<>();
      for( final InterfaceUsageType iface : usedInterfaces ) {
         final String ifaceName = iface.getInterface();
         ifaces.put( ifaceName, getInterfaceID( ifaceName ));
      }
      return ifaces;
   }

   List<InterfaceUsageType> getOffersOf( ComponentType component ) {
      return _offers.get( component );
   }

   List<InterfaceUsageType> getRequiresOf( ComponentType component ) {
      return _requires.get( component );
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

   public SortedSet<String> getUsedTypesBy( List<InterfaceUsageType> allOffered ) {
      final SortedSet<String> usedTypes = new TreeSet<>();
      for( final InterfaceUsageType offered : allOffered ) {
         final String            ifaceName = offered.getInterface();
         final SortedSet<String> used      = _usedTypes.get( ifaceName );
         if( used != null ) {
            usedTypes.addAll( used );
         }
      }
      return usedTypes;
   }

   public List<EventType> getEventsOf( List<InterfaceUsageType> allOffered ) {
      final List<EventType> events = new LinkedList<>();
      for( final InterfaceUsageType offered : allOffered ) {
         final String        ifaceName = offered.getInterface();
         final InterfaceType iface         = _interfaces.get( ifaceName );
         events.addAll( iface.getEvent());
      }
      return events;
   }

   public SortedMap<String, List<EventType>> getEventsMapOf( List<InterfaceUsageType> allOffered ) {
      final SortedMap<String, List<EventType>> events = new TreeMap<>();
      for( final InterfaceUsageType offered : allOffered ) {
         final String        ifaceName = offered.getInterface();
         final InterfaceType iface         = _interfaces.get( ifaceName );
         events.put( ifaceName, iface.getEvent());
      }
      return events;
   }

   int getEnumSize( String enumName ) {
      final EnumerationType enm = _enums.get( enumName );
      if( enm == null ) {
         throw new IllegalStateException( "Undeclared enum named '" + enumName + "'" );
      }
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

   protected int getStructSize( String structName ) {
      final StructType struct  = _structs.get( structName );
      if( struct == null ) {
         throw new IllegalStateException( "Undeclared struct named '" + structName + "'" );
      }
      int msgSize = 0;
      for( final FieldType field : struct.getField()) {
         final FieldtypeType type = field.getType();
         switch( type ) {
         case BOOLEAN  : msgSize += 1; break;
         case BYTE     : msgSize += 1; break;
         case SHORT    : msgSize += 2; break;
         case USHORT   : msgSize += 2; break;
         case INT      : msgSize += 4; break;
         case UINT     : msgSize += 4; break;
         case LONG     : msgSize += 8; break;
         case ULONG    : msgSize += 8; break;
         case FLOAT    : msgSize += 4; break;
         case DOUBLE   : msgSize += 8; break;
         case STRING   : msgSize += 4 + field.getLength().intValue(); break;
         case ENUM     : msgSize += getEnumSize  ( field.getUserTypeName()); break;
         case STRUCT   : msgSize += getStructSize( field.getUserTypeName()); break;
         default       : throw new IllegalStateException();
         }
      }
      return msgSize;
   }

   int getBufferCapacity( Collection<EventType> facets ) {
      int capacity = 0;
      for( final EventType facet : facets ) {
         int msgSize = 1 + 1; // INTERFACE + EVENT
         for( final FieldType field : facet.getField()) {
            final FieldtypeType type = field.getType();
            switch( type ) {
            case BOOLEAN: msgSize += 1; break;
            case BYTE   : msgSize += 1; break;
            case SHORT  : msgSize += 2; break;
            case USHORT : msgSize += 2; break;
            case INT    : msgSize += 4; break;
            case UINT   : msgSize += 4; break;
            case LONG   : msgSize += 8; break;
            case ULONG  : msgSize += 8; break;
            case FLOAT  : msgSize += 4; break;
            case DOUBLE : msgSize += 8; break;
            case STRING : msgSize += 4 + field.getLength().intValue(); break;
            case ENUM   : msgSize += getEnumSize  ( field.getUserTypeName()); break;
            case STRUCT : msgSize += getStructSize( field.getUserTypeName()); break;
            default     : throw new IllegalStateException();
            }
         }
         capacity = Math.max( capacity, msgSize );
      }
      return capacity;
   }

   int getBufferCapacity( String ifaceName ) {
      final InterfaceType iface = _interfaces.get( ifaceName );
      final List<EventType> events = iface.getEvent();
      return getBufferCapacity( events );
   }

   int getBufferCapacity( InterfaceType iface ) {
      final List<EventType> events = iface.getEvent();
      return getBufferCapacity( events );
   }

   int getBufferCapacity( List<InterfaceUsageType> offers ) {
      final List<EventType> allEvents = new LinkedList<>();
      for( final InterfaceUsageType required : offers ) {
         final InterfaceType iface = _interfaces.get( required.getInterface());
         allEvents.addAll( iface.getEvent());
      }
      return getBufferCapacity( allEvents );
   }

   static boolean contains( List<InterfaceUsageType> usedInterfaces, String ifaceName ) {
      for( final InterfaceUsageType facet : usedInterfaces ) {
         if( facet.getInterface().equals( ifaceName )) {
            return true;
         }
      }
      return false;
   }
}
