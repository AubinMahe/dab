#pragma once

#include <io/sockets.h>
#include <io/byte_buffer.h>

util_error io_datagram_socket_init   ( const char * addr, ushort port, struct sockaddr_in * target );
util_error io_datagram_socket_bind   ( SOCKET   sckt, const char * address, ushort port );
util_error io_datagram_socket_connect( SOCKET   sckt, const char * address, ushort port );
util_error io_datagram_socket_receive( SOCKET   sckt, io_byte_buffer * buffer, struct sockaddr_in * from );
util_error io_datagram_socket_send   ( SOCKET   sckt, io_byte_buffer * buffer );
util_error io_datagram_socket_sendTo ( SOCKET   sckt, io_byte_buffer * buffer, struct sockaddr_in * target );
util_error io_datagram_socket_close  ( SOCKET   sckt );
