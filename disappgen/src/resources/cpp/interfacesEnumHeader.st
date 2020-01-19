interfacesEnumHeader( namespace, interfaces ) ::= <<
#pragma once
// interfacesEnumHeader

#include \<io/ByteBuffer.hpp>

namespace <namespace> {

   enum class Interfaces : byte {
      FIRST,

      TIMEOUT,
      <interfaces:{ifaceName|<ifaceName; format="ID">,
}>
      LAST
   };
}

const char * toString( <namespace>::Interfaces intrfc );

const char * toString( <namespace>::Interfaces intrfc, byte facet );
>>
