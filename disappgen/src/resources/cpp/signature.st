signature( fields, withNames = true ) ::= <<
< if( ! fields )>void<
else><fields:{field|<
    if( field.isBoolean )>bool<
elseif( field.isString  )>const char *<
elseif( field.isDouble  )>const double &<
elseif( field.isEnum    )><typesNamespace>::<field.userType.name><
elseif( field.isStruct  )>const <typesNamespace>::<field.userType.name> &<
else                     ><type(field)><
endif>< if( withNames )> <field.name>< endif>}; separator=", "><endif>
>>
