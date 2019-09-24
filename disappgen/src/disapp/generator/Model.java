package disapp.generator;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.ST;

import disapp.generator.model.ComponentType;
import disapp.generator.model.DisappType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.FieldtypeType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.InterfaceUsageType;
import disapp.generator.model.StructType;

public final class Model {

   private final Map<String, InterfaceType>     _interfaces     = new LinkedHashMap<>();
   private final Map<String, EnumerationType>   _enums          = new HashMap<>();
   private final Map<String, StructType>        _structs        = new HashMap<>();
   private final Map<String, SortedSet<String>> _usages         = new HashMap<>();
   private final Map<String, List<EventType>>   _eventsPerIface = new HashMap<>();
   private final File                           _source;
   private final boolean                        _force;
   private final DisappType                     _application;
   private final long                           _lastModified;

   public Model( File source, boolean force ) throws JAXBException {
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
         typesUsedBy( component.getOffers());
         typesUsedBy( component.getRequires());
         eventsFor( component.getOffers());
         eventsFor( component.getRequires());
      }
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
                  SortedSet<String> types = _usages.get( interfaceName );
                  if( types == null ) {
                     _usages.put( interfaceName, types = new TreeSet<>());
                  }
                  types.add( typeName );
               }
            }
         }
      }
   }

   DisappType getApplication() {
      return _application;
   }

   InterfaceType getInterface( String name ) {
      return _interfaces.get( name );
   }

   EnumerationType getEnum( String name ) {
      return _enums.get( name );
   }

   StructType getStruct( String name ) {
      return _structs.get( name );
   }

   boolean enumIsDefined( String name ) {
      return _enums.containsKey( name );
   }

   boolean structIsDefined( String name ) {
      return _structs.containsKey( name );
   }

   boolean isUpToDate( File target ) {
      return ( ! _force )&&( target.lastModified() > _lastModified );
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

   protected int getBufferCapacity( Collection<EventType> facets ) {
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

   public int getBufferCapacity( List<InterfaceUsageType> offers ) {
      final List<EventType> allEvents = new LinkedList<>();
      for( final InterfaceUsageType required : offers ) {
         final InterfaceType iface = _interfaces.get( required.getInterface());
         allEvents.addAll( iface.getEvent());
      }
      return getBufferCapacity( allEvents );
   }

   private static void configureRendererWidths( BaseRenderer cr, List<FieldType> fields ) {
      int maxLength    = (Integer)cr.get(    "width" );
      int maxStrLength = (Integer)cr.get( "strWidth" );
      for( final FieldType field : fields ) {
         final String cname = cr.name( field.getName());
         maxLength = Math.max( maxLength, cname.length());
         if( field.getType() == FieldtypeType.STRING ) {
            maxStrLength = Math.max( maxStrLength, cname.length());
         }
      }
      cr.set( "width"   , maxLength    );
      cr.set( "strWidth", maxStrLength );
   }

   private static void configureRendererWidths( BaseRenderer cr, InterfaceType iface ) {
      final List<EventType> events = iface.getEvent();
      for( final EventType event : events ) {
         configureRendererWidths( cr, event.getField());
      }
   }

   protected void fillEnumTemplate( String name, ST tmpl ) {
      final EnumerationType enm = getEnum( name );
      if( enm == null ) {
         throw new IllegalStateException( "'" + name + "' is not an enumeration." );
      }
      tmpl.add( "enum", enm );
   }

   protected void fillStructHeaderTemplate( String name, ST tmpl ) {
      final StructType struct = _structs.get( name );
      if( struct == null ) {
         throw new IllegalStateException( "'" + name + "' is not a struct." );
      }
      tmpl.add( "struct", struct );
   }

   protected void fillStructBodyTemplate( String name, ST tmpl ) {
      fillStructHeaderTemplate( name, tmpl );
      final AttributeRenderer renderer = tmpl.groupThatCreatedThisInstance.getAttributeRenderer( String.class );
      if( renderer instanceof BaseRenderer ) {
         final BaseRenderer  cr     = (BaseRenderer)renderer;
         final StructType struct = _structs.get( name );
         cr.set( "width"   , 0 );
         cr.set( "strWidth", 0 );
         configureRendererWidths( cr, struct.getField());
      }
   }

   protected void fillRequiredHeaderTemplate( String ifaceName, InterfaceUsageType required, ST tmpl ) {
      tmpl.add( "usedTypes", _usages.get( required.getInterface()));
      tmpl.add( "ifaceName", ifaceName );
      tmpl.add( "rawSize"  , getBufferCapacity( _interfaces.get( required.getInterface()).getEvent()));
      tmpl.add( "iface"    , _interfaces.get( required.getInterface()));
   }

   protected void fillRequiredBodyTemplate( String ifaceName, InterfaceUsageType required, ST tmpl ) {
      int ifaceID = 1;
      for( final String name : _interfaces.keySet()) {
         if( name.equals( required.getInterface())) {
            break;
         }
         ++ifaceID;
      }
      final String            interfaceName = required.getInterface();
      final InterfaceType     iface         = _interfaces.get( interfaceName );
      final SortedSet<String> usedTypes     = _usages    .get( interfaceName );
      tmpl.add( "usedTypes", usedTypes );
      tmpl.add( "ifaceName", ifaceName );
      tmpl.add( "rawSize"  , getBufferCapacity( _interfaces.get( required.getInterface()).getEvent()));
      tmpl.add( "iface"    , iface);
      tmpl.add( "ifaceID"  , ifaceID );
      final AttributeRenderer renderer = tmpl.groupThatCreatedThisInstance.getAttributeRenderer( String.class );
      if( renderer instanceof BaseRenderer ) {
         final BaseRenderer cr = (BaseRenderer)renderer;
         cr.set( "width"   , 0 );
         cr.set( "strWidth", 0 );
         configureRendererWidths( cr, iface );
      }
   }

   public void fillOfferedHeader(
      String                   name,
      List<InterfaceUsageType> allOffered,
      ST                       tmpl )
   {
      final SortedSet<String> usedTypes = new TreeSet<>();
      final List<EventType> events = new LinkedList<>();
      for( final InterfaceUsageType offered : allOffered ) {
         final String            interfaceName = offered.getInterface();
         final InterfaceType     iface         = _interfaces.get( interfaceName );
         final SortedSet<String> used          = _usages    .get( interfaceName );
         if( used != null ) {
            usedTypes.addAll( used );
         }
         events.addAll( iface.getEvent());
      }
      tmpl.add( "name"     , name );
      tmpl.add( "usedTypes", usedTypes );
      tmpl.add( "events"   , events );
   }

   @SuppressWarnings("static-method")
   public void fillDispatcherHeader(
      String                   name,
      int                      rawSize,
      ST                       tmpl )
   {
      tmpl.add( "name"   , name );
      tmpl.add( "rawSize", rawSize );
   }

   private static boolean contains( List<InterfaceUsageType> usedInterfaces, String ifaceName ) {
      for( final InterfaceUsageType facet : usedInterfaces ) {
         if( facet.getInterface().equals( ifaceName )) {
            return true;
         }
      }
      return false;
   }

   public void fillDispatcherBody(
      String                   name,
      List<InterfaceUsageType> usedInterfaces,
      int                      rawSize,
      ST                       tmpl )
   {
      int intrfcMaxWidth = 0;
      for( final var iface : usedInterfaces ) {
         intrfcMaxWidth = Math.max( BaseRenderer.toID( iface.getInterface()).length(), intrfcMaxWidth );
      }
      byte rank = 0;
      final Map<String, Byte>            ifaces    = new LinkedHashMap<>();
      final Map<String, List<EventType>> events    = new LinkedHashMap<>();
      final SortedSet<String>            usedTypes = new TreeSet<>();
      for( final var iface : _application.getInterface()) {
         ++rank;
         final String ifaceName = iface.getName();
         if( contains( usedInterfaces, ifaceName )) {
            ifaces.put( ifaceName, rank );
            events.put( ifaceName, _interfaces.get( ifaceName ).getEvent());
            final SortedSet<String> used = _usages.get( ifaceName );
            if( used != null ) {
               usedTypes.addAll( used );
            }
         }
      }
      final AttributeRenderer renderer = tmpl.groupThatCreatedThisInstance.getAttributeRenderer( String.class );
      if( renderer instanceof BaseRenderer ) {
         final BaseRenderer cr = (BaseRenderer)renderer;
         cr.set( "width", intrfcMaxWidth );
      }
      tmpl.add( "name"   , name );
      tmpl.add( "ifaces" , ifaces );
      tmpl.add( "events" , events );
      tmpl.add( "usedTypes", usedTypes );
      tmpl.add( "rawSize", rawSize );
   }
}
