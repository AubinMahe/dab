byteBufferPut( prefix, fields ) ::= <<
<fields:{field|
< if(     field.isBoolean )>_out.putBool( <prefix><field.name> );
< elseif( field.isByte    )>_out.putByte( <prefix><field.name> );
< elseif( field.isShort   )>_out.putShort( <prefix><field.name> );
< elseif( field.isUshort  )>_out.putUShort( <prefix><field.name> );
< elseif( field.isInt     )>_out.putInt( <prefix><field.name> );
< elseif( field.isUint    )>_out.putUInt( <prefix><field.name> );
< elseif( field.isLong    )>_out.putLong( <prefix><field.name> );
< elseif( field.isUlong   )>_out.putULong( <prefix><field.name> );
< elseif( field.isFloat   )>_out.putFloat( <prefix><field.name> );
< elseif( field.isDouble  )>_out.putDouble( <prefix><field.name> );
< elseif( field.isString  )>_out.putString( <prefix><field.name> );
< elseif( field.isEnum    )>_out.putByte( static_cast\<byte>( <prefix><field.name> ));
< elseif( field.isStruct  )><prefix><field.name>.put( _out );
< endif>
}>
>>
