package disapp.generator;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.stringtemplate.v4.StringRenderer;

public class BaseRenderer extends StringRenderer {

   public Map<String, Object> _properties = new HashMap<>();

   public final static String toID( String name ) {
      final StringBuilder sb = new StringBuilder( 2*name.length());
      for( int i = 0, count = name.length(); i < count; ++i ) {
         final char    c          = name.charAt( i );
         final boolean nextExists = (( i + 1 ) < count );
         final char    next       = nextExists ? name.charAt( i + 1 ) : 0;
         sb.append( Character.toUpperCase( c ));
         if( Character.isLowerCase( c ) && nextExists && Character.isUpperCase( next )) {
            sb.append( '_' );
         }
      }
      return sb.toString();
   }

   public static String lowerCamelCase( String name ) {
      final StringBuilder sb = new StringBuilder( 2*name.length());
      for( int i = 0, count = name.length(); i < count; ++i ) {
         final char    c          = name.charAt( i );
         final boolean nextExists = (( i + 1 ) < count );
         if( nextExists &&(( c == '_' )||( c == '-' ))) {
            sb.append( Character.toUpperCase( name.charAt( ++i )));
         }
         else {
            sb.append( c );
         }
      }
      final String retVal = sb.toString();
      return ( retVal.length() > 0 )
         ? (Character.toLowerCase( retVal.charAt(0)) + retVal.substring( 1 ))
         : retVal;
   }

   public static String cap( String str ) {
      return ( str.length() > 0 )
         ? (Character.toUpperCase( str.charAt(0)) + str.substring( 1 ))
         : str;
   }

   protected String apply( String command, Locale locale, String str ) {
      switch( command ) {
      case "upper"         : return str.toString().toUpperCase( locale );
      case "lower"         : return str.toString().toLowerCase( locale );
      case "cap"           : return cap( str );
      case "UpperCamelCase": return cap( lowerCamelCase( str ));
      case "lowerCamelCase": return lowerCamelCase( str );
      case "url-encode"    : return URLEncoder.encode( str, Charset.defaultCharset());
      case "xml-encode"    : return escapeHTML( str );
      case "argument"      : return (str.length() > 0) ? Character.toLowerCase( str.charAt(0)) + str.substring( 1 ) : str;
      case "ID"            : return toID( str );
      case "width"         :
      case "strWidth"      :
         final Object property = _properties.get( command );
         if( property != null ) {
            final int width = (int)property;
            if( str.length() < width ) {
               for( int i = 0, count = ((int)property) - str.length(); i < count; ++i ) {
                  str +=  ' ';
               }
            }
         }
         break;
      }
      return str;
   }

   @Override
   public final String toString( Object o, String formatString, Locale locale ) {
      String str = (String)o;
      if( formatString == null ) {
         return str;
      }
      final String[] commands = formatString.split( "," );
      for( final String command : commands ) {
         str = apply( command, locale, str );
      }
      return str;
   }

   public final void set( String propertyName, Object propertyValue ) {
      _properties.put( propertyName, propertyValue );
   }

   public final Object get( String propertyName ) {
      return _properties.get( propertyName );
   }

   @SuppressWarnings("static-method")
   public String name( String name ) {
      return name;
   }
}
