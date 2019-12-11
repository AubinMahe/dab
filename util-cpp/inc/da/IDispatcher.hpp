#pragma once

#include <io/DatagramSocket.hpp>

namespace da {

   class IDispatcher {
   public:

      IDispatcher( void ) = default;

      virtual ~ IDispatcher( void ) = default;

   public:

      virtual void beforeDispatch( void ) = 0;

      virtual bool hasDispatched( byte intrfc, byte event, sockaddr_in & from, io::ByteBuffer & in ) = 0;

      virtual void afterDispatch( bool dispatched ) = 0;

   private:

      IDispatcher( const IDispatcher & ) = delete;
      IDispatcher & operator = ( const IDispatcher & ) = delete;
   };
}
