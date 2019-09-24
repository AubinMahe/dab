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

public class CppGenerator extends BaseGenerator {

   public CppGenerator( Model model ) {
      super( model );
   }

   @Override
   protected String getSignature( NodeList xFields, String namespace ) {
      String signature = ( xFields.getLength() > 0 ) ? "" : "void";
      for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
         final Element xField = (Element)xFields.item( j );
         final String  xName  = xField.getAttribute( "name" );
         final String  xType  = xField.getAttribute( "type" );
         final String  xUser  = xField.getAttribute( "userTypeName" );
         if( ! signature.isBlank()) {
            signature += ", ";
         }
         switch( xType ) {
         case "struct" : signature += "const " + xUser + " &"; break;
         case "enum"   : signature += xUser;                   break;
         case "string" : signature += "const std::string &";   break;
         case "double" : signature += "const double &";        break;
         case "boolean": signature += "bool";                  break;
         default       : signature += xType;                   break;
         }
         signature += ' ' + xName;
      }
      return signature;
   }

   private void generateRequiredHeaders(
      NodeList xRequires,
      String   genDir,
      String   namespace ) throws IOException
   {
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element xRequired     = (Element)xRequires.item( i );
         final String  interfaceName = xRequired.getAttribute( "interface" );
         final String  nspath        = namespace.replaceAll( "::", "/" );
         final File    target        = new File( genDir, nspath + "/I" + interfaceName + ".hpp" );
         if( ! _model.isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final Map<String, NodeList> iface         = _model.getInterface( interfaceName );
               ps.printf( "#pragma once\n" );
               ps.printf( "\n" );
               ps.printf( "#include <io/DatagramSocket.hpp>\n" );
               ps.printf( "\n" );
               final SortedSet<String> types = _typesUsage.get( interfaceName );
               if( types != null ) {
                  for( final String type : types ) {
                     ps.printf( "#include <%s/%s.hpp>\n", namespace, type );
                     ps.printf( "\n" );
                  }
               }
               ps.printf( "namespace %s {\n", namespace );
               ps.printf( "\n" );
               ps.printf( "   class I%s {\n", interfaceName );
               ps.printf( "   public:\n" );
               ps.printf( "\n" );
               ps.printf( "      I%s( void ) = default;\n", interfaceName );
               ps.printf( "      virtual ~ I%s( void ) = default;\n", interfaceName );
               ps.printf( "\n" );
               ps.printf( "   public:\n" );
               ps.printf( "\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  final String sign = getSignature( xFields, namespace );
                  if( sign.equals( "void" )) {
                     ps.printf( "      virtual void %s( sockaddr_in & target ) = 0;\n", event );
                  }
                  else {
                     ps.printf( "      virtual void %s( sockaddr_in & target, %s ) = 0;\n",
                        event, sign );
                  }
               }
               ps.printf( "\n" );
               ps.printf( "   private:\n" );
               ps.printf( "      I%s( const I%s & ) = delete;\n", interfaceName, interfaceName );
               ps.printf( "      I%s & operator = ( const I%s & ) = delete;\n",
                  interfaceName, interfaceName );
               ps.printf( "   };\n" );
               ps.printf( "\n" );
               ps.printf( "   I%s * new%s( io::DatagramSocket & socket );\n",
                  interfaceName, interfaceName );
               ps.printf( "}\n" );
            }
            System.out.printf( "%s written\n", target.getPath());
         }
      }
   }

   private void generateEnumHeader(
      String  enumName,
      String  genDir,
      String  namespace ) throws IOException
   {
      final File target =
         new File( genDir, namespace.replaceAll( "::", "/" ) + '/' + enumName + ".hpp" );
      if( _model.isUpToDate( target )) {
         return;
      }
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         final String enumNamelc = enumName.toLowerCase();
         ps.printf( "#pragma once\n" );
         ps.printf( "\n" );
         ps.printf( "#include <iostream>\n" );
         ps.printf( "\n" );
         ps.printf( "namespace %s {\n", namespace );
         ps.printf( "\n" );
         ps.printf( "   enum class %s : unsigned char {\n", enumName );
         ps.printf( "      FIRST,\n" );
         ps.printf( "\n" );
         final List<String> literals = _model.getEnum( enumName );
         boolean first = true;
         for( final String literal : literals ) {
            if( first ) {
               ps.printf( "      %s = FIRST,\n", literal );
               first = false;
            }
            else {
               ps.printf( "      %s,\n", literal );
            }
         }
         ps.printf( "\n" );
         ps.printf( "      LAST\n" );
         ps.printf( "   };\n" );
         ps.printf( "}\n" );
         ps.printf( "\n" );
         ps.printf( "std::ostream & operator << ( std::ostream & stream, const %s::%s & %s );\n",
            namespace, enumName, enumNamelc );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   private void generateEnumBody(
      String  enumName,
      String  genDir,
      String  namespace ) throws IOException
   {
      final String nspath = namespace.replaceAll( "::", "/" );
      final File   target = new File( genDir, nspath + '/' + enumName + ".cpp" );
      if( _model.isUpToDate( target )) {
         return;
      }
      try( final PrintStream ps = new PrintStream( target )) {
         final String enumNamelc = enumName.toLowerCase();
         ps.printf( "#include <%s/%s.hpp>\n", nspath, enumName );
         ps.printf( "\n" );
         ps.printf( "using namespace %s;\n", namespace );
         ps.printf( "\n" );
         ps.printf( "std::ostream & operator << ( std::ostream & stream, const %s & %s ) {\n",
            enumName, enumNamelc );
         ps.printf( "   switch( %s ) {\n", enumNamelc );
         final List<String> literals = _model.getEnum( enumName );
         int width = 0;
         for( final String literal : literals ) {
            width = Math.max( literal.length(), width );
         }
         for( final String literal : literals ) {
            ps.printf( "   case %s::%-" + width + "s: return stream << \"%s\"; %sbreak;\n",
               enumName, literal, literal,
               "                                                                                   "
               .substring( 0, width - literal.length()));
         }
         ps.printf( "   case %s::%-" + width + "s: return stream << \"%s\"; %sbreak;\n",
            enumName, "LAST", "LAST (inutilisé)",
            "                                                                                      "
            .substring( 0, width - "LAST (inutilisé)".length()));
         ps.printf( "   }\n" );
         ps.printf( "   return stream << \"inconnu (\" << %s << \")\";\n", enumNamelc );
         ps.printf( "}\n" );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   @Override
   protected void generateEnum( String xUser, String genDir, String packageOrNamespace ) throws IOException {
      generateEnumHeader( xUser, genDir, packageOrNamespace );
      generateEnumBody  ( xUser, genDir, packageOrNamespace );
   }

   private void generateStructHeader(
      String  structName,
      String  genDir,
      String  namespace ) throws IOException
   {
      final File target =
         new File( genDir, namespace.replaceAll( "::", "/" ) + '/' + structName + ".hpp" );
      if( _model.isUpToDate( target )) {
         return;
      }
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "#pragma once\n" );
         ps.printf( "\n" );
         ps.printf( "#include <io/ByteBuffer.hpp>\n" );
         ps.printf( "\n" );
         ps.printf( "namespace %s {\n", namespace );
         ps.printf( "\n" );
         ps.printf( "   struct %s {\n", structName );
         ps.printf( "\n" );
         final Map<String, Element> xFields = _model.getStruct( structName );
         for( final Element xField : xFields.values()) {
            final String xName = xField.getAttribute( "name" );
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "userTypeName" );
            switch( xType ) {
            case "struct" :
            case "enum"   : ps.printf( "      %s %s;\n"     , xUser, xName ); break;
            case "string" : ps.printf( "      std::%s %s;\n", xType, xName ); break;
            case "boolean": ps.printf( "      bool %s;\n"   , xName );        break;
            default       : ps.printf( "      %s %s;\n"     , xType, xName ); break;
            }
         }
         ps.printf( "\n" );
         ps.printf( "      void put( io::ByteBuffer & target ) const;\n" );
         ps.printf( "      void get( io::ByteBuffer & source );\n" );
         ps.printf( "   };\n" );
         ps.printf( "}\n" );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   private void generateStructBody( String structName, String genDir, String namespace ) throws IOException {
      final File target = new File( genDir, namespace.replaceAll( "::", "/" ) + '/' + structName + ".cpp" );
      if( _model.isUpToDate( target )) {
         return;
      }
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "#include <%s/%s.hpp>\n", namespace, structName );
         ps.printf( "\n" );
         ps.printf( "using namespace %s;\n", namespace );
         ps.printf( "\n" );
         ps.printf( "void %s::put( io::ByteBuffer & target ) const {\n", structName );
         final Map<String, Element> xFields = _model.getStruct( structName );
         for( final Element xField : xFields.values()) {
            final String xName = xField.getAttribute( "name" );
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "userTypeName" );
            switch( xType ) {
            case "boolean": ps.printf( "   target.putBool  ( %s );\n", xName ); break;
            case "byte"   : ps.printf( "   target.putByte  ( %s );\n", xName ); break;
            case "short"  : ps.printf( "   target.putShort ( %s );\n", xName ); break;
            case "ushort" : ps.printf( "   target.putUShort( %s );\n", xName ); break;
            case "int"    : ps.printf( "   target.putInt   ( %s );\n", xName ); break;
            case "uint"   : ps.printf( "   target.putUInt  ( %s );\n", xName ); break;
            case "long"   : ps.printf( "   target.putLong  ( %s );\n", xName ); break;
            case "ulong"  : ps.printf( "   target.putULong ( %s );\n", xName ); break;
            case "float"  : ps.printf( "   target.putFloat ( %s );\n", xName ); break;
            case "double" : ps.printf( "   target.putDouble( %s );\n", xName ); break;
            case "string" : ps.printf( "   target.putString( %s );\n", xName ); break;
            case "enum"   :
               if( _model.enumIsDefined( xUser )) {
                  ps.printf( "   target.putByte((byte)%s );\n", xName );
               }
               else {
                  throw new IllegalStateException( xType + " is not an Enum" );
               }
               break;
            case "struct" :
               if( _model.structIsDefined( xUser )) {
                  ps.printf( "   %s.put( target );\n", xName );
               }
               else {
                  throw new IllegalStateException( xType + " is not a Struct" );
               }
               break;
            }
         }
         ps.printf( "}\n" );
         ps.printf( "\n" );
         ps.printf( "void %s::get( io::ByteBuffer & source ) {\n", structName );
         for( final Element xField : xFields.values()) {
            final String xName = xField.getAttribute( "name" );
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "userTypeName" );
            switch( xType ) {
            case "boolean": ps.printf( "   %s = source.getBool  ();\n", xName ); break;
            case "byte"   : ps.printf( "   %s = source.getByte  ();\n", xName ); break;
            case "short"  : ps.printf( "   %s = source.getShort ();\n", xName ); break;
            case "ushort" : ps.printf( "   %s = source.getUShort();\n", xName ); break;
            case "int"    : ps.printf( "   %s = source.getInt   ();\n", xName ); break;
            case "uint"   : ps.printf( "   %s = source.getUInt  ();\n", xName ); break;
            case "long"   : ps.printf( "   %s = source.getLong  ();\n", xName ); break;
            case "ulong"  : ps.printf( "   %s = source.getULong ();\n", xName ); break;
            case "float"  : ps.printf( "   %s = source.getFloat ();\n", xName ); break;
            case "double" : ps.printf( "   %s = source.getDouble();\n", xName ); break;
            case "string" : ps.printf( "   %s = source.getString();\n", xName ); break;
            case "enum"   :
               if( _model.enumIsDefined( xUser )) {
                  ps.printf( "   %s = (%s)source.getByte();\n", xName, xUser );
               }
               else {
                  throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
               }
            break;
            case "struct":
               if( _model.structIsDefined( xUser )) {
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
   protected void generateStruct( String xUser, String genDir, String namespace ) throws IOException {
      generateStructHeader( xUser, genDir, namespace );
      generateStructBody  ( xUser, genDir, namespace );
   }

   private void generateRequiredBodies(
      NodeList xRequires,
      String   genDir,
      String   namespace ) throws IOException
   {
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element xRequired = (Element)xRequires.item( i );
         final String  ifaceName = xRequired.getAttribute( "interface" );
         final String  nspath    = namespace.replaceAll( "::", "/" );
         final File    target    = new File( genDir, nspath + "/net/" + ifaceName + ".cpp" );
         if( ! _model.isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final String                ifaceID = toID( ifaceName ) + "_ID";
               final Map<String, NodeList> iface   = _model.getInterface( ifaceName );
               ps.printf( "#include <%s/I%s.hpp>\n", namespace, ifaceName );
               ps.printf( "\n" );
               ps.printf( "namespace %s::net {\n", namespace );
               ps.printf( "\n" );
               ps.printf( "   class %s : public ::%s::I%s {\n", ifaceName, namespace, ifaceName );
               ps.printf( "   public:\n" );
               ps.printf( "\n" );
               ps.printf( "      %s( io::DatagramSocket & socket ) :\n", ifaceName );
               ps.printf( "         _socket( socket ),\n" );
               ps.printf( "         _out   ( %d )\n", _model.getBufferCapacity( iface.values()));
               ps.printf( "      {}\n" );
               ps.printf( "\n" );
               ps.printf( "   public:\n" );
               ps.printf( "\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  final String sign = getSignature( xFields, namespace );
                  if( sign.equals( "void" )) {
                     ps.printf( "      virtual void %s( sockaddr_in & target ) {\n", event, sign );
                  }
                  else {
                     ps.printf( "      virtual void %s( sockaddr_in & target, %s ) {\n",
                        event, sign );
                  }
                  ps.printf( "         _out.clear();\n" );
                  ps.printf( "         _out.putByte( %s );\n", ifaceID );
                  ps.printf( "         _out.putByte( %s );\n", toID( event ));
                  for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
                     final Element xField = (Element)xFields.item( j );
                     final String xName = xField.getAttribute( "name" );
                     final String xType = xField.getAttribute( "type" );
                     final String xUser = xField.getAttribute( "userTypeName" );
                     switch( xType ) {
                     case "boolean": ps.printf( "         _out.putBool( %s );\n", xName ); break;
                     case "byte"   : ps.printf( "         _out.putByte( %s );\n"   , xName ); break;
                     case "short"  : ps.printf( "         _out.putShort( %s );\n"  , xName ); break;
                     case "ushort" : ps.printf( "         _out.putUShort( %s );\n" , xName ); break;
                     case "int"    : ps.printf( "         _out.putInt( %s );\n"    , xName ); break;
                     case "uint"   : ps.printf( "         _out.putUInt( %s );\n"   , xName ); break;
                     case "long"   : ps.printf( "         _out.putLong( %s );\n"   , xName ); break;
                     case "ulong"  : ps.printf( "         _out.putULong( %s );\n"  , xName ); break;
                     case "float"  : ps.printf( "         _out.putFloat( %s );\n"  , xName ); break;
                     case "double" : ps.printf( "         _out.putDouble( %s );\n" , xName ); break;
                     case "string" : ps.printf( "         _out.putString( %s );\n" , xName ); break;
                     case "enum"   :
                        if( _model.enumIsDefined( xUser )) {
                           ps.printf( "         _out.putByte( static_cast<byte>( %s ));\n", xName );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not an Enum" );
                        }
                        break;
                     case "struct"   :
                        if( _model.structIsDefined( xUser )) {
                           ps.printf( "         %s.put( _out );\n", xName );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not a Struct" );
                        }
                        break;
                     }
                  }
                  ps.printf( "         _out.flip();\n" );
                  ps.printf( "         _socket.sendTo( _out, target );\n" );
                  ps.printf( "      }\n" );
                  ps.printf( "\n" );
               }
               ps.printf( "   private:\n" );
               ps.printf( "\n" );
               ps.printf( "      enum EventID {\n" );
               boolean first = true;
               for( final var e : iface.entrySet()) {
                  final String event = e.getKey();
                  if( first ) {
                     ps.printf( "         %s = 1,\n", toID( event ));
                     first = false;
                  }
                  else {
                     ps.printf( "         %s,\n", toID( event ));
                  }
               }
               ps.printf( "      };\n" );
               ps.printf( "\n" );
               ps.printf( "      enum Interface {\n" );
               ps.printf( "         %s = %d,\n", ifaceID, 1 + _model.getInterfaceRank( ifaceName ));
               ps.printf( "      };\n" );
               ps.printf( "\n" );
               ps.printf( "   private:\n" );
               ps.printf( "\n" );
               ps.printf( "      io::DatagramSocket & _socket;\n" );
               ps.printf( "      sockaddr_in          _target;\n" );
               ps.printf( "      io::ByteBuffer       _out;\n" );
               ps.printf( "   };\n" );
               ps.printf( "}\n" );
               ps.printf( "\n" );
               ps.printf( "namespace %s {\n", namespace );
               ps.printf( "\n" );
               ps.printf( "   I%s * new%s( io::DatagramSocket & socket ) {\n", ifaceName, ifaceName );
               ps.printf( "      return new ::%s::net::%s( socket );\n", namespace, ifaceName );
               ps.printf( "   }\n" );
               ps.printf( "}\n" );
            }
            System.out.printf( "%s written\n", target.getPath());
         }
      }
   }

   private void generateOfferedHeader(
      String   name,
      NodeList xOffers,
      String   genDir,
      String   namespace ) throws IOException
   {
      final String nspath = namespace.replaceAll( "::", "/" );
      final File   target = new File( genDir, nspath + "/I" + name + ".hpp" );
      if( ! _model.isUpToDate( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.printf( "#pragma once\n" );
            ps.printf( "\n" );
            ps.printf( "#include <string>\n" );
            ps.printf( "\n" );
            ps.printf( "#include <types.hpp>\n" );
            ps.printf( "\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element           xOffer      = (Element)xOffers.item( i );
               final String            xIntrfcName = xOffer.getAttribute( "interface" );
               final SortedSet<String> types       = _typesUsage.get( xIntrfcName );
               if( types != null ) {
                  for( final String type : types ) {
                     ps.printf( "#include <%s/%s.hpp>\n", namespace, type );
                  }
               }
            }
            ps.printf( "\n" );
            ps.printf( "namespace %s {\n", namespace );
            ps.printf( "\n" );
            ps.printf( "   class I%s {\n", name );
            ps.printf( "   public:\n" );
            ps.printf( "\n" );
            ps.printf( "      I%s( void ) = default;\n", name );
            ps.printf( "      virtual ~ I%s( void ) = default;\n", name );
            ps.printf( "\n" );
            ps.printf( "   public:\n" );
            ps.printf( "\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element               xOffer      = (Element)xOffers.item( i );
               final String                xIntrfcName = xOffer.getAttribute( "interface" );
               final Map<String, NodeList> iface       = _model.getInterface( xIntrfcName );
               for( final var e : iface.entrySet()) {
                  final String   srvcName = e.getKey();
                  final NodeList xFields  = e.getValue();
                  ps.printf( "      virtual void %s( %s ) = 0;\n",
                     srvcName, getSignature( xFields, namespace ));
               }
            }
            ps.printf( "\n" );
            ps.printf( "   public:\n" );
            ps.printf( "\n" );
            ps.printf( "      virtual void run( void ) = 0;\n" );
            ps.printf( "\n" );
            ps.printf( "   private:\n" );
            ps.printf( "      I%s( const I%s & ) = delete;\n", name, name );
            ps.printf( "      I%s & operator = ( const I%s & ) = delete;\n", name, name );
            ps.printf( "   };\n" );
            ps.printf( "}\n" );
         }
         System.out.printf( "%s written\n", target.getPath());
      }
   }

   private void generateDispatcherHeader(
      String name,
      String genDir,
      String namespace ) throws IOException
   {
      final String nspath = namespace.replaceAll( "::", "/" );
      final File   target = new File( genDir, nspath + "/I" + name + "Dispatcher.hpp" );
      if( ! _model.isUpToDate( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.printf( "#pragma once\n" );
            ps.printf( "\n" );
            ps.printf( "#include <%s/I%s.hpp>\n", namespace, name );
            ps.printf( "\n" );
            ps.printf( "#include <io/ByteBuffer.hpp>\n" );
            ps.printf( "#include <io/DatagramSocket.hpp>\n" );
            ps.printf( "\n" );
            ps.printf( "namespace %s {\n", namespace );
            ps.printf( "\n" );
            ps.printf( "   class I%sDispatcher {\n", name );
            ps.printf( "   public:\n" );
            ps.printf( "\n" );
            ps.printf( "      I%sDispatcher( void ) = default;\n", name );
            ps.printf( "      virtual ~ I%sDispatcher( void ) = default;\n", name );
            ps.printf( "\n" );
            ps.printf( "   public:\n" );
            ps.printf( "\n" );
            ps.printf( "      virtual bool hasDispatched( void ) = 0;\n" );
            ps.printf( "   };\n" );
            ps.printf( "\n" );
            ps.printf( "   I%sDispatcher * new%sDispatcher"
               + "( io::DatagramSocket & socket, I%s & listener );\n", name, name, name );
            ps.printf( "}\n" );
         }
         System.out.printf( "%s written\n", target.getPath());
      }
   }

   private void generateDispatcherBody(
      String   name,
      NodeList xOffers,
      String   genDir,
      String   namespace ) throws IOException
   {
      final String nspath = namespace.replaceAll( "::", "/" );
      final File   target = new File( genDir, nspath + "/net/" + name + "Dispatcher.cpp" );
      if( ! _model.isUpToDate( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            int intrfcMaxWidth = 0;
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               intrfcMaxWidth = Math.max( toID( xIntrfcName ).length(), intrfcMaxWidth );
            }
            ps.printf( "#include <%s/I%sDispatcher.hpp>\n", namespace, name );
            ps.printf( "\n" );
            ps.printf( "#include <iostream>\n" );
            ps.printf( "\n" );
            ps.printf( "namespace %s::net {\n", namespace );
            ps.printf( "\n" );
            ps.printf( "   class %sDispatcher : public ::%s::I%sDispatcher {\n",
               name, namespace, name );
            ps.printf( "   private:\n" );
            ps.printf( "\n" );
            ps.printf( "      enum class Interface : byte {\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               ps.printf( "         %-" + intrfcMaxWidth + "s = %d,\n",
                  toID( xIntrfcName ), 1 + _model.getInterfaceRank( xIntrfcName ));
            }
            ps.printf( "      };\n" );
            ps.printf( "\n" );
            ps.printf( "      friend std::ostream & operator << ( std::ostream & stream, Interface e ) {\n" );
            ps.printf( "         switch( e ) {\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               final String  id          = toID( xIntrfcName );
               ps.printf( "         case Interface::%-" + intrfcMaxWidth +
                  "s: return stream << \"%s\";\n", id, id );
            }
            ps.printf( "         }\n" );
            ps.printf( "         return stream << \"???\";\n" );
            ps.printf( "      }\n" );
            ps.printf( "\n" );
            final List<NodeList> allEvents = new LinkedList<>();
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element               xOffer      = (Element)xOffers.item( i );
               final String                xIntrfcName = xOffer.getAttribute( "interface" );
               final Map<String, NodeList> iface       = _model.getInterface( xIntrfcName );
               allEvents.addAll( iface.values());
               ps.printf( "      enum class %sEvent : byte {\n", xIntrfcName );
               boolean first = true;
               for( final String eventName : iface.keySet()) {
                  if( first ) {
                     ps.printf( "         %s = 1,\n", toID( eventName ));
                     first = false;
                  }
                  else {
                     ps.printf( "         %s,\n", toID( eventName ));
                  }
               }
               ps.printf( "      };\n" );
               ps.printf( "\n" );
               ps.printf( "      bool dispatch( %sEvent event ) {\n", xIntrfcName );
               ps.printf( "         switch( event ) {\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  ps.printf( "         case %sEvent::%s:{\n", xIntrfcName, toID( event ));
                  String signature = "";
                  for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
                     final Element xField = (Element)xFields.item( j );
                     final String  xName  = xField.getAttribute( "name" );
                     final String  xType  = xField.getAttribute( "type" );
                     final String  xUser  = xField.getAttribute( "userTypeName" );
                     switch( xType ) {
                     case "boolean": ps.printf( "            bool %s = _in.getBool();\n"         , xName ); break;
                     case "byte"   : ps.printf( "            byte %s = _in.getByte();\n"         , xName ); break;
                     case "short"  : ps.printf( "            short %s = _in.getShort();\n"       , xName ); break;
                     case "ushort" : ps.printf( "            ushort %s = _in.getUShort();\n"     , xName ); break;
                     case "int"    : ps.printf( "            int %s = _in.getInt();\n"           , xName ); break;
                     case "uint"   : ps.printf( "            uint %s = _in.getUInt();\n"         , xName ); break;
                     case "long"   : ps.printf( "            int64_t %s = _in.getLong();\n"      , xName ); break;
                     case "ulong"  : ps.printf( "            uint64_t %s = _in.getULong();\n"    , xName ); break;
                     case "float"  : ps.printf( "            float %s = _in.getFloat();\n"       , xName ); break;
                     case "double" : ps.printf( "            double %s = _in.getDouble();\n"     , xName ); break;
                     case "string" : ps.printf( "            std::string %s = _in.getString();\n", xName ); break;
                     case "enum"   :
                        if( _model.enumIsDefined( xUser )) {
                           ps.printf( "            %s %s = static_cast<%s>( _in.getByte());\n", xUser, xName, xUser );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not an Enum" );
                        }
                     break;
                     case "struct":
                        if( _model.structIsDefined( xUser )) {
                           ps.printf( "            %s %s;\n", xUser, xName );
                           ps.printf( "            %s.get( _in );\n", xName );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not a Struct" );
                        }
                     break;
                     }
                     if( ! signature.isBlank()) {
                        signature += ", ";
                     }
                     signature += xName;
                  }
                  if( signature.isBlank()) {
                     ps.printf( "            _listener.%s();\n", event );
                  }
                  else {
                     ps.printf( "            _listener.%s( %s );\n", event, signature );
                  }
                  ps.printf( "            break;\n" );
                  ps.printf( "         }\n" );
               }
               ps.printf( "         default:\n" );
               ps.printf( "            std::cerr\n" );
               ps.printf( "               << \"%sDispatcher.dispatch|Message reçu ignoré\"\n", name );
               ps.printf( "               << \", interface = %s\"\n", xIntrfcName );
               ps.printf( "               << \", event = \" << event\n" );
               ps.printf( "               << std::endl;\n" );
               ps.printf( "            return false;\n" );
               ps.printf( "         }\n" );
               ps.printf( "         return true;\n" );
               ps.printf( "      }\n" );
               ps.printf( "\n" );
               ps.printf( "      friend std::ostream & operator << "
                  + "( std::ostream & stream, %sEvent e ) {\n", xIntrfcName );
               ps.printf( "         switch( e ) {\n" );
               int width = 0;
               for( final String eventName : iface.keySet()) {
                  width = Math.max( toID( eventName ).length(), width );
               }
               for( final String eventName : iface.keySet()) {
                  ps.printf( "         case %sEvent::%-" + width + "s: return stream << \"%s\";\n",
                     xIntrfcName, toID( eventName ), toID( eventName ));
               }
               ps.printf( "         }\n" );
               ps.printf( "         return stream << \"???\";\n" );
               ps.printf( "      }\n" );
               ps.printf( "\n" );
            }
            ps.printf( "   public:\n" );
            ps.printf( "\n" );
            ps.printf( "      %sDispatcher( io::DatagramSocket & socket, I%s & listener ) :\n",
               name, name, name );
            ps.printf( "         _socket  ( socket   ),\n" );
            ps.printf( "         _listener( listener ),\n" );
            ps.printf( "         _in      ( %-8d )\n", _model.getBufferCapacity( allEvents ));
            ps.printf( "      {}\n" );
            ps.printf( "\n" );
            ps.printf( "   public:\n" );
            ps.printf( "\n" );
            ps.printf( "      bool hasDispatched( void ) {\n" );
            ps.printf( "         _in.clear();\n" );
            ps.printf( "         if( _socket.receive( _in )) {\n" );
            ps.printf( "            _in.flip();\n" );
            ps.printf( "            Interface interface = static_cast<Interface>( _in.getByte());\n" );
            ps.printf( "            byte      event     = _in.getByte();\n" );
            ps.printf( "            std::cerr\n" );
            ps.printf( "               << \"%sDispatcher.hasDispatched|\"\n", name );
            ps.printf( "               << \"interface = \" << interface\n" );
            ps.printf( "               << \" (\" << (int)interface << ')'\n" );
            ps.printf( "               << \", event: \" << (int)event\n" );
            ps.printf( "               << std::endl;\n" );
            ps.printf( "            switch( interface ) {\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               final String  id          = toID( xIntrfcName );
               ps.printf( "            case Interface::%-" + intrfcMaxWidth +
                  "s: return dispatch( static_cast<%sEvent>( event ));\n", id, xIntrfcName );
            }
            ps.printf( "            }\n" );
            ps.printf( "         }\n" );
            ps.printf( "         return false;\n" );
            ps.printf( "      }\n" );
            ps.printf( "\n" );
            ps.printf( "   private:\n" );
            ps.printf( "\n" );
            ps.printf( "      io::DatagramSocket & _socket;\n" );
            ps.printf( "      I%s & _listener;\n", name );
            ps.printf( "      io::ByteBuffer _in;\n" );
            ps.printf( "   };\n" );
            ps.printf( "}\n" );
            ps.printf( "\n" );
            ps.printf( "namespace %s {\n", namespace );
            ps.printf( "\n" );
            ps.printf(
               "   I%sDispatcher * new%sDispatcher( io::DatagramSocket & socket, I%s & listener ) {\n",
               name, name, name );
            ps.printf( "      return new ::%s::net::%sDispatcher( socket, listener );\n",
               namespace, name );
            ps.printf( "   }\n" );
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
      String   namespace ) throws IOException
   {
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy( xOffers  , generated, genDir, namespace );
      generateTypesUsedBy( xRequires, generated, genDir, namespace );
      generated.clear();

      generateRequiredHeaders( xRequires, genDir, namespace );
      generateRequiredBodies ( xRequires, genDir, namespace );

      generateOfferedHeader( name, xOffers, genDir, namespace );
      generateDispatcherHeader( name, genDir, namespace );
      generateDispatcherBody( name, xOffers, genDir, namespace );
   }
}
