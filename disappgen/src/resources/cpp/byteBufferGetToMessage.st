byteBufferGetToMessage( facetName, fields ) ::= <<
<fields:{field|
<     if( field.isBoolean )>msg._payload.<facetName>.<field.name> = in.getBool();
< elseif( field.isByte    )>msg._payload.<facetName>.<field.name> = in.getByte();
< elseif( field.isShort   )>msg._payload.<facetName>.<field.name> = in.getShort();
< elseif( field.isUshort  )>msg._payload.<facetName>.<field.name> = in.getUShort();
< elseif( field.isInt     )>msg._payload.<facetName>.<field.name> = in.getInt();
< elseif( field.isUint    )>msg._payload.<facetName>.<field.name> = in.getUInt();
< elseif( field.isLong    )>msg._payload.<facetName>.<field.name> = in.getLong();
< elseif( field.isUlong   )>msg._payload.<facetName>.<field.name> = in.getULong();
< elseif( field.isFloat   )>msg._payload.<facetName>.<field.name> = in.getFloat();
< elseif( field.isDouble  )>msg._payload.<facetName>.<field.name> = in.getDouble();
< elseif( field.isString  )>in.getString( msg._payload.<facetName>.<field.name>, <field.length>+1 );
< elseif( field.isEnum    )>msg._payload.<facetName>.<field.name> = in.getByte();
< elseif( field.isStruct  )>msg._payload.<facetName>.<field.name>.get( in );
< endif>
}>
>>
