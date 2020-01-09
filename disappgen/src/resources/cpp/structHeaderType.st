structHeaderType( field ) ::= <<
< if(     field.isBoolean )>bool<
  elseif( field.isByte    )>byte<
  elseif( field.isShort   )>short<
  elseif( field.isUshort  )>unsigned short<
  elseif( field.isInt     )>int<
  elseif( field.isUint    )>unsigned int<
  elseif( field.isLong    )>int64_t<
  elseif( field.isUlong   )>uint64_t<
  elseif( field.isFloat   )>float<
  elseif( field.isDouble  )>double<
  elseif( field.isString  )>char<
  elseif( field.isEnum    )><types.(field.userType)><
  elseif( field.isStruct  )><types.(field.userType)><
  endif>
>>
