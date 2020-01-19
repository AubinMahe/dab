offeredInterfaceHeader( namespace, ifaceName, usedTypes, eventsOrRequests, types ) ::= <<
#pragma once
//offeredInterface

#include \<types.hpp>

<usedTypes:{type|
#include \<<types.(type); format="NamespaceToPath">.hpp>
}>
namespace <namespace> {

   class I<ifaceName> {
   public:

      I<ifaceName>( void ) = default;
      virtual ~ I<ifaceName>( void ) = default;

   public:

<eventsOrRequests:{facet|< if( facet.isData )>< else>
      virtual void <facet.name>( <
if( facet.isRequest )><
         signature( facet.arguments.field )>, <types.(facet.type)> & <facet.name><
else><
         signature( facet.field )><
endif> ) = 0;< endif>
}>

   private:
      I<ifaceName>( const I<ifaceName> & ) = delete;
      I<ifaceName> & operator = ( const I<ifaceName> & ) = delete;
   };
}

>>
