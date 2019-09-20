#pragma once

#include <io/byte_buffer.h>

typedef struct dab_carte_s {
   char id[5];
   char code[5];
   byte month;
   ushort year;
   byte nb_essais;
} dab_carte;

util_error dab_carte_put( dab_carte * This, io_byte_buffer * target );
util_error dab_carte_get( dab_carte * This, io_byte_buffer * source );
