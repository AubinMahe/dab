#include <dab/site_central.h>
#include <stdio.h>

enum dab_event_id {
   GET_INFORMATIONS = 1,
   INCR_NB_ESSAIS,
   RETRAIT,
   SHUTDOWN,
};

enum dab_interface {
   SITE_CENTRAL_ID = 4,
};

util_error dab_site_central_init( dab_site_central * This, SOCKET socket ) {
   This->socket = socket;
   return io_byte_buffer_wrap( &This->out, 18, This->raw );
}

util_error dab_site_central_get_informations( dab_site_central * This, struct sockaddr_in * target, const char * carteID ) {
   UTIL_ERROR_CHECK( io_byte_buffer_clear   ( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, SITE_CENTRAL_ID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, GET_INFORMATIONS ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_string( &This->out, carteID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_sendTo( This->socket, &This->out, target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_site_central_incr_nb_essais( dab_site_central * This, struct sockaddr_in * target, const char * carteID ) {
   UTIL_ERROR_CHECK( io_byte_buffer_clear   ( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, SITE_CENTRAL_ID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, INCR_NB_ESSAIS ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_string( &This->out, carteID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_sendTo( This->socket, &This->out, target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_site_central_retrait( dab_site_central * This, struct sockaddr_in * target, const char * carteID, double montant ) {
   UTIL_ERROR_CHECK( io_byte_buffer_clear   ( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, SITE_CENTRAL_ID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, RETRAIT ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_string( &This->out, carteID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_double( &This->out, montant ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_sendTo( This->socket, &This->out, target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_site_central_shutdown( dab_site_central * This, struct sockaddr_in * target ) {
   UTIL_ERROR_CHECK( io_byte_buffer_clear   ( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, SITE_CENTRAL_ID ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte( &This->out, SHUTDOWN ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_flip( &This->out ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_datagram_socket_sendTo( This->socket, &This->out, target ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}
