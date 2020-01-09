responsesHeader( namespace, ifaceName, requests, types ) ::= <<
#pragma once
// responsesHeader
<requests:{request|
#include \<<types.(request.type); format="NamespaceToPath">.hpp\>
}>
namespace <namespace> {

   class I<ifaceName>Responses {
   public:

      I<ifaceName>Responses( void ) = default;
      virtual ~ I<ifaceName>Responses( void ) = default;

   public:

<requests:{request|
      virtual void <request.name>Response( const <types.(request.type)> & <request.name> ) = 0;
}>
   private:
      I<ifaceName>Responses( const I<ifaceName>Responses & ) = delete;
      I<ifaceName>Responses & operator = ( const I<ifaceName>Responses & ) = delete;
   };
}

>>
