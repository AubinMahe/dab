#pragma once

#include <io/datagram_socket.h>

#include <dab/etat.h>

typedef struct dab_ihm_s {
   SOCKET socket;
   byte raw[18];
   io_byte_buffer out;
} dab_ihm;

util_error dab_ihm_init( dab_ihm * This, SOCKET socket );
util_error dab_ihm_set_status( dab_ihm * This, struct sockaddr_in * target, dab_etat etat );
util_error dab_ihm_set_solde_caisse( dab_ihm * This, struct sockaddr_in * target, double solde );
util_error dab_ihm_confisquer_la_carte( dab_ihm * This, struct sockaddr_in * target );
util_error dab_ihm_shutdown( dab_ihm * This, struct sockaddr_in * target );
