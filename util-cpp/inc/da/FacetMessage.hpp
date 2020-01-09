#pragma once

#include <io/sockets.hpp>

namespace da {

   template<class I, class F, class E>
   struct FacetMessage {

      sockaddr_in _from;
      I           _interface;
      F           _event;
      E           _instance;
      E           _fromInstance; // Requester instance ID

      FacetMessage( const sockaddr_in & from, I intrfc, F event, E instance, E fromInstance ) :
         _from        ( from ),
         _interface   ( intrfc ),
         _event       ( event ),
         _instance    ( instance ),
         _fromInstance( fromInstance )
      {}

      template<class A>
      const A & getArg1( const FacetMessage & msg );

      template<class A, class B>
      const B & getArg2( const FacetMessage & msg );

      template<class A, class B, class C>
      const C & getArg3( const FacetMessage & msg );

      template<class A, class B, class C, class D>
      const D & getArg4( const FacetMessage & msg );
   };
}

#include "FacetMessage4.hpp"

namespace da {

   template<class I, class F, class E>
   template<class A>
   const A & FacetMessage<I,F,E>::getArg1( const FacetMessage & msg ) {
      const FacetMessage1<I, F, E, A> & message = static_cast<FacetMessage1<I, F, E, A> &>( msg );
      return message._arg1;
   }

   template<class I, class F, class E>
   template<class A, class B>
   const B & FacetMessage<I,F,E>::getArg2( const FacetMessage & msg ) {
      const FacetMessage2<I, F, E, A, B> & message = static_cast<FacetMessage2<I, F, E, A, B> &>( msg );
      return message._arg2;
   }

   template<class I, class F, class E>
   template<class A, class B, class C>
   const C & FacetMessage<I,F,E>::getArg3( const FacetMessage & msg ) {
      const FacetMessage3<I, F, E, A, B, C> & message = static_cast<FacetMessage3<I, F, E, A, B, C> &>( msg );
      return message._arg3;
   }

   template<class I, class F, class E>
   template<class A, class B, class C, class D>
   const D & FacetMessage<I,F,E>::getArg4( const FacetMessage & msg ) {
      const FacetMessage4<I, F, E, A, B, C, D> & message = static_cast<FacetMessage4<I, F, E, A, B, C, D> &>( msg );
      return message._arg4;
   }
}
