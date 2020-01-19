byteBufferGetToMessage( facetName, fields ) ::= <<
<fields:{field|
<     if( field.isBoolean )>msg._payload.<field.name> = ByteBufferHelper.getBoolean( in );
< elseif( field.isByte    )>msg._payload.<field.name> = in.getByte();
< elseif( field.isShort   )>msg._payload.<field.name> = in.getShort();
< elseif( field.isUshort  )>msg._payload.<field.name> = in.getUShort();
< elseif( field.isInt     )>msg._payload.<field.name> = in.getInt();
< elseif( field.isUint    )>msg._payload.<field.name> = in.getUInt();
< elseif( field.isLong    )>msg._payload.<field.name> = in.getLong();
< elseif( field.isUlong   )>msg._payload.<field.name> = in.getULong();
< elseif( field.isFloat   )>msg._payload.<field.name> = in.getFloat();
< elseif( field.isDouble  )>msg._payload.<field.name> = in.getDouble();
< elseif( field.isString  )>msg._payload.<field.name> = ByteBufferHelper.getString( in );
< elseif( field.isEnum    )>msg._payload.<field.name> = in.getByte();
< elseif( field.isStruct  )>msg._payload.<field.name>.get( in );
< endif>
}>
>>
