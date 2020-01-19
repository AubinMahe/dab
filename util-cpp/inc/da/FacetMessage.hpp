#pragma once

#include <da/InstanceID.hpp>
#include <io/sockets.hpp>

#include <type_traits>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

namespace da {

   template<class I>
   struct BaseFacetMessage {

      static_assert(std::is_enum<I>::value,
         "I type parameter of FacetMessage must be a class enum : byte");

      sockaddr_in _from;
      I           _interface;
      byte        _event;
      InstanceID  _instance;
      InstanceID  _fromInstance; // Requester instance ID

   public:

      BaseFacetMessage( const sockaddr_in & from, I intrfc, byte event, const InstanceID & instance, const InstanceID & fromInstance ) :
         _from        ( from ),
         _interface   ( intrfc ),
         _event       ( event ),
         _instance    ( instance ),
         _fromInstance( fromInstance )
      {}

      BaseFacetMessage( void ) = default;
      BaseFacetMessage( const BaseFacetMessage & right ) = default;
      BaseFacetMessage & operator = ( const BaseFacetMessage & right ) = default;
   };

   template<class I, class P>
   struct FacetMessage : public BaseFacetMessage<I> {

      P _payload;

   public:

      FacetMessage( const sockaddr_in & from, I intrfc, byte event, const InstanceID & instance, const InstanceID & fromInstance ) :
         BaseFacetMessage<I>( from, intrfc, event, instance, fromInstance )
      {}

      FacetMessage( void ) = default;
      FacetMessage( const FacetMessage & right ) = default;
      FacetMessage & operator = ( const FacetMessage & right ) = default;
   };
}
