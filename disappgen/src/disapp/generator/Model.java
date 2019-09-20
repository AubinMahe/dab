package disapp.generator;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

final class Model {

   private final Document                           _doc;
   private final Map<String, List<String>>          _enums      = new LinkedHashMap<>();
   private final Map<String, Map<String, Element>>  _structs    = new LinkedHashMap<>();
   private final Map<String, String>                _enumsType  = new HashMap<>();
   private final Map<String, Map<String, NodeList>> _interfaces = new LinkedHashMap<>();
   private /* */ long                               _lastModified = 0L;
   private final /* */ boolean                            _force;

   Model( File src, boolean force ) throws Exception {
      _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( src );
      _lastModified = src.lastModified();
      _force = force;
      readEnums();
      readStructs();
      readInterfaces();
   }

   void readEnums() {
      final NodeList xEnums = _doc.getElementsByTagName( "enumeration" );
      for( int i = 0, iCount = xEnums.getLength(); i < iCount; ++i ) {
         final Element      xEnum     = (Element)xEnums.item( i );
         final String       xEnumName = xEnum.getAttribute( "name" );
         final String       xEnumType = xEnum.getAttribute( "type" );
         final NodeList     xLiterals = xEnum.getElementsByTagName( "literal" );
         final List<String> literals  = new LinkedList<>();
         _enums.put( xEnumName, literals );
         _enumsType.put( xEnumName, xEnumType.isBlank() ? "byte" : xEnumType );
         for( int j = 0, jCount = xLiterals.getLength(); j < jCount; ++j ) {
            final Element xLiteral     = (Element)xLiterals.item( j );
            final String  xLiteralName = xLiteral.getAttribute( "name" );
            literals.add( xLiteralName );
         }
      }
   }

   void readStructs() {
      final NodeList xStructs = _doc.getElementsByTagName( "struct" );
      for( int i = 0, iCount = xStructs.getLength(); i < iCount; ++i ) {
         final Element  xStruct     = (Element)xStructs.item( i );
         final String   xStructName = xStruct.getAttribute( "name" );
         final NodeList xFields     = xStruct.getElementsByTagName( "field" );
         final Map<String,
            Element>    fields      = new LinkedHashMap<>();
         _structs.put( xStructName, fields );
         for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
            final Element xField     = (Element)xFields.item( j );
            final String  xFieldName = xField.getAttribute( "name" );
            fields.put( xFieldName, xField );
         }
      }
   }

   void readInterfaces() {
      final NodeList xInterfaces = _doc.getElementsByTagName( "interface" );
      for( int i = 0, iCount = xInterfaces.getLength(); i < iCount; ++i ) {
         final Element      xInterface  = (Element)xInterfaces.item( i );
         final String       xIntrfcName = xInterface.getAttribute( "name" );
         final NodeList     xEvents     = xInterface.getElementsByTagName( "event" );
         final Map<String, NodeList> events = new LinkedHashMap<>();
         _interfaces.put( xIntrfcName, events );
         for( int j = 0, jCount = xEvents.getLength(); j < jCount; ++j ) {
            final Element xEvent     = (Element)xEvents.item( j );
            final String  xEventName = xEvent.getAttribute( "name" );
            events.put( xEventName, xEvent.getElementsByTagName( "field" ));
         }
      }
   }

   int getInterfaceRank( String ifaceName ) {
      final var it = _interfaces.keySet().iterator();
      for( int i = 0; i < _interfaces.size(); ++i ) {
         final String ifc = it.next();
         if( ifc.equals( ifaceName )) {
            return i;
         }
      }
      throw new IllegalStateException( "Unknown interface '" + ifaceName + "'" );
   }

   static String getPathOfField( Element xField ) {
      final Element xEvent     = (Element)xField.getParentNode();
      final Element xInterface = (Element)xEvent.getParentNode();
      final String  field      = xField    .getAttribute( "name" );
      final String  event      = xEvent    .getAttribute( "name" );
      final String  intrfc     = xInterface.getAttribute( "name" );
      return "interface[@name=" + intrfc + "]"
         + "/event[@name=" + event + "]"
         + "/field[@name=" + field + "]";
   }

   int getEnumSize( Element xField ) {
      final String scalar = _enumsType.get( xField.getAttribute( "user" ));
      switch( scalar ) {
      case "boolean": return 1;
      case "byte"   : return 1;
      case "short"  : return 2;
      case "ushort" : return 2;
      case "int"    : return 4;
      case "uint"   : return 4;
      case "long"   : return 8;
      case "ulong"  : return 8;
      case "":
         throw new IllegalStateException(
            getPathOfField( xField ) +
            "': 'enum' type must be completed with the type which is be based on" );
      default:
         throw new IllegalStateException(
            xField.getAttribute( "name" ) +
            "': 'enum' type can't be based on '" + scalar + "'" );
      }
   }

   int getStructSize( Collection<Element> fields ) {
      int msgSize = 0;
      for( final Element xField : fields ) {
         final String xType = xField.getAttribute( "type" );
         switch( xType ) {
         case "boolean": msgSize += 1; break;
         case "byte"   : msgSize += 1; break;
         case "short"  : msgSize += 2; break;
         case "ushort" : msgSize += 2; break;
         case "int"    : msgSize += 4; break;
         case "uint"   : msgSize += 4; break;
         case "long"   : msgSize += 8; break;
         case "ulong"  : msgSize += 8; break;
         case "float"  : msgSize += 4; break;
         case "double" : msgSize += 8; break;
         case "string" : msgSize += 4 + getStringSize( xField ); break;
         case "user"   : msgSize += getUserTypeSize( xField ); break;
         }
      }
      return msgSize;
   }

   int getUserTypeSize( Element xField ) {
      final String       name  = xField.getAttribute( "user" );
      final List<String> xEnum = _enums.get( name );
      if( xEnum != null ) {
         return getEnumSize( xField );
      }
      final Map<String, Element> xStruct = _structs.get( name );
      if( xStruct != null ) {
         return getStructSize( xStruct.values());
      }
      return 0;
   }

   static int getStringSize( Element xField ) {
      final String length = xField.getAttribute( "length" );
      if( length.isBlank()) {
         throw new IllegalStateException(
            getPathOfField( xField ) + "': 'string' type must be completed with its length" );
      }
      try {
         return Integer.parseInt( length );
      }
      catch( final Throwable t ) {
         throw new IllegalStateException(
            getPathOfField( xField ) + "': 'string' length is not a number", t );
      }
   }

   int getBufferCapacity( Collection<NodeList> xFieldsList ) {
      int capacity = 0;
      for( final NodeList xFields : xFieldsList ) {
         int msgSize = 1 + 1; // INTERFACE + EVENT
         for( int i = 0, iCount = xFields.getLength(); i < iCount; ++i ) {
            final Element xField = (Element)xFields.item( i );
            final String  xType  = xField.getAttribute( "type" );
            switch( xType ) {
            case "boolean": msgSize += 1; break;
            case "byte"   : msgSize += 1; break;
            case "short"  : msgSize += 2; break;
            case "ushort" : msgSize += 2; break;
            case "int"    : msgSize += 4; break;
            case "uint"   : msgSize += 4; break;
            case "long"   : msgSize += 8; break;
            case "ulong"  : msgSize += 8; break;
            case "float"  : msgSize += 4; break;
            case "double" : msgSize += 8; break;
            case "string" : msgSize += 4 + getStringSize( xField ); break;
            case "user"   : msgSize += getUserTypeSize( xField ); break;
            }
         }
         capacity = Math.max( capacity, msgSize );
      }
      return capacity;
   }

   NodeList getComponents() {
      return _doc.getElementsByTagName( "component" );
   }

   Map<String, NodeList> getInterface( String interfaceName ) {
      return _interfaces.get( interfaceName );
   }

   Map<String, Element> getStruct( String name ) {
      return _structs.get( name );
   }

   List<String> getEnum( String name ) {
      return _enums.get( name );
   }

   boolean enumIsDefined( String name ) {
      return _enums.containsKey( name );
   }

   boolean structIsDefined( String name ) {
      return _structs.containsKey( name );
   }

   protected boolean isUpToDate( File target ) {
      return ( ! _force )&&( target.lastModified() > _lastModified );
   }
}
