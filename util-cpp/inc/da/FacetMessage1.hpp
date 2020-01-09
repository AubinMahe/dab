#pragma once

#include "FacetMessage.hpp"

namespace da {

   template<class I, class F, class E, class A>
   struct FacetMessage1 : public FacetMessage<I, F, E> {

      A _arg1;

      FacetMessage1( const sockaddr_in & from, I intrfc, F event, E instance, E fromInstance, A arg1 ) :
         FacetMessage<I, F, E>( from, intrfc, event, instance, fromInstance )
      {
         _arg1 = arg1;
      }
   };
}
