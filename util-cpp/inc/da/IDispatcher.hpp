#pragma once

#include <io/DatagramSocket.hpp>

namespace da {

   class IDispatcher {
   public:

      IDispatcher( bool & running ) : _running( running ) {}

      virtual ~ IDispatcher( void ) = default;

   public:

      bool isRunning( void ) const { return _running; }

   public:

      virtual void beforeDispatch( void ) = 0;

      virtual bool hasDispatched( byte intrfc, byte event, sockaddr_in & from, io::ByteBuffer & in ) = 0;

      virtual void afterDispatch( bool dispatched ) = 0;

   protected:

      bool & _running;

   private:

      IDispatcher( const IDispatcher & ) = delete;
      IDispatcher & operator = ( const IDispatcher & ) = delete;
   };
}
