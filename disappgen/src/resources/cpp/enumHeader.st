enumHeader( namespace, enum ) ::= <<
#pragma once

namespace <namespace> {

   enum class <enum.name> : <enumtype( enum )> {
      FIRST,

< first(enum.literal):{literal|
      <literal.name; format="upper"> = FIRST,}>
< rest(enum.literal) :{literal|
      <literal.name; format="upper">,
}>
      LAST
   };

   const char * toString( const <enum.name> & enm );
}

>>
