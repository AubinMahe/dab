structBody( namespace, struct, modelName, maxLength, maxStrLength, types ) ::= <<
#include \<<types.(modelName); format="NamespaceToPath">.hpp>
#include \<string.h>

using namespace <namespace>;

void <struct.name>::put( io::ByteBuffer & target ) const {
<struct.field:{field|
   < if(     field.isBoolean )>target.putBool  ( <field.name> );
   < elseif( field.isByte    )>target.putByte  ( <field.name> );
   < elseif( field.isShort   )>target.putShort ( <field.name> );
   < elseif( field.isUshort  )>target.putUShort( <field.name> );
   < elseif( field.isInt     )>target.putInt   ( <field.name> );
   < elseif( field.isUint    )>target.putUInt  ( <field.name> );
   < elseif( field.isLong    )>target.putLong  ( <field.name> );
   < elseif( field.isUlong   )>target.putULong ( <field.name> );
   < elseif( field.isFloat   )>target.putFloat ( <field.name> );
   < elseif( field.isDouble  )>target.putDouble( <field.name> );
   < elseif( field.isString  )>target.putString( <field.name> );
   < elseif( field.isEnum    )>target.putByte  ((byte)<field.name> );
   < elseif( field.isStruct  )><field.name>.put( target );
   < endif>
}>}

void <struct.name>::get( io::ByteBuffer & source ) {
<struct.field:{field|
   < if(     field.isBoolean )><field.name; format="width"> = source.getBool  ();
   < elseif( field.isByte    )><field.name; format="width"> = source.getByte  ();
   < elseif( field.isShort   )><field.name; format="width"> = source.getShort ();
   < elseif( field.isUshort  )><field.name; format="width"> = source.getUShort();
   < elseif( field.isInt     )><field.name; format="width"> = source.getInt   ();
   < elseif( field.isUint    )><field.name; format="width"> = source.getUInt  ();
   < elseif( field.isLong    )><field.name; format="width"> = source.getLong  ();
   < elseif( field.isUlong   )><field.name; format="width"> = source.getULong ();
   < elseif( field.isFloat   )><field.name; format="width"> = source.getFloat ();
   < elseif( field.isDouble  )><field.name; format="width"> = source.getDouble();
   < elseif( field.isString  )>source.getString( <field.name>, sizeof( <field.name> ));
   < elseif( field.isEnum    )><field.name; format="width"> = (<types.(field.userType)>)source.getByte();
   < elseif( field.isStruct  )><field.name>.get( source );
   < endif>
}>}

void <struct.name>::set( const <struct.name> & source ) {
<struct.field:{field|
   < if(     field.isStruct )><field.name>.set( source.<field.name> );
   < elseif( field.isString )>memmove( <field.name>, source.<field.name>, sizeof( <field.name> ));
   < else                    ><field.name> = source.<field.name>;
   < endif>
}>}

>>
