byteBufferGet( fields ) ::= <<
<fields:{field|
<     if( field.isBoolean )>bool <field.name> = in.getBool();
< elseif( field.isByte    )>byte <field.name> = in.getByte();
< elseif( field.isShort   )>short <field.name> = in.getShort();
< elseif( field.isUshort  )>unsigned short <field.name> = in.getUShort();
< elseif( field.isInt     )>int <field.name> = in.getInt();
< elseif( field.isUint    )>unsigned int <field.name> = in.getUInt();
< elseif( field.isLong    )>int64_t <field.name> = in.getLong();
< elseif( field.isUlong   )>uint64_t <field.name> = in.getULong();
< elseif( field.isFloat   )>float <field.name> = in.getFloat();
< elseif( field.isDouble  )>double <field.name> = in.getDouble();
< elseif( field.isString  )>char <field.name>[<field.length> + 1];
in.getString( <field.name>, sizeof( <field.name> ));
< elseif( field.isEnum    )><types.(field.userType)> <field.name> = in.getByte();
< elseif( field.isStruct  )><types.(field.userType)> <field.name>;
<field.name>.get( in );
< endif>
}>
>>
