#include <io/DatagramSocket.hpp>
#include <io/sockets.hpp>

#include <string.h>

using namespace io;

void DatagramSocket::init( const char * hostnameOrIp, unsigned short port, sockaddr_in & target ) {
   hostent * he = ::gethostbyname( hostnameOrIp );
   if( ! he ) {
      throw os::StdApiException( "DatagramSocket.init", __FILE__, __LINE__ );
   }
   target.sin_family = AF_INET;
   target.sin_port   = htons( port );
   ::memcpy( &target.sin_addr, he->h_addr_list[0], (size_t)he->h_length );
}

DatagramSocket::DatagramSocket( void ) :
   _socket( socket( AF_INET, SOCK_DGRAM, 0 ))
{
   if( _socket == INVALID_SOCKET ) {
      throw os::StdApiException( "DatagramSocket.<ctor>", __FILE__, __LINE__ );
   }
}

DatagramSocket:: ~ DatagramSocket( void ) {
   closesocket( _socket );
}

DatagramSocket & DatagramSocket::bind( const char * intrfc, unsigned short port ) {
   sockaddr_in localAddr;
   ::memset( &localAddr, 0, sizeof( localAddr ));
   localAddr.sin_family = AF_INET;
   ::inet_pton( AF_INET, intrfc, &localAddr.sin_addr.s_addr );
   localAddr.sin_port = htons( port );
   if( ::bind( _socket, (sockaddr *)&localAddr, sizeof( localAddr ))) {
      ::closesocket( _socket );
      throw os::StdApiException( "DatagramSocket.bind", __FILE__, __LINE__ );
   }
   return *this;
}

DatagramSocket & DatagramSocket::connect( const char * hostnameOrIp, unsigned short port ) {
   sockaddr_in target;
   init( hostnameOrIp, port, target );
   if( ::connect( _socket, (sockaddr *)&target, sizeof( target ))) {
      ::closesocket( _socket );
      throw os::StdApiException( "DatagramSocket.connect", __FILE__, __LINE__ );
   }
   return *this;
}

bool DatagramSocket::receive( ByteBuffer & bb ) {
   size_t  max    = bb.limit() - bb.position();
   void *  buffer = bb.array() + bb.position();
   int     flags  = 0;
   ssize_t count  = ::recvfrom( _socket, (char *)buffer, max, flags, 0, 0 );
   if( count < 0 ) {
      throw os::StdApiException( "DatagramSocket.receive", __FILE__, __LINE__ );
   }
   bb.position( bb.position() + (size_t)count );
   return true;
}

bool DatagramSocket::receive( ByteBuffer & bb, sockaddr_in & from ) {
   size_t    max    = bb.limit() - bb.position();
   void *    buffer = bb.array() + bb.position();
   socklen_t len    = sizeof( from );
   int       flags  = 0;
   ssize_t   count  = ::recvfrom( _socket, (char *)buffer, max, flags, (sockaddr *)&from, &len );
   if( count < 0 ) {
      throw os::StdApiException( "DatagramSocket.receive(from)", __FILE__, __LINE__ );
   }
   bb.position( bb.position() + (size_t)count );
   return true;
}

DatagramSocket & DatagramSocket::send( ByteBuffer & bb ) {
   size_t       len    = bb.limit() - bb.position();
   const char * buffer = (const char *)( bb.array() + bb.position());
   ssize_t      count  = ::send( _socket, buffer, len, 0 );
   if( count < 0 || ( len != (size_t)count )) {
      throw os::StdApiException( "DatagramSocket.send", __FILE__, __LINE__ );
   }
   bb.position( bb.position() + (size_t)count );
   return *this;
}

DatagramSocket & DatagramSocket::sendTo( ByteBuffer & bb, struct sockaddr_in & target ) {
   size_t       len    = bb.limit() - bb.position();
   const char * buffer = (const char *)( bb.array() + bb.position());
   ssize_t      count  = ::sendto( _socket, buffer, len, 0, (struct sockaddr *)&target, sizeof( struct sockaddr_in ));
   if( count < 0 || ( len != (size_t)count )) {
      throw os::StdApiException( "DatagramSocket.sendTo", __FILE__, __LINE__ );
   }
   bb.position( bb.position() + (size_t)count );
   return *this;
}
