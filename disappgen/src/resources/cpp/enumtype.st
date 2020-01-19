enumtype( field ) ::= <<
<     if( field.isBoolean )>bool<
  elseif( field.isByte    )>unsigned char<
  elseif( field.isShort   )>short<
  elseif( field.isUshort  )>unsigned short<
  elseif( field.isInt     )>int<
  elseif( field.isUint    )>unsigned<
  elseif( field.isLong    )>int64_t<
  elseif( field.isUlong   )>uint64_t<
  endif>
>>
