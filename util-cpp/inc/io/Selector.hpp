#pragma once

#include <io/sockets.hpp>

#include <initializer_list>

namespace io {

   class Selector {
   public:

      Selector( std::initializer_list<SOCKET> sockets );

   public:

      bool select( unsigned timeout = -1U );

      bool isSet( SOCKET socket );

   private:

      fd_set _ensemble;

   private:
      Selector( const Selector & ) = delete;
      Selector & operator = ( const Selector & ) = delete;
   };
}
