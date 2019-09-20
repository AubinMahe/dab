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

public class JavaGenerator extends BaseGenerator {

   public JavaGenerator( Model model ) {
      super( model );
   }

   @Override
   protected String getSignature( NodeList xFields, String pckg ) {
      String signature = "";
      for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
         final Element xField = (Element)xFields.item( j );
         final String  xName  = xField.getAttribute( "name" );
         final String  xType  = xField.getAttribute( "type" );
         if( ! signature.isBlank()) {
            signature += ", ";
         }
         switch( xType ) {
         case "ushort" : signature += "int";  break;
         case "uint"   : signature += "long"; break;
         case "user"   : signature += xField.getAttribute( "user" ); break;
         case "string" : signature += "String"; break;
         default       : signature += xType; break;
         }
         signature += ' ' + xName;
      }
      return signature;
   }

   @Override
   protected void generateEnum( String enumName, String genDir, String pckg ) throws IOException {
      final File target =
         new File( genDir, pckg.replaceAll( "\\.", "/" ) + '/' + enumName + ".java" );
      if( _model.isUpToDate( target )) {
         return;
      }
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.printf( "package %s;\n", pckg );
         ps.printf( "\n" );
         ps.printf( "public enum %s {\n", enumName );
         ps.printf( "\n" );
         final List<String> literals = _model.getEnum( enumName );
         for( final String literal : literals ) {
            ps.printf( "   %s,\n", literal );
         }
         ps.printf( "}\n" );
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   @Override
   protected void generateStruct( String sName, String genDir, String pckg ) throws IOException {
      final File target = new File( genDir, pckg.replaceAll( "\\.", "/" ) + '/' + sName + ".java" );
      if( _model.isUpToDate( target )) {
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
         ps.printf( "public class %s {\n", sName );
         ps.printf( "\n" );
         final Map<String, Element> xFields = _model.getStruct( sName );
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
               if( _model.enumIsDefined( xUser )) {
                  ps.printf( "   %s _%s;\n", xUser, xName );
               }
               else if( _model.structIsDefined( xUser )) {
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
               if( _model.enumIsDefined( xUser )) {
                  ps.printf( "      target.put((byte)%s.ordinal());\n", xName );
               }
               else if( _model.structIsDefined( xUser )) {
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
               if( _model.enumIsDefined( xUser )) {
                  ps.printf( "      %s = %s.values()[source.get()];\n", xUser, xName, xUser );
               }
               else if( _model.structIsDefined( xUser )) {
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

   private void generateInterface( NodeList xFacets, String genDir, String pckg ) throws IOException {
      for( int i = 0, iCount = xFacets.getLength(); i < iCount; ++i ) {
         final Element xFacet        = (Element)xFacets.item( i );
         final String  interfaceName = xFacet.getAttribute( "interface" );
         final String  nspath        = pckg.replaceAll( "\\.", "/" );
         final File    target        = new File( genDir, nspath + "/I" + interfaceName + ".java" );
         if( ! _model.isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final Map<String, NodeList> iface = _model.getInterface( interfaceName );
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
                  final String   sign    = getSignature( xFields, pckg );
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

   private void generateRequired( NodeList xRequires, String genDir, String pckg ) throws IOException {
      for( int i = 0, iCount = xRequires.getLength(); i < iCount; ++i ) {
         final Element xRequired     = (Element)xRequires.item( i );
         final String  interfaceName = xRequired.getAttribute( "interface" );
         final String  nspath        = pckg.replaceAll( "\\.", "/" );
         final File    target        =
            new File( genDir, nspath + "/net/" + interfaceName + ".java" );
         if( ! _model.isUpToDate( target )) {
            target.getParentFile().mkdirs();
            try( final PrintStream ps = new PrintStream( target )) {
               final Map<String, NodeList> iface = _model.getInterface( interfaceName );
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
                  ifaceID, 1 + _model.getInterfaceRank( interfaceName ));
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
                  _model.getBufferCapacity( iface.values()));
               ps.printf( "\n" );
               ps.printf( "   public %s( DatagramChannel channel ) {\n", interfaceName );
               ps.printf( "      _channel = channel;\n" );
               ps.printf( "   }\n" );
               for( final var e : iface.entrySet()) {
                  final String   event   = e.getKey();
                  final NodeList xFields = e.getValue();
                  final String   sign    = getSignature( xFields, pckg );
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
                        if( _model.enumIsDefined( xUser )) {
                           ps.printf( "      _out.put((byte)%s.ordinal());\n", xName );
                        }
                        else if( _model.structIsDefined( xUser )) {
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

   private void generateDispatcher( String name, NodeList xOffers, String genDir, String pckg ) throws IOException {
      final String nspath = pckg.replaceAll( "\\.", "/" );
      final File   target = new File( genDir, nspath + "/net/" + name + "Dispatcher.java" );
      if( ! _model.isUpToDate( target )) {
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
                  toID( ifaceName ), 1 + _model.getInterfaceRank( ifaceName ));
            }
            final List<NodeList> allEvents = new LinkedList<>();
            for( int i = 0, iCount = xOffers.getLength(); i < iCount; ++i ) {
               final Element               xOffer      = (Element)xOffers.item( i );
               final String                xIntrfcName = xOffer.getAttribute( "interface" );
               final Map<String, NodeList> iface       = _model.getInterface( xIntrfcName );
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
                        if( _model.enumIsDefined( xUser )) {
                           ps.printf( "final %s %s = %s.values()[_in.get()];\n", xUser, xName, xUser );
                        }
                        else if( _model.structIsDefined( xUser )) {
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
               _model.getBufferCapacity( allEvents ));
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

   @Override
   protected void generateComponent(
      String   name,
      NodeList xOffers,
      NodeList xRequires,
      String   genDir,
      String   pckg     ) throws IOException
   {
      final List<String> generated = new LinkedList<>();
      generateTypesUsedBy( xOffers  , generated, genDir, pckg );
      generateTypesUsedBy( xRequires, generated, genDir, pckg );
      generated.clear();

      generateInterface( xRequires, genDir, pckg );
      generateRequired ( xRequires, genDir, pckg );

      generateInterface (       xOffers, genDir, pckg );
      generateDispatcher( name, xOffers, genDir, pckg );
   }
}
