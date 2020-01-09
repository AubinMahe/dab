#pragma once

#include "FacetMessage2.hpp"

namespace da {

   template<class I, class F, class E, class A, class B, class C>
   struct FacetMessage3 : public FacetMessage2<I, F, E, A, B> {

      A _arg3;

      FacetMessage3( const sockaddr_in & from, I intrfc, F event, E instance, E fromInstance, A arg1, B arg2, C arg3 ) :
         FacetMessage2<I,F,E,A,B>( from, intrfc, event, instance, fromInstance, arg1, arg2 )
      {
         _arg3 = arg3;
      }
   };
}
