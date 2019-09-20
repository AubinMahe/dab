#pragma once

#include <io/byte_buffer.h>

typedef struct dab_compte_s {
   char id[5];
   double solde;
   bool autorise;
} dab_compte;

util_error dab_compte_put( dab_compte * This, io_byte_buffer * target );
util_error dab_compte_get( dab_compte * This, io_byte_buffer * source );
