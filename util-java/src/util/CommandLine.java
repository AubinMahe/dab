package util;

import java.util.HashMap;
import java.util.Map;

public class CommandLine {

   private final Map<String, String> _namedArgs = new HashMap<>();

   public boolean parse( String[] args ) {
      for( final String arg : args ) {
         final int start = arg.indexOf( "--" );
         final int sep   = arg.indexOf( "=" );
         if(( start == 0 )&&( sep > start )) {
            final String name  = arg.substring( 2, sep );
            final String value = arg.substring( sep + 1 );
            _namedArgs.put( name, value );
         }
         else {
            return false;
         }
      }
      return true;
   }

   public String getString( String name ) {
      return _namedArgs.get( name );
   }

   public int getInt( String name ) {
      return Integer.parseInt( _namedArgs.get( name ));
   }

   public double getDouble( String name ) {
      return Double.parseDouble( _namedArgs.get( name ));
   }
}
