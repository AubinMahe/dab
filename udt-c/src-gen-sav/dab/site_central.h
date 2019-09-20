#pragma once

#include <io/datagram_socket.h>

typedef struct dab_site_central_s {
   SOCKET socket;
   byte raw[18];
   io_byte_buffer out;
} dab_site_central;

util_error dab_site_central_init( dab_site_central * This, SOCKET socket );
util_error dab_site_central_get_informations( dab_site_central * This, struct sockaddr_in * target, const char * carteID );
util_error dab_site_central_incr_nb_essais( dab_site_central * This, struct sockaddr_in * target, const char * carteID );
util_error dab_site_central_retrait( dab_site_central * This, struct sockaddr_in * target, const char * carteID, double montant );
util_error dab_site_central_shutdown( dab_site_central * This, struct sockaddr_in * target );
