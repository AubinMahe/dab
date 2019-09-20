package disapp.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CGenerator extends BaseGenerator {

   public CGenerator( Model model ) {
      super( model );
   }

   private static String cname( String name ) {
      final int len = name.length();
      final StringBuilder sb = new StringBuilder( 2*len );
      sb.append( Character.toLowerCase( name.charAt( 0 )));
      boolean previousIsUppercase = Character.isUpperCase( name.charAt( 0 ));
      for( int i = 1; i < len; ++i ) {
         if( Character.isUpperCase( name.charAt( i )) && ! previousIsUppercase ) {
            sb.append( '_' );
         }
         sb.append( Character.toLowerCase( name.charAt( i )));
         previousIsUppercase = Character.isUpperCase( name.charAt( i ));
      }
      return sb.toString();
   }

   @Override
   protected String getSignature( NodeList xFields, String pckg ) {
      String signature = ( xFields.getLength() > 0 ) ? "" : "void";
      for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
         final Element xField = (Element)xFields.item( j );
         final String  xName  = xField.getAttribute( "name" );
         final String  xType  = xField.getAttribute( "type" );
         final String  xUser  = xField.getAttribute( "user" );
         if( ! signature.isBlank()) {
            signature += ", ";
         }
         switch( xType ) {
         case "user"   :
            if( _model.structIsDefined( xUser )) {
               signature += "const " + pckg + '_' + cname( xUser ) + " *";
            }
            else {
               signature += pckg + '_' + cname( xUser );
            }
            break;
         case "string" : signature += "const char *"; break;
         case "double" : signature += "double "; break;
         case "boolean": signature += "bool"; break;
         default       : signature += xType; break;
         }
         signature = signature.strip() + ' ' + xName.strip();
      }
      return signature;
   }

   private int generateRequiredHeaders(
      NodeList xRequires,
      String   genDir,
      String   prefix ) throws IOException
   {
      final List<NodeList> allEvents = new LinkedList<>();
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element               xOffer      = (Element)xRequires.item( i );
         final String                xIntrfcName = xOffer.getAttribute( "interface" );
         final Map<String, NodeList> iface       = _model.getInterface( xIntrfcName );
         allEvents.addAll( iface.values());
      }
      final int rawSize = _model.getBufferCapacity( allEvents );
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element xRequired     = (Element)xRequires.item( i );
         final String  interfaceName = xRequired.getAttribute( "interface" );
         final String  func = cname( interfaceName );
         final File    target        = new File( genDir, prefix + "/" + func + ".h" );
         if( ! _model.isUpToDate( target )) {
            final String required = prefix + '_' + func;
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final Map<String, NodeList> iface = _model.getInterface( interfaceName );
               ps.printf( "#pragma once\n" );
               ps.printf( "\n" );
               ps.printf( "#include <io/datagram_socket.h>\n" );
               ps.printf( "\n" );
               final SortedSet<String> types = _typesUsage.get( interfaceName );
               if( types != null ) {
                  for( final String type : types ) {
                     ps.printf( "#include <%s/%s.h>\n", prefix, cname( type ));
                     ps.printf( "\n" );
                  }
               }
               ps.printf( "typedef struct %s_s {\n", required );
               ps.printf( "   SOCKET socket;\n" );
               ps.printf( "   byte raw[%d];\n", rawSize );
               ps.printf( "   io_byte_buffer out;\n" );
               ps.printf( "} %s;\n", required );
               ps.printf( "\n" );
               ps.printf( "void %s_init( %s * This, SOCKET socket );\n", required, required );
               ps.printf( "\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = cname( e.getKey());
                  final NodeList xFields = e.getValue();
                  final String   sign    = getSignature( xFields, prefix );
                  if( sign.equals( "void" )) {
                     ps.printf( "void %s_%s( %s * This, struct sockaddr_in * target );\n",
                        required, event, required );
                  }
                  else {
                     ps.printf( "void %s_%s( %s * This, struct sockaddr_in * target, %s );\n",
                        required, event, required, sign );
                  }
               }
            }
            System.out.printf( "%s written\n", target.getPath());
         }
      }
      return rawSize;
   }

   private void generateRequiredBodies(
      NodeList xRequires,
      String   genDir,
      String   prefix,
      int      rawSize ) throws IOException
   {
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element xRequired = (Element)xRequires.item( i );
         final String  ifaceName = xRequired.getAttribute( "interface" );
         final File    target    = new File( genDir, prefix + "/net/" + cname( ifaceName ) + ".c" );
         if( ! _model.isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final String                ifaceID = toID( ifaceName ) + "_ID";
               final Map<String, NodeList> iface   = _model.getInterface( ifaceName );
               final String                adt     = prefix + '_' + cname( ifaceName );
               ps.printf( "#include <%s/%s.h>\n", prefix, cname( ifaceName ));
               ps.printf( "\n" );
               ps.printf( "enum %s_event_id {\n", prefix );
               boolean first = true;
               for( final var e : iface.entrySet()) {
                  final String event = e.getKey();
                  if( first ) {
                     ps.printf( "   %s = 1,\n", toID( event ));
                     first = false;
                  }
                  else {
                     ps.printf( "   %s,\n", toID( event ));
                  }
               }
               ps.printf( "};\n" );
               ps.printf( "\n" );
               ps.printf( "enum %s_interface {\n", prefix );
               ps.printf( "   %s = %d,\n", ifaceID, 1 + _model.getInterfaceRank( ifaceName ));
               ps.printf( "};\n" );
               ps.printf( "\n" );
               ps.printf( "void %s_init( %s * This, SOCKET socket ) {\n", adt, adt );
               ps.printf( "   This->socket = socket;\n" );
               ps.printf( "   io_byte_buffer_wrap( &This->out, %d, This->raw );\n", rawSize );
               ps.printf( "}\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = cname( e.getKey());
                  final NodeList xFields = e.getValue();
                  final String sign = getSignature( xFields, prefix );
                  ps.printf( "\n" );
                  if( sign.equals( "void" )) {
                     ps.printf( "void %s_%s( %s * This, struct sockaddr_in * target ) {\n", adt, event, adt );
                  }
                  else {
                     ps.printf( "void %s_%s( %s * This, struct sockaddr_in * target, %s ) {\n", adt, event, adt, sign );
                  }
                  ps.printf( "   io_byte_buffer_clear   ( &This->out );\n" );
                  ps.printf( "   io_byte_buffer_put_byte( &This->out, %s );\n", ifaceID );
                  ps.printf( "   io_byte_buffer_put_byte( &This->out, %s );\n", toID( event ));
                  for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
                     final Element xField = (Element)xFields.item( j );
                     final String xName = xField.getAttribute( "name" );
                     final String xType = xField.getAttribute( "type" );
                     final String xUser = xField.getAttribute( "user" );
                     switch( xType ) {
                     case "boolean": ps.printf( "   io_byte_buffer_put_bool"  +"( &This->out, %s );\n", xName ); break;
                     case "byte"   : ps.printf( "   io_byte_buffer_put_byte"  +"( &This->out, %s );\n", xName ); break;
                     case "short"  : ps.printf( "   io_byte_buffer_put_short" +"( &This->out, %s );\n", xName ); break;
                     case "ushort" : ps.printf( "   io_byte_buffer_put_ushort"+"( &This->out, %s );\n", xName ); break;
                     case "int"    : ps.printf( "   io_byte_buffer_put_int"   +"( &This->out, %s );\n", xName ); break;
                     case "uint"   : ps.printf( "   io_byte_buffer_put_uint"  +"( &This->out, %s );\n", xName ); break;
                     case "long"   : ps.printf( "   io_byte_buffer_put_long"  +"( &This->out, %s );\n", xName ); break;
                     case "ulong"  : ps.printf( "   io_byte_buffer_put_ulong" +"( &This->out, %s );\n", xName ); break;
                     case "float"  : ps.printf( "   io_byte_buffer_put_float" +"( &This->out, %s );\n", xName ); break;
                     case "double" : ps.printf( "   io_byte_buffer_put_double"+"( &This->out, %s );\n", xName ); break;
                     case "string" : ps.printf( "   io_byte_buffer_put_string"+"( &This->out, %s );\n", xName ); break;
                     case "user"   :
                        if( _model.enumIsDefined( xUser )) {
                           ps.printf( "   io_byte_buffer_put_byte( &This->out, %s );\n", xName );
                        }
                        else if( _model.structIsDefined( xUser )) {
                           ps.printf( "   %s_put( %s, &This->out );\n", xUser, xName );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
                        }
                        break;
                     }
                  }
                  ps.printf( "   io_byte_buffer_flip( &This->out );\n" );
                  ps.printf( "   io_datagram_socket_sendTo( This->socket, &This->out, target );\n" );
                  ps.printf( "}\n" );
               }
            }
            System.out.printf( "%s written\n", target.getPath());
         }
      }
   }

   private void generateEnumHeader(
      String  enumName,
      String  genDir,
      String  prefix ) throws IOException
   {
      final File target = new File( genDir, prefix + '/' + cname( enumName ) + ".h" );
      if( _model.isUpToDate( target )) {
         return;
      }
      final String adt = prefix + '_' + cname( enumName );
      final String ADT = adt.toUpperCase();
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "#pragma once\n" );
         ps.printf( "\n" );
         ps.printf( "typedef enum %s_e {\n", adt );
         ps.printf( "   %s_FIRST,\n", ADT);
         ps.printf( "\n" );
         final List<String> literals = _model.getEnum( enumName );
         boolean first = true;
         for( final String literal : literals ) {
            if( first ) {
               ps.printf( "   %s_%s = %s_FIRST,\n", ADT, literal.toUpperCase(), ADT );
               first = false;
            }
            else {
               ps.printf( "   %s_%s,\n", ADT, literal.toUpperCase());
            }
         }
         ps.printf( "\n" );
         ps.printf( "   %s_LAST\n", ADT );
         ps.printf( "\n" );
         ps.printf( "} %s;\n", adt );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   @Override
   protected void generateEnum( String xUser, String genDir, String prefix ) throws IOException {
      generateEnumHeader( xUser, genDir, prefix );
   }

   private void generateStructHeader(
      String  structName,
      String  genDir,
      String  prefix ) throws IOException
   {
      final File target = new File( genDir, prefix + '/' + cname( structName ) + ".h" );
      if( _model.isUpToDate( target )) {
         return;
      }
      final String adt = prefix + '_' + cname( structName );
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "#pragma once\n" );
         ps.printf( "\n" );
         ps.printf( "#include <io/byte_buffer.h>\n" );
         ps.printf( "\n" );
         ps.printf( "typedef struct %s_s {\n", adt );
         final Map<String, Element> xFields = _model.getStruct( structName );
         for( final Element xField : xFields.values()) {
            final String xName   = cname( xField.getAttribute( "name" ));
            final String xType   = xField.getAttribute( "type" );
            final String xUser   = xField.getAttribute( "user" );
            final String xLength = xField.getAttribute( "length" );
            switch( xType ) {
            case "user"   : ps.printf( "   %s %s;\n", xUser, xName ); break;
            case "string" : ps.printf( "   char %s[%s];\n", xName, xLength ); break;
            case "boolean": ps.printf( "   bool %s;\n", xName ); break;
            default       : ps.printf( "   %s %s;\n", xType, xName ); break;
            }
         }
         ps.printf( "} %s;\n", adt );
         ps.printf( "\n" );
         ps.printf( "void %s_put( %s * This, io_byte_buffer * target );\n", adt, adt );
         ps.printf( "void %s_get( %s * This, io_byte_buffer * source );\n", adt, adt );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   private void generateStructBody(
      String  structName,
      String  genDir,
      String  prefix ) throws IOException
   {
      final File target = new File( genDir, prefix + '/' + cname( structName ) + ".c" );
      if( _model.isUpToDate( target )) {
         return;
      }
      final String adt = prefix + '_' + cname( structName );
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "#include <%s/%s.h>\n", prefix, cname( structName ));
         ps.printf( "\n" );
         ps.printf( "void %s_put( %s * This, io_byte_buffer * target ) {\n", adt, adt );
         final Map<String, Element> xFields = _model.getStruct( structName );
         for( final Element xField : xFields.values()) {
            final String xName = cname( xField.getAttribute( "name" ));
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "user" );
            switch( xType ) {
            case "boolean": ps.printf( "   io_byte_buffer_put_bool"  +"( target, This->%s );\n", xName ); break;
            case "byte"   : ps.printf( "   io_byte_buffer_put_byte"  +"( target, This->%s );\n", xName ); break;
            case "short"  : ps.printf( "   io_byte_buffer_put_short" +"( target, This->%s );\n", xName ); break;
            case "ushort" : ps.printf( "   io_byte_buffer_put_ushort"+"( target, This->%s );\n", xName ); break;
            case "int"    : ps.printf( "   io_byte_buffer_put_int"   +"( target, This->%s );\n", xName ); break;
            case "uint"   : ps.printf( "   io_byte_buffer_put_uint"  +"( target, This->%s );\n", xName ); break;
            case "long"   : ps.printf( "   io_byte_buffer_put_long"  +"( target, This->%s );\n", xName ); break;
            case "ulong"  : ps.printf( "   io_byte_buffer_put_ulong" +"( target, This->%s );\n", xName ); break;
            case "float"  : ps.printf( "   io_byte_buffer_put_float" +"( target, This->%s );\n", xName ); break;
            case "double" : ps.printf( "   io_byte_buffer_put_double"+"( target, This->%s );\n", xName ); break;
            case "string" : ps.printf( "   io_byte_buffer_put_string"+"( target, This->%s );\n", xName ); break;
            case "user"   :
               if( _model.enumIsDefined( xUser )) {
                  ps.printf( "   putByte( target, (byte)%s );\n", xName );
               }
               if( _model.structIsDefined( xUser )) {
                  ps.printf( "   %s_%s_put( target );\n", prefix, xName );
               }
               else {
                  throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
               }
               break;
            }
         }
         ps.printf( "}\n" );
         ps.printf( "\n" );
         ps.printf( "void %s_get( %s * This, io_byte_buffer * source ) {\n", adt, adt );
         for( final Element xField : xFields.values()) {
            final String xName   = cname( xField.getAttribute( "name" ));
            final String xType   = xField.getAttribute( "type" );
            final String xUser   = xField.getAttribute( "user" );
            final String xLength = xField.getAttribute( "length" );
            switch( xType ) {
            case "boolean": ps.printf( "   io_byte_buffer_get_bool"  +"( source, &This->%s );\n", xName ); break;
            case "byte"   : ps.printf( "   io_byte_buffer_get_byte"  +"( source, &This->%s );\n", xName ); break;
            case "short"  : ps.printf( "   io_byte_buffer_get_short" +"( source, &This->%s );\n", xName ); break;
            case "ushort" : ps.printf( "   io_byte_buffer_get_ushort"+"( source, &This->%s );\n", xName ); break;
            case "int"    : ps.printf( "   io_byte_buffer_get_int"   +"( source, &This->%s );\n", xName ); break;
            case "uint"   : ps.printf( "   io_byte_buffer_get_uint"  +"( source, &This->%s );\n", xName ); break;
            case "long"   : ps.printf( "   io_byte_buffer_get_long"  +"( source, &This->%s );\n", xName ); break;
            case "ulong"  : ps.printf( "   io_byte_buffer_get_ulong" +"( source, &This->%s );\n", xName ); break;
            case "float"  : ps.printf( "   io_byte_buffer_get_float" +"( source, &This->%s );\n", xName ); break;
            case "double" : ps.printf( "   io_byte_buffer_get_double"+"( source, &This->%s );\n", xName ); break;
            case "string" : ps.printf( "   io_byte_buffer_get_string"+"( source, This->%s, %s );\n",
               xName, xLength );
            break;
            case "user"   :
               if( _model.enumIsDefined( xUser )) {
                  ps.printf( "   %s = (%s)io_byte_buffer_get_byte( source );\n", xName, xUser );
               }
               else if( _model.structIsDefined( xUser )) {
                  ps.printf( "   %s.get( source );\n", xName );
               }
               else {
                  throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
               }
            break;
            }
         }
         ps.printf( "}\n" );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   @Override
   protected void generateStruct( String xUser, String genDir, String prefix ) throws IOException {
      generateStructHeader( xUser, genDir, prefix );
      generateStructBody  ( xUser, genDir, prefix );
   }

   private void generateOfferedHeader(
      String   name,
      NodeList xOffers,
      String   genDir,
      String   prefix ) throws IOException
   {
      final File target = new File( genDir, prefix + "/" + cname( name ) + ".h" );
      if( ! _model.isUpToDate( target )) {
         final String adt = prefix + '_' + cname( name );
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.printf( "#pragma once\n" );
            ps.printf( "\n" );
            ps.printf( "#include <string.h>\n" );
            ps.printf( "#include <types.h>\n" );
            ps.printf( "\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element           xOffer      = (Element)xOffers.item( i );
               final String            xIntrfcName = xOffer.getAttribute( "interface" );
               final SortedSet<String> types       = _typesUsage.get( xIntrfcName );
               if( types != null ) {
                  for( final String type : types ) {
                     ps.printf( "#include <%s/%s.h>\n", prefix, cname( type ));
                     ps.printf( "\n" );
                  }
               }
            }
            ps.printf( "struct %s_s;\n", adt );
            ps.printf( "typedef struct %s_s %s;\n", adt, adt );
            ps.printf( "\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element               xOffer      = (Element)xOffers.item( i );
               final String                xIntrfcName = xOffer.getAttribute( "interface" );
               final Map<String, NodeList> iface       = _model.getInterface( xIntrfcName );
               for( final var e : iface.entrySet()) {
                  final String   srvcName = cname( e.getKey());
                  final NodeList xFields  = e.getValue();
                  final String sign = getSignature( xFields, prefix ).strip();
                  if( sign.equals( "void" )) {
                     ps.printf( "void %s_%s( struct %s_s * This );\n", adt, srvcName, adt );
                  }
                  else {
                     ps.printf( "void %s_%s( struct %s_s * This, %s );\n", adt, srvcName, adt, sign );
                  }
               }
            }
         }
         System.out.printf( "%s written\n", target.getPath());
      }
   }

   private int generateDispatcherHeader(
      String   name,
      NodeList xOffers,
      String   genDir,
      String   prefix ) throws IOException
   {
      final List<NodeList> allEvents = new LinkedList<>();
      for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
         final Element               xOffer      = (Element)xOffers.item( i );
         final String                xIntrfcName = xOffer.getAttribute( "interface" );
         final Map<String, NodeList> iface       = _model.getInterface( xIntrfcName );
         allEvents.addAll( iface.values());
      }
      final int rawSize = _model.getBufferCapacity( allEvents );
      final File target = new File( genDir, prefix + "/" + cname( name ) + "_dispatcher.h" );
      if( ! _model.isUpToDate( target )) {
         final String adt = prefix + '_' + cname( name ) + "_dispatcher";
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.printf( "#pragma once\n" );
            ps.printf( "\n" );
            ps.printf( "#include <%s/%s.h>\n", prefix, cname( name ));
            ps.printf( "\n" );
            ps.printf( "#include <io/byte_buffer.h>\n" );
            ps.printf( "#include <io/datagram_socket.h>\n" );
            ps.printf( "\n" );
            ps.printf( "typedef struct %s_s {\n", adt );
            ps.printf( "   SOCKET socket;\n" );
            ps.printf( "   %s_%s * listener;\n", prefix, cname( name ));
            ps.printf( "   byte raw[%d];\n", rawSize );
            ps.printf( "   io_byte_buffer in;\n" );
            ps.printf( "} %s;\n", adt );
            ps.printf( "\n" );
            ps.printf( "void %s_init( %s * This, SOCKET socket, %s_%s * listener );\n",
               adt, adt, prefix, cname( name ) );
            ps.printf( "bool %s_has_dispatched( %s * This );\n", adt, adt );
         }
         System.out.printf( "%s written\n", target.getPath());
      }
      return rawSize;
   }

   private void generateDispatcherBody(
      String   name,
      NodeList xOffers,
      String   genDir,
      String   prefix,
      int      rawSize ) throws IOException
   {
      final File target = new File( genDir, prefix + "/net/" + cname( name ) + "_dispatcher.c" );
      if( ! _model.isUpToDate( target )) {
         final String adt = prefix + '_' + cname( name ) + "_dispatcher";
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            int intrfcMaxWidth = 0;
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               intrfcMaxWidth = Math.max( toID( xIntrfcName ).length(), intrfcMaxWidth );
            }
            ps.printf( "#include <%s/%s_dispatcher.h>\n", prefix, cname( name ));
            ps.printf( "#include <stdio.h>\n" );
            ps.printf( "\n" );
            ps.printf( "typedef enum %s_interface_e {\n", adt );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               ps.printf( "   %-" + intrfcMaxWidth + "s = %d,\n",
                  toID( xIntrfcName ), 1 + _model.getInterfaceRank( xIntrfcName ));
            }
            ps.printf( "} %s_interface;\n", adt );
            ps.printf( "\n" );
            final List<NodeList> allEvents = new LinkedList<>();
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element               xOffer      = (Element)xOffers.item( i );
               final String                xIntrfcName = xOffer.getAttribute( "interface" );
               final Map<String, NodeList> iface       = _model.getInterface( xIntrfcName );
               allEvents.addAll( iface.values());
               final String eventEnum = adt + '_' + cname( xIntrfcName ) + "_event";
               ps.printf( "typedef enum %s_e {\n", eventEnum );
               boolean first = true;
               final String eprfx = cname( xIntrfcName ).toUpperCase();
               for( final String eventName : iface.keySet()) {
                  if( first ) {
                     ps.printf( "   %s_%s = 1,\n", eprfx, toID( eventName ));
                     first = false;
                  }
                  else {
                     ps.printf( "   %s_%s,\n", eprfx, toID( eventName ));
                  }
               }
               ps.printf( "} %s;\n", eventEnum );
               ps.printf( "\n" );
               ps.printf( "static bool %s_%s_dispatch(\n", prefix, cname( xIntrfcName ) );
               ps.printf( "   %s_%s_dispatcher * This,\n", prefix, cname( name ));
               ps.printf( "   %s event )\n", eventEnum );
               ps.printf( "{\n" );
               ps.printf( "   switch( event ) {\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  ps.printf( "   case %s_%s:{\n", eprfx, toID( event ));
                  String signature = "";
                  for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
                     final Element xField  = (Element)xFields.item( j );
                     final String  xName   = xField.getAttribute( "name" );
                     final String  xType   = xField.getAttribute( "type" );
                     final String  xUser   = xField.getAttribute( "user" );
                     final String  xLength = xField.getAttribute( "length" );
                     if( ! xType.equals( "user" )) {
                        switch( xType ) {
                        case "boolean": ps.printf( "      bool %s;\n", xName ); break;
                        case "string" : ps.printf( "      char %s[%s];\n", xName, xLength ); break;
                        default       : ps.printf( "      %s %s;\n", xType, xName ); break;
                        }
                     }
                     switch( xType ) {
                     case "boolean": ps.printf( "      io_byte_buffer_get_bool"  +"( &This->in, &%s );\n", xName ); break;
                     case "byte"   : ps.printf( "      io_byte_buffer_get_byte"  +"( &This->in, &%s );\n", xName ); break;
                     case "short"  : ps.printf( "      io_byte_buffer_get_short" +"( &This->in, &%s );\n", xName ); break;
                     case "ushort" : ps.printf( "      io_byte_buffer_get_ushort"+"( &This->in, &%s );\n", xName ); break;
                     case "int"    : ps.printf( "      io_byte_buffer_get_int"   +"( &This->in, &%s );\n", xName ); break;
                     case "uint"   : ps.printf( "      io_byte_buffer_get_uint"  +"( &This->in, &%s );\n", xName ); break;
                     case "long"   : ps.printf( "      io_byte_buffer_get_long"  +"( &This->in, &%s );\n", xName ); break;
                     case "ulong"  : ps.printf( "      io_byte_buffer_get_ulong" +"( &This->in, &%s );\n", xName ); break;
                     case "float"  : ps.printf( "      io_byte_buffer_get_float" +"( &This->in, &%s );\n", xName ); break;
                     case "double" : ps.printf( "      io_byte_buffer_get_double"+"( &This->in, &%s );\n", xName ); break;
                     case "string" : ps.printf( "      io_byte_buffer_get_string"+"( &This->in, %s, %s );\n", xName, xLength ); break;
                     case "user"   :
                        if( _model.enumIsDefined( xUser )) {
                           ps.printf( "      %s_%s %s;\n", prefix, cname( xUser ), xName );
                           ps.printf( "      io_byte_buffer_get_byte( &This->in, &%s );\n", xName );
                        }
                        else if( _model.structIsDefined( xUser )) {
                           ps.printf( "      %s_%s %s;\n", prefix, cname( xUser ), xName );
                           ps.printf( "      %s_%s_get( &%s, &This->in );\n", prefix, cname( xUser ), xName );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
                        }
                     break;
                     }
                     if( ! signature.isBlank()) {
                        signature += ", ";
                     }
                     if( _model.structIsDefined( xUser )) {
                        signature += '&' + xName;
                     }
                     else {
                        signature += xName;
                     }
                  }
                  final String function = prefix + '_' + cname( name +'_' + event );
                  if( signature.isBlank()) {
                     ps.printf( "      %s( This->listener );\n", function );
                  }
                  else {
                     ps.printf( "      %s( This->listener, %s );\n", function, signature );
                  }
                  ps.printf( "   break;\n" );
                  ps.printf( "   }\n" );
               }
               ps.printf( "   default:\n" );
               ps.printf( "      fprintf( stderr, \"%s|Message reçu ignoré\\n\" );\n", adt );
               ps.printf( "      fprintf( stderr, \"\\tinterface = %s\\n\" );\n", xIntrfcName );
               ps.printf( "      fprintf( stderr, \"\\tevent     = %%d\\n\", event );\n" );
               ps.printf( "      return false;\n" );
               ps.printf( "   }\n" );
               ps.printf( "   return true;\n" );
               ps.printf( "}\n" );
               ps.printf( "\n" );
            }
            ps.printf( "void %s_init( %s * This, SOCKET socket, %s_%s * listener ) {\n",
               adt, adt, prefix, cname( name ) );
            ps.printf( "   This->socket   = socket;\n" );
            ps.printf( "   This->listener = listener;\n" );
            ps.printf( "   io_byte_buffer_wrap( &This->in, %d, This->raw );\n", rawSize );
            ps.printf( "}\n" );
            ps.printf( "\n" );
            ps.printf( "bool %s_has_dispatched( %s * This ) {\n", adt, adt );
            ps.printf( "   struct sockaddr_in from;\n" );
            ps.printf( "   io_byte_buffer_clear( &This->in );\n" );
            ps.printf( "   util_error status = io_datagram_socket_receive( This->socket, &This->in, &from );\n" );
            ps.printf( "   if( UTIL_NO_ERROR == status ) {\n" );
            ps.printf( "      io_byte_buffer_flip( &This->in );\n" );
            ps.printf( "      byte interface, event;\n" );
            ps.printf( "      io_byte_buffer_get_byte( &This->in, &interface );\n" );
            ps.printf( "      io_byte_buffer_get_byte( &This->in, &event );\n" );
            ps.printf( "      switch( interface ) {\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               final String  id          = toID( xIntrfcName );
               ps.printf( "      case %-" + intrfcMaxWidth +
                  "s: return %s_%s_dispatch( This, event );\n", id, prefix, cname( xIntrfcName ));
            }
            ps.printf( "      }\n" );
            ps.printf( "   }\n" );
            ps.printf( "   return false;\n" );
            ps.printf( "}\n" );
         }
         System.out.printf( "%s written\n", target.getPath());
      }
   }

   @Override
   protected void generateComponent(
      String   name,
      NodeList xOffers,
      NodeList xRequires,
      String   genDir,
      String   prefix ) throws IOException
   {
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy( xOffers  , generated, genDir, prefix );
      generateTypesUsedBy( xRequires, generated, genDir, prefix );
      generated.clear();

      int rawSize = generateRequiredHeaders( xRequires, genDir, prefix );
      generateRequiredBodies( xRequires, genDir, prefix, rawSize );

      generateOfferedHeader( name, xOffers, genDir, prefix );
      rawSize = generateDispatcherHeader( name, xOffers, genDir, prefix );
      generateDispatcherBody( name, xOffers, genDir, prefix, rawSize );
   }
}
