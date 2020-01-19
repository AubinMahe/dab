args( fields, prefix = "" ) ::= <<
< if( fields )> < endif><fields:{field|<prefix><field.name>}; separator=", ">
>>
