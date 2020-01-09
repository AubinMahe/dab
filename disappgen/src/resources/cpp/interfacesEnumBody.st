interfacesEnumBody( namespace, interfaces ) ::= <<
#include "Interfaces.hpp"

namespace <namespace> {
   
   const char * toString( const Interfaces & interfaces ) {
      switch( interfaces ) {
      case Interfaces::LOOPBACK: return "LOOPBACK";
      <interfaces:{ifaceName|case Interfaces::<ifaceName; format="ID">: return "<ifaceName; format="ID">";
}>      case Interfaces::LAST: break;
      }
      return "???";
   }
}

>>
