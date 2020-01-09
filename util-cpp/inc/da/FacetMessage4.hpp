#pragma once

#include "FacetMessage3.hpp"

namespace da {

   template<class I, class F, class E, class A, class B, class C, class D>
   struct FacetMessage4 : public FacetMessage3<I, F, E, A, B, C> {

      A _arg4;

      FacetMessage4( const sockaddr_in & from, I intrfc, F event, E instance, E fromInstance, A arg1, B arg2, C arg3, D arg4 ) :
         FacetMessage3<I,F,E,A,B,C>( from, intrfc, event, instance, fromInstance, arg1, arg2, arg3 )
      {
         _arg4 = arg4;
      }
   };
}
