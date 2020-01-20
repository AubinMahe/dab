instancesEnum( package, instances ) ::= <<
package <package>;
// instancesEnum
import java.nio.ByteBuffer;

enum Instances implements da.InstanceID {

   <instances:{instance|<instance.name; format="ID">( <i> )}; separator=",\n">
   ;

   private final byte _value;

   Instances( int value ) {
      _value = (byte)value;
   }

   static public Instances valueOf( int value ) {
      for( final Instances e : values()) {
         if( e._value == value ) {
            return e;
         }
      }
      return null;
   }

   @Override
   public void put( ByteBuffer target ) {
      target.put( _value );
   }

   @Override
   public byte value() {
      return _value;
   }
}

>>
