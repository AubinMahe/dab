package disapp.generator;

import java.util.Locale;

public final class CRenderer extends BaseRenderer {

   public static String cname( String name ) {
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
      return sb.toString().replaceAll( "__", "_" );
   }

   @Override
   protected String apply( String command, Locale locale, String str ) {
      if( command.equals( "cname" )) {
         return cname( str );
      }
      return super.apply( command, locale, str );
   }

   @Override
   public String name( String name ) {
      return cname( name );
   }
}
