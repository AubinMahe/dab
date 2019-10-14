#include <io/datagram_socket.h>
#include <os/errors.h>

#include <string.h>

util_error io_datagram_socket_init( const char * addr, ushort port, struct sockaddr_in * target ) {
   UTIL_CHECK_NON_NULL( addr );
   UTIL_CHECK_NON_NULL( target );
   struct hostent * he = gethostbyname( addr );
   OS_ASSERT( "gethostbyname", he, 1 );
   target->sin_family = AF_INET;
   target->sin_port   = htons( port );
   memcpy( &target->sin_addr, he->h_addr_list[0], (size_t)he->h_length );
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_bind( SOCKET sckt, const char * address, ushort port, struct sockaddr_in * localAddr ) {
   UTIL_ASSERT( sckt > 0 );
   UTIL_CHECK_NON_NULL( address );
   UTIL_CHECK_NON_NULL( localAddr );
   memset( localAddr, 0, sizeof( struct sockaddr_in ));
   localAddr->sin_family = AF_INET;
   OS_ERROR_IF( inet_pton( AF_INET, address, &localAddr->sin_addr.s_addr ), 0 );
   localAddr->sin_port = htons( port );
   if( bind( sckt, (struct sockaddr *)localAddr, sizeof( struct sockaddr_in ))) {
      OS_ERROR_PRINT( "bind", 1 );
      closesocket( sckt );
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_connect( SOCKET sckt, const char * address, ushort port ) {
   UTIL_ASSERT( sckt > 0 );
   UTIL_CHECK_NON_NULL( address );
   struct hostent * he = gethostbyname( address );
   OS_ASSERT( "gethostbyname", he, 1 );
   struct sockaddr_in target;
   target.sin_family = AF_INET;
   target.sin_port   = htons( port );
   memcpy( &target.sin_addr, he->h_addr_list[0], (size_t)he->h_length );
   if( connect( sckt, (struct sockaddr *)&target, sizeof( target ))) {
      OS_ERROR_PRINT( "connect", 1 );
      closesocket( sckt );
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_receive( SOCKET sckt, io_byte_buffer * bb, struct sockaddr_in * from ) {
   UTIL_ASSERT( sckt > 0 );
   UTIL_CHECK_NON_NULL( bb );
   size_t    max    = bb->limit - bb->position;
   byte *    buffer = bb->bytes + bb->position;
   int       flags  = 0;
   socklen_t len    = from ? sizeof( from ) : 0;
#ifdef _WIN32
   ssize_t   count  = recvfrom( sckt, (char *)buffer, (int)max, flags, (struct sockaddr *)from, &len );
   OS_ASSERT( "recvfrom", count >= 0, 1 );
#else
   ssize_t   count  = recvfrom( sckt, (char *)buffer, max, flags, (struct sockaddr *)from, &len );
   OS_ASSERT( "recvfrom", count >= 0, 1 );
#endif
   bb->position += (size_t)count;
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_send( SOCKET sckt, io_byte_buffer * bb ) {
   UTIL_ASSERT( sckt > 0 );
   UTIL_CHECK_NON_NULL( bb );
   size_t       len    = bb->limit - bb->position;
   const char * buffer = (const char *)( bb->bytes + bb->position );
#ifdef _WIN32
   ssize_t      count  = send( sckt, buffer, (int)len, 0 );
   OS_ASSERT( "send",( count >= 0 )&&( len == (size_t)count ), 1 );
#else
   ssize_t      count  = send( sckt, buffer, len, 0 );
   OS_ASSERT( "send",( count >= 0 )&&( len == (size_t)count ), 1 );
#endif
   bb->position += (size_t)count;
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_send_to( SOCKET sckt, io_byte_buffer * bb, struct sockaddr_in * target ) {
   UTIL_ASSERT( sckt > 0 );
   UTIL_CHECK_NON_NULL( bb );
   UTIL_CHECK_NON_NULL( target );
   size_t       len    = bb->limit - bb->position;
   const char * buffer = (const char *)( bb->bytes + bb->position );
#ifdef _WIN32
   ssize_t      count  = sendto( sckt, buffer, (int)len, 0, (struct sockaddr *)target, sizeof( struct sockaddr_in ));
   OS_ASSERT( "send",( count >= 0 )&&( len == (size_t)count ), 1 );
#else
   ssize_t      count  = sendto( sckt, buffer, len, 0, (struct sockaddr *)target, sizeof( struct sockaddr_in ));
   OS_ASSERT( "send",( count >= 0 )&&( len == (size_t)count ), 1 );
#endif
   bb->position += (size_t)count;
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_close( SOCKET sckt ) {
   UTIL_ASSERT( sckt > 0 );
   OS_CHECK( closesocket( sckt ));
   return UTIL_NO_ERROR;
}
