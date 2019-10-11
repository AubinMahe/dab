#pragma once

#ifdef _WIN32
#  include <os/win32.hpp>
#else
#  include <netinet/in.h>
   typedef int SOCKET;
#endif

#include <io/ByteBuffer.hpp>

namespace io {

   class DatagramSocket {
   public:

      static void init( const char * hostnameOrIp, unsigned short port, sockaddr_in & target );

   public:

      /**
       *  throws os::StdApiException
       */
      DatagramSocket( void );
      ~ DatagramSocket( void );

   public:

      inline operator SOCKET() const { return _socket; }

      DatagramSocket & bind( const char * intrfc, unsigned short port );

      DatagramSocket & connect( const char * hostnameOrIp, unsigned short port );

      bool receive( ByteBuffer & buffer );

      bool receive( ByteBuffer & buffer, sockaddr_in & from );

      DatagramSocket & send( ByteBuffer & buffer );

      DatagramSocket & sendTo( ByteBuffer & buffer, struct sockaddr_in & target );

   private:

      SOCKET _socket;

   private:
      DatagramSocket( const DatagramSocket & ) = delete;
      DatagramSocket & operator = ( const DatagramSocket & ) = delete;
   };
}
