#include <io/datagram_socket.h>
#include <io/sockets.h>

#include <string.h>

util_error io_datagram_socket_init( const char * addr, ushort port, struct sockaddr_in * target ) {
   if( ! addr || ! target ) {
      return UTIL_NULL_ARG;
   }
   struct hostent * he = gethostbyname( addr );
   if( ! he ) {
      return UTIL_OS_ERROR;
   }
   target->sin_family = AF_INET;
   target->sin_port   = htons( port );
   memcpy( &target->sin_addr, he->h_addr_list[0], (size_t)he->h_length );
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_bind( SOCKET sckt, const char * intrfc, ushort port ) {
   if( ! sckt || ! intrfc ) {
      return UTIL_NULL_ARG;
   }
   struct sockaddr_in localAddr;
   memset( &localAddr, 0, sizeof( localAddr ));
   localAddr.sin_family = AF_INET;
   inet_pton( AF_INET, intrfc, &localAddr.sin_addr.s_addr );
   localAddr.sin_port = htons( port );
   if( bind( sckt, (struct sockaddr *)&localAddr, sizeof( localAddr ))) {
      closesocket( sckt );
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_connect( SOCKET sckt, const char * addr, ushort port ) {
   if( ! sckt || ! addr ) {
      return UTIL_NULL_ARG;
   }
   struct hostent * he = gethostbyname( addr );
   if( ! he ) {
      return UTIL_OS_ERROR;
   }
   struct sockaddr_in target;
   target.sin_family = AF_INET;
   target.sin_port   = htons( port );
   memcpy( &target.sin_addr, he->h_addr_list[0], (size_t)he->h_length );
   if( connect( sckt, (struct sockaddr *)&target, sizeof( target ))) {
      closesocket( sckt );
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_receive( SOCKET sckt, io_byte_buffer * bb, struct sockaddr_in * from ) {
   if( ! sckt || ! bb ) {
      return UTIL_NULL_ARG;
   }
   size_t    max    = bb->limit - bb->position;
   byte *    buffer = bb->bytes + bb->position;
   int       flags  = 0;
   socklen_t len    = from ? sizeof( from ) : 0;
   ssize_t   count  = recvfrom( sckt, (char *)buffer, max, flags, (struct sockaddr *)from, &len );
   if( count < 0 ) {
      return UTIL_OS_ERROR;
   }
   bb->position += (size_t)count;
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_send( SOCKET sckt, io_byte_buffer * bb ) {
   if( ! sckt || ! bb ) {
      return UTIL_NULL_ARG;
   }
   size_t       len    = bb->limit - bb->position;
   const char * buffer = (const char *)( bb->bytes + bb->position );
   ssize_t      count  = send( sckt, buffer, len, 0 );
   if( count < 0 || ( len != (size_t)count )) {
      return UTIL_OS_ERROR;
   }
   bb->position += (size_t)count;
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_sendTo( SOCKET sckt, io_byte_buffer * bb, struct sockaddr_in * target ) {
   if( ! sckt || ! bb || ! target ) {
      return UTIL_NULL_ARG;
   }
   size_t       len    = bb->limit - bb->position;
   const char * buffer = (const char *)( bb->bytes + bb->position );
   ssize_t      count  = sendto( sckt, buffer, len, 0, (struct sockaddr *)target, sizeof( struct sockaddr_in ));
   if( count < 0 || ( len != (size_t)count )) {
      return UTIL_OS_ERROR;
   }
   bb->position += (size_t)count;
   return UTIL_NO_ERROR;
}

util_error io_datagram_socket_close( SOCKET sckt ) {
   if( ! sckt ) {
      return UTIL_NULL_ARG;
   }
   if( closesocket( sckt )) {
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}
