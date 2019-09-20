#pragma once

#include <dab/unite_de_traitement.h>

#include <io/byte_buffer.h>
#include <io/datagram_socket.h>

typedef struct dab_unite_de_traitement_dispatcher_s {
   SOCKET socket;
   dab_unite_de_traitement * listener;
   byte raw[39];
   io_byte_buffer in;
} dab_unite_de_traitement_dispatcher;

util_error dab_unite_de_traitement_dispatcher_init(
   dab_unite_de_traitement_dispatcher * This,
   SOCKET                               socket,
   dab_unite_de_traitement *            listener );
util_error dab_unite_de_traitement_dispatcher_dispatch( dab_unite_de_traitement_dispatcher * This, bool * has_dispatched );
