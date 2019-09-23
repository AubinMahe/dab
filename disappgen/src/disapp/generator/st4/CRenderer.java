package disapp.generator.st4;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.stringtemplate.v4.StringRenderer;

public final class CRenderer extends StringRenderer {

   public Map<String, Object> _properties = new HashMap<>();

   public static StringBuilder cname( String name ) {
      final int len = name.length();
      final StringBuilder sb = new StringBuilder( 2*len );
      sb.append( Character.toLowerCase( name.charAt( 0 )));
      boolean previousIsUppercase = Character.isUpperCase( name.charAt( 0 ));
      for( int i = 1; i < len; ++i ) {
         if( Character.isUpperCase( name.charAt( i )) && ! previousIsUppercase ) {
            if( sb.charAt( sb.length() - 1 ) != '_' ) {
               sb.append( '_' );
            }
         }
         sb.append( Character.toLowerCase( name.charAt( i )));
         previousIsUppercase = Character.isUpperCase( name.charAt( i ));
      }
      return sb;
   }

   public static StringBuilder toID( String name ) {
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
      return sb;
   }

   private String applyProperties( StringBuilder sb, String formatString, Locale locale, String[] args ) {
      String retVal = sb.toString().replaceAll( "__", "_" );
      if( args.length > 1 ) {
         final String prop = args[1];
         final Object property = _properties.get( prop );
         if( property == null ) {
            return super.toString( retVal, formatString.replaceAll( "cname,", "" ), locale );
         }
         if( prop.equals( "width" )|| prop.equals( "strWidth" )) {
            final int width = (int)property;
            if( retVal.length() < width ) {
               for( int i = 0, count = ((int)property) - retVal.length(); i < count; ++i ) {
                  sb.append( ' ' );
               }
               retVal = sb.toString();
            }
         }
      }
      return retVal;
   }

   @Override
   public String toString( Object o, String formatString, Locale locale ) {
      final String name = (String)o;
      if( formatString != null ) {
         final String[] args = formatString.split(",");
         switch( args[0] ) {
         case "cname": return applyProperties( cname( name ), formatString, locale, args );
         case "ID"   : return applyProperties(  toID( name ), formatString, locale, args );
         }
      }
      return super.toString( o, formatString, locale );
   }

   public void set( String propertyName, Object propertyValue ) {
      _properties.put( propertyName, propertyValue );
   }

   public Object get( String propertyName ) {
      return _properties.get( propertyName );
   }
}
