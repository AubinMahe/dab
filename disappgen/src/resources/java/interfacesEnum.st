interfacesEnum( package, interfaces ) ::= <<
package <package>;
// interfacesEnum

import java.nio.ByteBuffer;

public enum Interfaces {

   TIMEOUT( 1 ),
   <interfaces:{ifaceName|<ifaceName; format="ID">( <interfaces.(ifaceName)> )}; separator=",\n">
   ;

   private final byte _value;

   Interfaces( int value ) {
      _value = (byte)value;
   }

   static public Interfaces valueOf( int value ) {
      for( final Interfaces e : values()) {
         if( e._value == value ) {
            return e;
         }
      }
      return null;
   }

   public void put( ByteBuffer target ) {
      target.put( _value );
   }
}

>>
