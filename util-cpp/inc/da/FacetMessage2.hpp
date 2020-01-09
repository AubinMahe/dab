#pragma once

#include "FacetMessage1.hpp"

namespace da {

   template<class I, class F, class E, class A, class B>
   struct FacetMessage2 : public FacetMessage1<I, F, E, A> {

      B _arg2;

      FacetMessage2( const sockaddr_in & from, I intrfc, F event, E instance, E fromInstance, A arg1, B arg2 ) :
         FacetMessage1<I,F,E,A>( from, intrfc, event, instance, fromInstance, arg1 )
      {
         _arg2 = arg2;
      }
   };
}
