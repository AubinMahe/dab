interfacesEnumHeader( namespace, interfaces ) ::= <<
#include \<io/ByteBuffer.hpp>

namespace <namespace> {

   enum class Interfaces : byte {

      LOOPBACK = 0,
      <interfaces:{ifaceName|<ifaceName; format="ID"> = <interfaces.(ifaceName)>,
}>      LAST
   };

   const char * toString( const Interfaces & interfaces );
}

>>
