#include <dab/ihm.h>
#include <stdio.h>

enum dab_event_id {
   SET_STATUS = 1,
   SET_SOLDE_CAISSE,
   CONFISQUER_LA_CARTE,
   SHUTDOWN,
};

enum dab_interface {
   IHM_ID = 1,
};

util_error dab_ihm_init( dab_ihm * This, SOCKET socket ) {
   This->socket = socket;
   return io_byte_buffer_wrap( &This->out, 18, This->raw );
}

util_error dab_ihm_set_status( dab_ihm * This, struct sockaddr_in * target, dab_etat etat ) {
   UTIL_ERROR_CHECK( io_byte_buffer_clear   ( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, IHM_ID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, SET_STATUS ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, etat ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_sendTo( This->socket, &This->out, target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_ihm_set_solde_caisse( dab_ihm * This, struct sockaddr_in * target, double solde ) {
   UTIL_ERROR_CHECK( io_byte_buffer_clear   ( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, IHM_ID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, SET_SOLDE_CAISSE ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_double( &This->out, solde ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_sendTo( This->socket, &This->out, target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_ihm_confisquer_la_carte( dab_ihm * This, struct sockaddr_in * target ) {
   UTIL_ERROR_CHECK( io_byte_buffer_clear   ( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, IHM_ID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, CONFISQUER_LA_CARTE ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_sendTo( This->socket, &This->out, target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_ihm_shutdown( dab_ihm * This, struct sockaddr_in * target ) {
   UTIL_ERROR_CHECK( io_byte_buffer_clear   ( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, IHM_ID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, SHUTDOWN ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_sendTo( This->socket, &This->out, target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}
