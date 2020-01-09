struct( package, struct, maxLength, maxStrLength, types ) ::= <<
package <package>;
// struct

import java.nio.ByteBuffer;

import io.ByteBufferHelper;

public class <struct.name> {

<struct.field:{field|
   public < if( field.isStruct )>final <common/type( field )> <field.name> = new <common/type( field )>();
< else><common/type( field )> <field.name>;
< endif>
}>
   public void set( <struct.name> source ) {
      <struct.field:{field|
< if( field.isStruct  )><field.name>.set( source.<field.name> );
< else><field.name> = source.<field.name>;
< endif>
}>   }

   public void put( ByteBuffer target ) {
      <struct.field:{field|
< if(     field.isBoolean )>ByteBufferHelper.putBoolean( target, <field.name> );
< elseif( field.isByte    )>target.put       ( <field.name> );
< elseif( field.isShort   )>target.putShort  ( <field.name> );
< elseif( field.isUshort  )>target.putShort  ( <field.name> );
< elseif( field.isInt     )>target.putInt    ( <field.name> );
< elseif( field.isUint    )>target.putInt    ( <field.name> );
< elseif( field.isLong    )>target.putLong   ( <field.name> );
< elseif( field.isUlong   )>target.putLong   ( <field.name> );
< elseif( field.isFloat   )>target.putFloat  ( <field.name> );
< elseif( field.isDouble  )>target.putDouble ( <field.name> );
< elseif( field.isEnum    )>target.put       ((byte)<field.name>.ordinal());
< elseif( field.isString  )>ByteBufferHelper.putString( target, <field.name> );
< elseif( field.isStruct  )><field.name>.put( target );
< endif>
}>   }

   public void get( ByteBuffer source ) {
      <struct.field:{field|
< if(     field.isBoolean )><field.name; format="width"> = ByteBufferHelper.getBoolean( source );
< elseif( field.isByte    )><field.name; format="width"> = source.get();
< elseif( field.isShort   )><field.name; format="width"> = source.getShort();
< elseif( field.isUshort  )><field.name; format="width"> = source.getShort();
< elseif( field.isInt     )><field.name; format="width"> = source.getInt();
< elseif( field.isUint    )><field.name; format="width"> = source.getInt();
< elseif( field.isLong    )><field.name; format="width"> = source.getLong();
< elseif( field.isUlong   )><field.name; format="width"> = source.getLong();
< elseif( field.isFloat   )><field.name; format="width"> = source.getFloat();
< elseif( field.isDouble  )><field.name; format="width"> = source.getDouble();
< elseif( field.isEnum    )><field.name; format="width"> = <types.(field.userType)>.values()[source.get()];
< elseif( field.isString  )><field.name; format="width"> = ByteBufferHelper.getString( source );
< elseif( field.isStruct  )><field.name; format="width">.get( source );
< endif>
}>   }
}

>>
