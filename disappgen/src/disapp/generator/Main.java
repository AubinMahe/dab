package disapp.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Main {

   private static final Map<String, List<String>>          _enums      = new LinkedHashMap<>();
   private static final Map<String, Map<String, Element>>  _structs    = new LinkedHashMap<>();
   private static final Map<String, String>                _enumsType  = new HashMap<>();
   private static final Map<String, Map<String, NodeList>> _interfaces = new LinkedHashMap<>();
   private static final Map<String, SortedSet<String>>     _typesUsage = new HashMap<>();

   private static long _modelLastModified = 0L;

   private static void readEnums( Document doc ) {
      final NodeList xEnums = doc.getElementsByTagName( "enumeration" );
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

   private static void readStructs( Document doc ) {
      final NodeList xStructs = doc.getElementsByTagName( "struct" );
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

   private static void readInterfaces( Document doc ) {
      final NodeList xInterfaces = doc.getElementsByTagName( "interface" );
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

   private static boolean isUpToDate( File target ) {
      return target.lastModified() > _modelLastModified;
   }

   private static void generateJavaEnum(
      String  enumName,
      String  genDir,
      String  pckg    ) throws IOException
   {
      final File target =
         new File( genDir, pckg.replaceAll( "\\.", "/" ) + '/' + enumName + ".java" );
      if( isUpToDate( target )) {
         return;
      }
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "package %s;\n", pckg );
         ps.printf( "\n" );
         ps.printf( "public enum %s {\n", enumName );
         ps.printf( "\n" );
         final List<String> literals = _enums.get( enumName );
         for( final String literal : literals ) {
            ps.printf( "   %s,\n", literal );
         }
         ps.printf( "}\n" );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   private static void generateJavaStruct(
      String  structName,
      String  genDir,
      String  pckg    ) throws IOException
   {
      final File target =
         new File( genDir, pckg.replaceAll( "\\.", "/" ) + '/' + structName + ".java" );
      if( isUpToDate( target )) {
         return;
      }
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "package %s;\n", pckg );
         ps.printf( "\n" );
         ps.printf( "import java.nio.ByteBuffer;\n" );
         ps.printf( "\n" );
         ps.printf( "import io.ByteBufferHelper;\n" );
         ps.printf( "\n" );
         ps.printf( "public class %s {\n", structName );
         ps.printf( "\n" );
         final Map<String, Element> xFields = _structs.get( structName );
         for( final Element xField : xFields.values()) {
            final String xName = xField.getAttribute( "name" );
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "user" );
            switch( xType ) {
            case "boolean": ps.printf( "   public boolean %s;\n", xName ); break;
            case "byte"   : ps.printf( "   public byte    %s;\n", xName ); break;
            case "short"  : ps.printf( "   public short   %s;\n", xName ); break;
            case "ushort" : ps.printf( "   public short   %s;\n", xName ); break;
            case "int"    : ps.printf( "   public int     %s;\n", xName ); break;
            case "uint"   : ps.printf( "   public int     %s;\n", xName ); break;
            case "long"   : ps.printf( "   public long    %s;\n", xName ); break;
            case "ulong"  : ps.printf( "   public long    %s;\n", xName ); break;
            case "float"  : ps.printf( "   public float   %s;\n", xName ); break;
            case "double" : ps.printf( "   public double  %s;\n", xName ); break;
            case "string" : ps.printf( "   public String  %s;\n", xName ); break;
            case "user"   :
               if( _enums.containsKey( xUser )) {
                  ps.printf( "   %s _%s;\n", xUser, xName );
               }
               else if( _structs.containsKey( xUser )) {
                  ps.printf( "   %s _%s = new %s();\n", xUser, xName, xUser );
               }
               else {
                  throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
               }
               break;
            }
         }
         ps.printf( "\n" );
         ps.printf( "   public void put( ByteBuffer target ) {\n" );
         for( final Element xField : xFields.values()) {
            final String xName = xField.getAttribute( "name" );
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "user" );
            switch( xType ) {
            case "boolean":
               ps.printf( "      ByteBufferHelper.putBoolean( target, %s );\n", xName );
               break;
            case "byte"   : ps.printf( "      target.put( %s );\n"      , xName ); break;
            case "short"  : ps.printf( "      target.putShort( %s );\n" , xName ); break;
            case "ushort" : ps.printf( "      target.putShort( %s );\n" , xName ); break;
            case "int"    : ps.printf( "      target.putInt( %s );\n"   , xName ); break;
            case "uint"   : ps.printf( "      target.putInt( %s );\n"   , xName ); break;
            case "long"   : ps.printf( "      target.putLong( %s );\n"  , xName ); break;
            case "ulong"  : ps.printf( "      target.putLong( %s );\n"  , xName ); break;
            case "float"  : ps.printf( "      target.putFloat( %s );\n" , xName ); break;
            case "double" : ps.printf( "      target.putDouble( %s );\n", xName ); break;
            case "string" :
               ps.printf( "      ByteBufferHelper.putString( target, %s );\n" , xName );
               break;
            case "user"   :
               if( _enums.containsKey( xUser )) {
                  ps.printf( "      target.put((byte)%s.ordinal());\n", xName );
               }
               else if( _structs.containsKey( xUser )) {
                  ps.printf( "      %s.put( target );\n", xName );
               }
               else {
                  throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
               }
               break;
            }
         }
         ps.printf( "   }\n" );
         ps.printf( "\n" );
         ps.printf( "   public void get( ByteBuffer source ) {\n" );
         for( final Element xField : xFields.values()) {
            final String xName = xField.getAttribute( "name" );
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "user" );
            switch( xType ) {
            case "boolean":
               ps.printf( "      %s = ByteBufferHelper.getBoolean( source );\n", xName );
               break;
            case "byte"   : ps.printf( "      %s = source.get();\n"      , xName ); break;
            case "short"  : ps.printf( "      %s = source.getShort();\n" , xName ); break;
            case "ushort" : ps.printf( "      %s = source.getShort();\n" , xName ); break;
            case "int"    : ps.printf( "      %s = source.getInt();\n"   , xName ); break;
            case "uint"   : ps.printf( "      %s = source.getInt();\n"   , xName ); break;
            case "long"   : ps.printf( "      %s = source.getLong();\n"  , xName ); break;
            case "ulong"  : ps.printf( "      %s = source.getLong();\n"  , xName ); break;
            case "float"  : ps.printf( "      %s = source.getFloat();\n" , xName ); break;
            case "double" : ps.printf( "      %s = source.getDouble();\n", xName ); break;
            case "string" :
               ps.printf( "      %s = ByteBufferHelper.getString( source );\n", xName );
               break;
            case "user"   :
               if( _enums.containsKey( xUser )) {
                  ps.printf( "      %s = %s.values()[source.get()];\n", xUser, xName, xUser );
               }
               else if( _structs.containsKey( xUser )) {
                  ps.printf( "      %s.get( source );\n", xName );
               }
               else {
                  throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
               }
            break;
            }
         }
         ps.printf( "   }\n" );
         ps.printf( "\n" );
         ps.printf( "}\n" );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   private static void generateCppEnumHeader(
      String  enumName,
      String  genDir,
      String  namespace ) throws IOException
   {
      final File target =
         new File( genDir, namespace.replaceAll( "::", "/" ) + '/' + enumName + ".hpp" );
      if( isUpToDate( target )) {
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
         final List<String> literals = _enums.get( enumName );
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

   private static void generateCppEnumBody(
      String  enumName,
      String  genDir,
      String  namespace ) throws IOException
   {
      final String nspath = namespace.replaceAll( "::", "/" );
      final File   target = new File( genDir, nspath + '/' + enumName + ".cpp" );
      if( isUpToDate( target )) {
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
         final List<String> literals = _enums.get( enumName );
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

   private static void generateCppStructHeader(
      String  structName,
      String  genDir,
      String  namespace ) throws IOException
   {
      final File target =
         new File( genDir, namespace.replaceAll( "::", "/" ) + '/' + structName + ".hpp" );
      if( isUpToDate( target )) {
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
         final Map<String, Element> xFields = _structs.get( structName );
         for( final Element xField : xFields.values()) {
            final String xName = xField.getAttribute( "name" );
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "user" );
            switch( xType ) {
            case "user"   : ps.printf( "      %s %s;\n"     , xUser, xName ); break;
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

   private static void generateCppStructBody(
      String  structName,
      String  genDir,
      String  namespace ) throws IOException
   {
      final File target =
         new File( genDir, namespace.replaceAll( "::", "/" ) + '/' + structName + ".cpp" );
      if( isUpToDate( target )) {
         return;
      }
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "#include <%s/%s.hpp>\n", namespace, structName );
         ps.printf( "\n" );
         ps.printf( "using namespace %s;\n", namespace );
         ps.printf( "\n" );
         ps.printf( "void %s::put( io::ByteBuffer & target ) const {\n", structName );
         final Map<String, Element> xFields = _structs.get( structName );
         for( final Element xField : xFields.values()) {
            final String xName = xField.getAttribute( "name" );
            final String xType = xField.getAttribute( "type" );
            final String xUser = xField.getAttribute( "user" );
            switch( xType ) {
            case "boolean": ps.printf( "   target.putBoolean( %s );\n", xName ); break;
            case "byte"   : ps.printf( "   target.putByte( %s );\n"   , xName ); break;
            case "short"  : ps.printf( "   target.putShort( %s );\n"  , xName ); break;
            case "ushort" : ps.printf( "   target.putShort( %s );\n"  , xName ); break;
            case "int"    : ps.printf( "   target.putInt( %s );\n"    , xName ); break;
            case "uint"   : ps.printf( "   target.putInt( %s );\n"    , xName ); break;
            case "long"   : ps.printf( "   target.putLong( %s );\n"   , xName ); break;
            case "ulong"  : ps.printf( "   target.putLong( %s );\n"   , xName ); break;
            case "float"  : ps.printf( "   target.putFloat( %s );\n"  , xName ); break;
            case "double" : ps.printf( "   target.putDouble( %s );\n" , xName ); break;
            case "string" : ps.printf( "   target.putString( %s );\n" , xName ); break;
            case "user"   :
               if( _enums.containsKey( xUser )) {
                  ps.printf( "   target.putByte((byte)%s );\n", xName );
               }
               else if( _structs.containsKey( xUser )) {
                  ps.printf( "   %s.put( target );\n", xName );
               }
               else {
                  throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
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
            final String xUser = xField.getAttribute( "user" );
            switch( xType ) {
            case "boolean": ps.printf( "   %s = source.getBoolean();\n", xName ); break;
            case "byte"   : ps.printf( "   %s = source.getByte();\n"   , xName ); break;
            case "short"  : ps.printf( "   %s = source.getShort();\n"  , xName ); break;
            case "ushort" : ps.printf( "   %s = source.getShort();\n"  , xName ); break;
            case "int"    : ps.printf( "   %s = source.getInt();\n"    , xName ); break;
            case "uint"   : ps.printf( "   %s = source.getInt();\n"    , xName ); break;
            case "long"   : ps.printf( "   %s = source.getLong();\n"   , xName ); break;
            case "ulong"  : ps.printf( "   %s = source.getLong();\n"   , xName ); break;
            case "float"  : ps.printf( "   %s = source.getFloat();\n"  , xName ); break;
            case "double" : ps.printf( "   %s = source.getDouble();\n" , xName ); break;
            case "string" : ps.printf( "   %s = source.getString();\n" , xName ); break;
            case "user"   :
               if( _enums.containsKey( xUser )) {
                  ps.printf( "   %s = (%s)source.getByte();\n", xName, xUser );
               }
               else if( _structs.containsKey( xUser )) {
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

   private static void generateTypesUsedBy(
      NodeList     facets,
      List<String> generated,
      String       genDir,
      String       packageOrNamespace,
      boolean      javaLanguage        ) throws IOException
   {
      for( int i = 0, iCount = facets.getLength(); i < iCount; ++i ) {
         final Element               facet         = (Element)facets.item( i );
         final String                interfaceName = facet.getAttribute( "interface" );
         final Map<String, NodeList> iface         = _interfaces.get( interfaceName );
         for( final var xFields : iface.values()) {
            for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
               final Element xField = (Element)xFields.item( j );
               final String  xType  = xField.getAttribute( "type" );
               if( xType.equals( "user" )) {
                  final String xUser = xField.getAttribute( "user" );
                  if(( xUser != null )&&( ! generated.contains( xUser ))) {
                     SortedSet<String> types = _typesUsage.get( interfaceName );
                     if( types == null ) {
                        _typesUsage.put( interfaceName, types = new TreeSet<>());
                     }
                     types.add( xUser );
                     if( _enums.containsKey( xUser )) {
                        if( javaLanguage ) {
                           generateJavaEnum( xUser, genDir, packageOrNamespace );
                        }
                        else {
                           generateCppEnumHeader( xUser, genDir, packageOrNamespace );
                           generateCppEnumBody  ( xUser, genDir, packageOrNamespace );
                        }
                     }
                     else if( _structs.containsKey( xUser )) {
                        if( javaLanguage ) {
                           generateJavaStruct( xUser, genDir, packageOrNamespace );
                        }
                        else {
                           generateCppStructHeader( xUser, genDir, packageOrNamespace );
                           generateCppStructBody  ( xUser, genDir, packageOrNamespace );
                        }
                     }
                     else {
                        throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
                     }
                     generated.add( xUser );
                  }
               }
            }
         }
      }
   }

   private static String getSignature( NodeList xFields, boolean javaLanguage ) {
      String signature = ( xFields.getLength() > 0 ) ? "" : ( javaLanguage ? "" : "void" );
      for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
         final Element xField = (Element)xFields.item( j );
         final String  xName  = xField.getAttribute( "name" );
         final String  xType  = xField.getAttribute( "type" );
         if( ! signature.isBlank()) {
            signature += ", ";
         }
         if( javaLanguage ) {
            switch( xType ) {
            case "ushort" : signature += "int";  break;
            case "uint"   : signature += "long"; break;
            case "user"   : signature += xField.getAttribute( "user" ); break;
            case "string" : signature += "String"; break;
            default       : signature += xType; break;
            }
         }
         else {
            switch( xType ) {
            case "user"   : signature += "const " + xField.getAttribute( "user" ) + " &"; break;
            case "string" : signature += "const std::string &"; break;
            case "double" : signature += "const double &"; break;
            case "boolean": signature += "bool"; break;
            default       : signature += xType; break;
            }
         }
         signature += ' ' + xName;
      }
      return signature;
   }

   private static void generateCppRequiredHeaders(
      NodeList xRequires,
      String   genDir,
      String   namespace ) throws IOException
   {
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element xRequired     = (Element)xRequires.item( i );
         final String  interfaceName = xRequired.getAttribute( "interface" );
         final String  nspath        = namespace.replaceAll( "::", "/" );
         final File    target        = new File( genDir, nspath + "/I" + interfaceName + ".hpp" );
         if( ! isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final Map<String, NodeList> iface         = _interfaces.get( interfaceName );
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
                  final String sign = getSignature( xFields, false );
                  if( sign.equals( "void" )) {
                     ps.printf( "      virtual void %s( sockaddr_in & target ) = 0;\n", event, sign );
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

   private static String getPathOfField( Element xField ) {
      final Element xEvent     = (Element)xField.getParentNode();
      final Element xInterface = (Element)xEvent.getParentNode();
      final String  field      = xField    .getAttribute( "name" );
      final String  event      = xEvent    .getAttribute( "name" );
      final String  intrfc     = xInterface.getAttribute( "name" );
      return "interface[@name=" + intrfc + "]"
         + "/event[@name=" + event + "]"
         + "/field[@name=" + field + "]";
   }

   private static int getEnumSize( Element xField ) {
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

   private static int getStructSize( Collection<Element> fields ) {
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

   private static int getUserTypeSize( Element xField ) {
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

   private static int getStringSize( Element xField ) {
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

   private static int getBufferCapacity( Collection<NodeList> xFieldsList ) {
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

   private static String toID( String eventName ) {
      final StringBuilder sb = new StringBuilder( 2*eventName.length());
      for( int i = 0, count = eventName.length(); i < count; ++i ) {
         final char    c          = eventName.charAt( i );
         final boolean nextExists = (( i + 1 ) < count );
         final char    next       = nextExists ? eventName.charAt( i + 1 ) : 0;
         sb.append( Character.toUpperCase( c ));
         if( Character.isLowerCase( c ) && nextExists && Character.isUpperCase( next )) {
            sb.append( '_' );
         }
      }
      return sb.toString().replaceAll( "__", "_" );
   }

   private static int getInterfaceRank( String ifaceName ) {
      final var it = _interfaces.keySet().iterator();
      for( int i = 0; i < _interfaces.size(); ++i ) {
         final String ifc = it.next();
         if( ifc.equals( ifaceName )) {
            return i;
         }
      }
      throw new IllegalStateException( "Unknown interface '" + ifaceName + "'" );
   }

   private static void generateCppRequiredBodies(
      NodeList xRequires,
      String   genDir,
      String   namespace ) throws IOException
   {
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element xRequired = (Element)xRequires.item( i );
         final String  ifaceName = xRequired.getAttribute( "interface" );
         final String  nspath    = namespace.replaceAll( "::", "/" );
         final File    target    = new File( genDir, nspath + "/net/" + ifaceName + ".cpp" );
         if( ! isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final String                ifaceID = toID( ifaceName ) + "_ID";
               final Map<String, NodeList> iface   = _interfaces.get( ifaceName );
               ps.printf( "#include <dab/I%s.hpp>\n", ifaceName );
               ps.printf( "\n" );
               ps.printf( "namespace %s::net {\n", namespace );
               ps.printf( "\n" );
               ps.printf( "   class %s : public ::%s::I%s {\n", ifaceName, namespace, ifaceName );
               ps.printf( "   public:\n" );
               ps.printf( "\n" );
               ps.printf( "      %s( io::DatagramSocket & socket ) :\n", ifaceName );
               ps.printf( "         _socket( socket ),\n" );
               ps.printf( "         _out( %d )\n", getBufferCapacity( iface.values()));
               ps.printf( "      {}\n" );
               ps.printf( "\n" );
               ps.printf( "   public:\n" );
               ps.printf( "\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  final String sign = getSignature( xFields, false );
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
                     final String xUser = xField.getAttribute( "user" );
                     switch( xType ) {
                     case "boolean": ps.printf( "         _out.putBoolean( %s );\n", xName ); break;
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
                     case "user"   :
                        if( _enums.containsKey( xUser )) {
                           ps.printf( "         _out.putByte( static_cast<byte>( %s ));\n", xName );
                        }
                        else if( _structs.containsKey( xUser )) {
                           ps.printf( "         %s.put( _out );\n", xName );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
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
               ps.printf( "         %s = %d,\n", ifaceID, 1 + getInterfaceRank( ifaceName ));
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
               ps.printf( "   ::%s::I%s * new%s( io::DatagramSocket & socket ) {\n",
                  namespace, ifaceName, ifaceName );
               ps.printf( "      return new ::%s::net::%s( socket );\n",
                  namespace, ifaceName );
               ps.printf( "   }\n" );
               ps.printf( "}\n" );
            }
            System.out.printf( "%s written\n", target.getPath());
         }
      }
   }

   private static void generateCppOfferedHeader(
      String   name,
      NodeList xOffers,
      String   genDir,
      String   namespace ) throws IOException
   {
      final String nspath = namespace.replaceAll( "::", "/" );
      final File   target = new File( genDir, nspath + "/I" + name + ".hpp" );
      if( ! isUpToDate( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.printf( "#pragma once\n" );
            ps.printf( "\n" );
            ps.printf( "#include <string>\n" );
            ps.printf( "#include <types.hpp>\n" );
            ps.printf( "\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element           xOffer      = (Element)xOffers.item( i );
               final String            xIntrfcName = xOffer.getAttribute( "interface" );
               final SortedSet<String> types       = _typesUsage.get( xIntrfcName );
               if( types != null ) {
                  for( final String type : types ) {
                     ps.printf( "#include <%s/%s.hpp>\n", namespace, type );
                     ps.printf( "\n" );
                  }
               }
            }
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
               final Map<String, NodeList> iface       = _interfaces.get( xIntrfcName );
               for( final var e : iface.entrySet()) {
                  final String   srvcName = e.getKey();
                  final NodeList xFields  = e.getValue();
                  ps.printf( "      virtual void %s( %s ) = 0;\n",
                     srvcName, getSignature( xFields, false ));
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

   private static void generateCppDispatcherHeader(
      String name,
      String genDir,
      String namespace ) throws IOException
   {
      final String nspath = namespace.replaceAll( "::", "/" );
      final File   target = new File( genDir, nspath + "/I" + name + "Dispatcher.hpp" );
      if( ! isUpToDate( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.printf( "#pragma once\n" );
            ps.printf( "\n" );
            ps.printf( "#include <dab/I%s.hpp>\n", name );
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
               + "( io::DatagramSocket & socket, IUniteDeTraitement & listener );\n", name, name );
            ps.printf( "}\n" );
         }
         System.out.printf( "%s written\n", target.getPath());
      }
   }

   private static void generateCppDispatcherBody(
      String   name,
      NodeList xOffers,
      String   genDir,
      String   namespace ) throws IOException
   {
      final String nspath = namespace.replaceAll( "::", "/" );
      final File   target = new File( genDir, nspath + "/net/" + name + "Dispatcher.cpp" );
      if( ! isUpToDate( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            int intrfcMaxWidth = 0;
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               intrfcMaxWidth = Math.max( toID( xIntrfcName ).length(), intrfcMaxWidth );
            }
            ps.printf( "#include <dab/I%sDispatcher.hpp>\n", name );
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
                  toID( xIntrfcName ), 1 + getInterfaceRank( xIntrfcName ));
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
               final Map<String, NodeList> iface       = _interfaces.get( xIntrfcName );
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
                     final String  xUser  = xField.getAttribute( "user" );
                     switch( xType ) {
                     case "boolean": ps.printf( "            bool %s = _in.getBoolean();\n"      , xName ); break;
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
                     case "user"   :
                        if( _enums.containsKey( xUser )) {
                           ps.printf( "            %s %s = static_cast<%s>( _in.getByte());\n", xUser, xName, xUser );
                        }
                        else if( _structs.containsKey( xUser )) {
                           ps.printf( "            %s %s;\n", xUser, xName );
                           ps.printf( "            %s.get( _in );\n", xName );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
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
            ps.printf( "         _in      ( %-8d )\n", getBufferCapacity( allEvents ));
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

   private static void generateCppComponent(
      String   name,
      NodeList xOffers,
      NodeList xRequires,
      String   genDir,
      String   namespace ) throws IOException
   {
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy( xOffers  , generated, genDir, namespace, false );
      generateTypesUsedBy( xRequires, generated, genDir, namespace, false );
      generated.clear();

      generateCppRequiredHeaders( xRequires, genDir, namespace );
      generateCppRequiredBodies ( xRequires, genDir, namespace );

      generateCppOfferedHeader( name, xOffers, genDir, namespace );
      generateCppDispatcherHeader( name, genDir, namespace );
      generateCppDispatcherBody( name, xOffers, genDir, namespace );
   }

   private static void generateJavaInterface(
      NodeList xFacets,
      String   genDir,
      String   pckg     ) throws IOException
   {
      for( int i = 0, iCount = xFacets.getLength(); i < iCount; ++i ) {
         final Element xFacet        = (Element)xFacets.item( i );
         final String  interfaceName = xFacet.getAttribute( "interface" );
         final String  nspath        = pckg.replaceAll( "\\.", "/" );
         final File    target        = new File( genDir, nspath + "/I" + interfaceName + ".java" );
         if( ! isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final Map<String, NodeList> iface = _interfaces.get( interfaceName );
               ps.printf( "package %s;\n", pckg );
               ps.printf( "\n" );
               ps.printf( "import java.io.IOException;\n" );
               ps.printf( "import java.net.SocketAddress;\n" );
               ps.printf( "\n" );
               final SortedSet<String> enums = _typesUsage.get( interfaceName );
               if( enums != null ) {
                  for( final String enm : enums ) {
                     ps.printf( "import %s.%s;\n", pckg, enm );
                  }
                  ps.printf( "\n" );
               }
               ps.printf( "public interface I%s {\n", interfaceName );
               ps.printf( "\n" );
               int width = 0;
               for( final var e : iface.entrySet()) {
                  final String event = e.getKey();
                  width = Math.max( width, event.length());
               }
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  final String   sign    = getSignature( xFields, true );
                  ps.printf( "   void %-" + width + "s(", event );
                  if( sign.isBlank()) {
                     ps.printf( " SocketAddress from ", sign );
                  }
                  else {
                     ps.printf( " SocketAddress from, %s ", sign );
                  }
                  ps.printf( ") throws IOException;\n", event );
               }
               ps.printf( "}\n" );
            }
            System.out.printf( "%s written\n", target.getPath());
         }
      }
   }

   private static void generateJavaRequired(
      NodeList xRequires,
      String   genDir,
      String   pckg     ) throws IOException
   {
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element xRequired     = (Element)xRequires.item( i );
         final String  interfaceName = xRequired.getAttribute( "interface" );
         final String  nspath        = pckg.replaceAll( "\\.", "/" );
         final File    target        =
            new File( genDir, nspath + "/net/" + interfaceName + ".java" );
         if( ! isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final Map<String, NodeList> iface = _interfaces.get( interfaceName );
               ps.printf( "package %s.net;\n", pckg );
               ps.printf( "\n" );
               ps.printf( "import java.io.IOException;\n" );
               ps.printf( "import java.net.SocketAddress;\n" );
               ps.printf( "import java.nio.ByteBuffer;\n" );
               ps.printf( "import java.nio.channels.DatagramChannel;\n" );
               ps.printf( "\n" );
               ps.printf( "import io.ByteBufferHelper;\n" );
               ps.printf( "import %s.I%s;\n", pckg, interfaceName );
               ps.printf( "\n" );
               final SortedSet<String> enums = _typesUsage.get( interfaceName );
               if( enums != null ) {
                  for( final String enm : enums ) {
                     ps.printf( "import %s.%s;\n", pckg, enm );
                  }
                  ps.printf( "\n" );
               }
               ps.printf( "public class %s implements I%s {\n", interfaceName, interfaceName );
               ps.printf( "\n" );
               final String ifaceID = toID( interfaceName );
               ps.printf( "   private static final byte %s = %d;\n",
                  ifaceID, 1 + getInterfaceRank( interfaceName ));
               ps.printf( "\n" );
               int eventMaxWidth = 0;
               for( final String eventName : iface.keySet()) {
                  eventMaxWidth = Math.max( eventMaxWidth, toID( eventName ).length());
               }
               int rank = 0;
               for( final var e : iface.entrySet()) {
                  final String event = e.getKey();
                  ps.printf( "   private static final byte %-" + eventMaxWidth + "s = %d;\n",
                     toID( event ), ++rank );
               }
               ps.printf( "\n" );
               ps.printf( "   private final DatagramChannel _channel;\n" );
               ps.printf( "   private final ByteBuffer      _out = ByteBuffer.allocate( %d );\n",
                  getBufferCapacity( iface.values()));
               ps.printf( "\n" );
               ps.printf( "   public %s( DatagramChannel channel ) {\n", interfaceName );
               ps.printf( "      _channel = channel;\n" );
               ps.printf( "   }\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  final String   sign    = getSignature( xFields, true );
                  ps.printf( "\n" );
                  ps.printf( "   @Override\n" );
                  ps.printf( "   public void %s(", event );
                  if( sign.isBlank()) {
                     ps.printf( " SocketAddress target ", sign );
                  }
                  else {
                     ps.printf( " SocketAddress target, %s ", sign );
                  }
                  ps.printf( ") throws IOException {\n", event );
                  ps.printf( "      _out.clear();\n" );
                  ps.printf( "      _out.put( %s );\n", ifaceID );
                  ps.printf( "      _out.put( %s );\n", toID( event ));
                  for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
                     final Element xField = (Element)xFields.item( j );
                     final String xName = xField.getAttribute( "name" );
                     final String xType = xField.getAttribute( "type" );
                     final String xUser = xField.getAttribute( "user" );
                     switch( xType ) {
                     case "boolean": ps.printf( "      ByteBufferHelper.putBoolean( _out, %s );\n", xName ); break;
                     case "byte"   : ps.printf( "      _out.put( %s );\n"       , xName ); break;
                     case "short"  : ps.printf( "      _out.putShort( %s );\n"  , xName ); break;
                     case "ushort" : ps.printf( "      _out.putShort((short)%s );\n", xName ); break;
                     case "int"    : ps.printf( "      _out.putInt( %s );\n"    , xName ); break;
                     case "uint"   : ps.printf( "      _out.putInt((int)%s );\n", xName ); break;
                     case "long"   : ps.printf( "      _out.putLong( %s );\n"   , xName ); break;
                     case "ulong"  : ps.printf( "      _out.putLong( %s );\n"   , xName ); break;
                     case "float"  : ps.printf( "      _out.putFloat( %s );\n"  , xName ); break;
                     case "double" : ps.printf( "      _out.putDouble( %s );\n" , xName ); break;
                     case "string" : ps.printf( "      ByteBufferHelper.putString( _out, %s );\n" , xName ); break;
                     case "user"   :
                        if( _enums.containsKey( xUser )) {
                           ps.printf( "      _out.put((byte)%s.ordinal());\n", xName );
                        }
                        else if( _structs.containsKey( xUser )) {
                           ps.printf( "      %s.put( _out );\n", xName );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
                        }
                        break;
                     }
                  }
                  ps.printf( "      _out.flip();\n" );
                  ps.printf( "      _channel.send( _out, target );\n" );
                  ps.printf( "      System.err.printf( \"LecteurDeCarte.carteLue|informations sent to %%s\\n\", target );\n" );
                  ps.printf( "   }\n" );
               }
               ps.printf( "}\n" );
            }
            System.out.printf( "%s written\n", target.getPath());
         }
      }
   }

   private static void generateJavaDispatcher(
      String   name,
      NodeList xOffers,
      String   genDir,
      String   pckg     ) throws IOException
   {
      final String nspath = pckg.replaceAll( "\\.", "/" );
      final File   target = new File( genDir, nspath + "/net/" + name + "Dispatcher.java" );
      if( ! isUpToDate( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.printf( "package %s.net;\n", pckg );
            ps.printf( "\n" );
            ps.printf( "import java.io.IOException;\n" );
            ps.printf( "import java.net.SocketAddress;\n" );
            ps.printf( "import java.nio.ByteBuffer;\n" );
            ps.printf( "import java.nio.channels.DatagramChannel;\n" );
            ps.printf( "\n" );
            ps.printf( "import io.ByteBufferHelper;\n" );
            ps.printf( "import %s.I%s;\n", pckg, name );
            ps.printf( "\n" );
            final SortedSet<String> enums = _typesUsage.get( name );
            if( enums != null ) {
               for( final String enm : enums ) {
                  ps.printf( "import %s.%s;\n", pckg, enm );
               }
               ps.printf( "\n" );
            }
            ps.printf( "public class %sDispatcher {\n", name );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffered  = (Element)xOffers.item( i );
               final String  ifaceName = xOffered.getAttribute( "interface" );
               ps.printf( "\n" );
               ps.printf( "   private static final byte INTRFC_%s = %d;\n",
                  toID( ifaceName ), 1 + getInterfaceRank( ifaceName ));
            }
            final List<NodeList> allEvents = new LinkedList<>();
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element               xOffer      = (Element)xOffers.item( i );
               final String                xIntrfcName = xOffer.getAttribute( "interface" );
               final Map<String, NodeList> iface       = _interfaces.get( xIntrfcName );
               allEvents.addAll( iface.values());
               ps.printf( "\n" );
               ps.printf( "   private static enum %sEvent {\n", xIntrfcName );
               int eventID = 0;
               for( final String eventName : iface.keySet()) {
                  ps.printf( "      %s( %d ),\n", toID( eventName ), ++eventID );
               }
               ps.printf( "      ;\n" );
               ps.printf( "\n" );
               ps.printf( "      private final int _value;\n" );
               ps.printf( "\n" );
               ps.printf( "      %sEvent( int value ) {\n", xIntrfcName );
               ps.printf( "         _value = value;\n" );
               ps.printf( "      }\n" );
               ps.printf( "\n" );
               ps.printf( "      static %sEvent valueOf( int value ) {\n", xIntrfcName );
               ps.printf( "         for( final %sEvent e : values()) {\n", xIntrfcName );
               ps.printf( "            if( e._value == value ) {\n" );
               ps.printf( "               return e;\n" );
               ps.printf( "            }\n" );
               ps.printf( "         }\n" );
               ps.printf( "         return null;\n" );
               ps.printf( "      }\n" );
               ps.printf( "   }\n" );
               ps.printf( "\n" );
               ps.printf( "   private boolean dispatch( %sEvent event, SocketAddress from ) "
                  + "throws IOException {\n", xIntrfcName );
               ps.printf( "      switch( event ) {\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  ps.printf( "      case %s:{\n", toID( event ));
                  String signature = "";
                  for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
                     final Element xField = (Element)xFields.item( j );
                     final String  xName  = xField.getAttribute( "name" );
                     final String  xType  = xField.getAttribute( "type" );
                     final String  xUser  = xField.getAttribute( "user" );
                     ps.printf( "         " );
                     switch( xType ) {
                     case "boolean":
                        ps.printf( "final boolean %s = ByteBufferHelper.getBoolean( _in );\n", xName );
                        break;
                     case "byte"   : ps.printf( "final byte %s = _in.get();\n"        , xName ); break;
                     case "short"  : ps.printf( "final short %s = _in.getShort();\n"  , xName ); break;
                     case "ushort" : ps.printf( "final short %s = _in.getShort();\n"  , xName ); break;
                     case "int"    : ps.printf( "final int %s = _in.getInt();\n"      , xName ); break;
                     case "uint"   : ps.printf( "final int %s = _in.getInt();\n"      , xName ); break;
                     case "long"   : ps.printf( "final long %s = _in.getLong();\n"    , xName ); break;
                     case "ulong"  : ps.printf( "final long %s = _in.getLong();\n"    , xName ); break;
                     case "float"  : ps.printf( "final float %s = _in.getFloat();\n"  , xName ); break;
                     case "double" : ps.printf( "final double %s = _in.getDouble();\n", xName ); break;
                     case "string" :
                        ps.printf( "final String %s = ByteBufferHelper.getString( _in );\n", xName );
                        break;
                     case "user"   :
                        if( _enums.containsKey( xUser )) {
                           ps.printf( "final %s %s = %s.values()[_in.get()];\n", xUser, xName, xUser );
                        }
                        else if( _structs.containsKey( xUser )) {
                           ps.printf( "final %s %s = new %s( _in );\n", xUser, xName, xUser );
                        }
                        else {
                           throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
                        }
                     break;
                     }
                     if( ! signature.isBlank()) {
                        signature += ", ";
                     }
                     signature += xName;
                  }
                  if( signature.isBlank()) {
                     ps.printf( "         _listener.%s( from );\n", event );
                  }
                  else {
                     ps.printf( "         _listener.%s( from, %s );\n", event, signature );
                  }
                  ps.printf( "         return true;}\n" );
               }
               ps.printf( "      default:\n" );
               ps.printf( "         System.err.printf( \"%%s.run|Unexpected event\\n\","
                  + " getClass().getName());\n" );
               ps.printf( "         return false;\n" );
               ps.printf( "      }\n" );
               ps.printf( "   }\n" );
            }
            ps.printf( "\n" );
            ps.printf( "   private final DatagramChannel _channel;\n" );
            ps.printf( "   private final I%-14s _listener;\n", name );
            ps.printf( "   private final ByteBuffer      _in = ByteBuffer.allocate( %d );\n",
               getBufferCapacity( allEvents ));
            ps.printf( "\n" );
            ps.printf( "   public %sDispatcher( DatagramChannel channel, I%s listener ) {\n",
               name, name );
            ps.printf( "      _channel  = channel;\n" );
            ps.printf( "      _listener = listener;\n" );
            ps.printf( "   }\n" );
            ps.printf( "\n" );
            ps.printf( "   public boolean hasDispatched() throws IOException {\n" );
            ps.printf( "      _in.clear();\n" );
            ps.printf( "      final SocketAddress from = _channel.receive( _in );\n" );
            ps.printf( "      _in.flip();\n" );
            ps.printf( "      final byte intrfc = _in.get();\n" );
            ps.printf( "      final byte event  = _in.get();\n" );
            ps.printf( "      System.err.printf( \"%%s.run|intrfc = %%d, event = %%d\\n\","
               + " getClass().getName(), intrfc, event );\n" );
            ps.printf( "      switch( intrfc ) {\n" );
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element xOffer      = (Element)xOffers.item( i );
               final String  xIntrfcName = xOffer.getAttribute( "interface" );
               ps.printf( "      case INTRFC_%s: return dispatch( %sEvent.valueOf( event ), from );\n",
                  toID( xIntrfcName ), xIntrfcName );
            }
            ps.printf( "      }\n" );
            ps.printf( "      return false;\n" );
            ps.printf( "   }\n" );
            ps.printf( "}\n" );
         }
         System.out.printf( "%s written\n", target.getPath());
      }
   }

   private static void generateJavaComponent(
      String   name,
      NodeList xOffers,
      NodeList xRequires,
      String   genDir,
      String   pckg     ) throws IOException
   {
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy( xOffers  , generated, genDir, pckg, true );
      generateTypesUsedBy( xRequires, generated, genDir, pckg, true );
      generated.clear();

      generateJavaInterface( xRequires, genDir, pckg );
      generateJavaRequired ( xRequires, genDir, pckg );

      generateJavaInterface (       xOffers, genDir, pckg );
      generateJavaDispatcher( name, xOffers, genDir, pckg );
   }

   private static void generateComponents( Document doc ) throws IOException {
      final NodeList xComponents = doc.getElementsByTagName( "component" );
      for( int i = 0, iCount = xComponents.getLength(); i < iCount; ++i ) {
         final Element  xComponent = (Element)xComponents.item( i );
         final String   name       = xComponent.getAttribute( "name" );
         final NodeList xOffers    = xComponent.getElementsByTagName( "offers" );
         final NodeList xRequires  = xComponent.getElementsByTagName( "requires" );
         final NodeList xJavas     = xComponent.getElementsByTagName( "java" );
         final NodeList xCpps      = xComponent.getElementsByTagName( "cpp" );
         if( xJavas.getLength() == 1 ) {
            final Element xJava    = (Element)xJavas.item( 0 );
            final String  xSrcDir  = xJava.getAttribute( "src-dir" );
            final String  xPackage = xJava.getAttribute( "package" );
            generateJavaComponent( name, xOffers, xRequires, xSrcDir, xPackage );
         }
         else if( xCpps.getLength() == 1 ) {
            final Element xCpp       = (Element)xCpps.item( 0 );
            final String  xSrcDir    = xCpp.getAttribute( "src-dir" );
            final String  xNamespace = xCpp.getAttribute( "namespace" );
            generateCppComponent( name, xOffers, xRequires, xSrcDir, xNamespace );
         }
         else {
            throw new IllegalStateException( "XML non valide !" );
         }
      }
   }

   public static void main( String[] args ) throws Throwable {
      if( args.length != 1 ) {
         System.err.println( "usage: disapp.generator.Main <xml system file>" );
         System.exit(1);
      }
      final File model = new File( args[0] );
      final Document doc =
         DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( model );
      readEnums( doc );
      readStructs( doc );
      readInterfaces( doc );
      _modelLastModified = model.lastModified();
      generateComponents( doc );
      System.exit(0);
   }
}
