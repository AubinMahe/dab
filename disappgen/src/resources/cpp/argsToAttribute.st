argsToAttribute( fields, source ) ::=<<
<fields:{field|<
if( field.isString  )>::strncpy( _<field.name>, <source>.carteID, sizeof( _<field.name> ));<
else                 >_<field.name> = <source>.<field.name>;< endif>}; separator="\n">
>>
