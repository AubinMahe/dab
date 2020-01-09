structHeader( namespace, struct, types ) ::= <<
#pragma once

#include \<io/ByteBuffer.hpp>

<struct.field:{field|<
if( field.isStruct || field.isEnum )>
#include \<<types.(field.userType); format="NamespaceToPath">.hpp><
endif>}>

namespace <namespace> {

   struct <struct.name> {

<struct.field:{field|<
if( field.isString )>
      char <field.name>[<field.length> + 1];<
else>
      <structHeaderType(field)> <field.name>;<
endif>}>

      void put( io::ByteBuffer & target ) const;
      void get( io::ByteBuffer & source );
   };
}

>>
