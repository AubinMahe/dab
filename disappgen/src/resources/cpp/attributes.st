attributes( fields, prefix = "" ) ::= <<
<fields:{field|
<     if( field.isBoolean )>bool <prefix><field.name>;
< elseif( field.isByte    )>byte <prefix><field.name>;
< elseif( field.isShort   )>short <prefix><field.name>;
< elseif( field.isUshort  )>unsigned short <prefix><field.name>;
< elseif( field.isInt     )>int <prefix><field.name>;
< elseif( field.isUint    )>unsigned int <prefix><field.name>;
< elseif( field.isLong    )>int64_t <prefix><field.name>;
< elseif( field.isUlong   )>uint64_t <prefix><field.name>;
< elseif( field.isFloat   )>float <prefix><field.name>;
< elseif( field.isDouble  )>double <prefix><field.name>;
< elseif( field.isString  )>char <prefix><field.name>[<field.length>+1];
< elseif( field.isEnum    )><types.(field.userType)> <prefix><field.name>;
< elseif( field.isStruct  )><types.(field.userType)> <prefix><field.name>;
< endif>}>
>>
