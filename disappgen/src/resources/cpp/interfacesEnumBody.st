interfacesEnumBody( namespace, interfaces ) ::= <<
#include "Interfaces.hpp"
#include "TimeoutInterface.hpp"
<interfaces:{ifaceName|#include "<ifaceName>Interface.hpp"
}>
using namespace <namespace>;
   
const char * toString( Interfaces interfaces ) {
   switch( interfaces ) {
   case Interfaces::TIMEOUT: return "TIMEOUT";
   <interfaces:{ifaceName|case Interfaces::<ifaceName; format="ID">: return "<ifaceName; format="ID">";
}>   default:
   case Interfaces::FIRST:
   case Interfaces::LAST:
      break;
   }
   return "???";
}

const char * toString( Interfaces intrfc, byte facet ) {
   switch( intrfc ) {
   case Interfaces::TIMEOUT: return TimeoutInterface::toString( facet );
   <interfaces:{ifaceName|case Interfaces::<ifaceName; format="ID">: return <ifaceName>Interface::toString( facet );
}>   default:
   case Interfaces::FIRST:
   case Interfaces::LAST:
      break;
   }
   return "???";
}

>>
