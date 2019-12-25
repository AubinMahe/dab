#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <util/error_codes.h>
#include "../types.h"

typedef enum io_byte_order_e {

   io_byte_order_BIG_ENDIAN,
   io_byte_order_LITTLE_ENDIAN

} io_byte_order;

typedef struct io_byte_buffer_s {

   io_byte_order order;
   size_t        position;
   size_t        limit;
   size_t        capacity;
   size_t        mark;
   byte *        bytes;

} io_byte_buffer;

util_error io_byte_buffer_wrap        ( io_byte_buffer * This, size_t capacity, byte * array );

util_error io_byte_buffer_set_order   ( io_byte_buffer * This, io_byte_order order );
util_error io_byte_buffer_get_order   ( io_byte_buffer * This, io_byte_order * target );
util_error io_byte_buffer_array       ( io_byte_buffer * This, byte ** to );
util_error io_byte_buffer_clear       ( io_byte_buffer * This );
util_error io_byte_buffer_mark        ( io_byte_buffer * This );
util_error io_byte_buffer_reset       ( io_byte_buffer * This );
util_error io_byte_buffer_rewind      ( io_byte_buffer * This );
util_error io_byte_buffer_flip        ( io_byte_buffer * This );
util_error io_byte_buffer_get_position( io_byte_buffer * This, size_t * position );
util_error io_byte_buffer_set_position( io_byte_buffer * This, size_t   position );
util_error io_byte_buffer_get_limit   ( io_byte_buffer * This, size_t * limit );
util_error io_byte_buffer_remaining   ( io_byte_buffer * This, size_t * remaining );
util_error io_byte_buffer_put         ( io_byte_buffer * This, const byte * src, size_t from, size_t to );
util_error io_byte_buffer_get         ( io_byte_buffer * This, byte * target, size_t from, size_t to );
util_error io_byte_buffer_put_byte    ( io_byte_buffer * This, byte value );
util_error io_byte_buffer_get_byte    ( io_byte_buffer * This, byte * target );
util_error io_byte_buffer_put_bool    ( io_byte_buffer * This, bool value );
util_error io_byte_buffer_get_bool    ( io_byte_buffer * This, bool * value );
util_error io_byte_buffer_put_short   ( io_byte_buffer * This, short value );
util_error io_byte_buffer_put_ushort  ( io_byte_buffer * This, unsigned short value );
util_error io_byte_buffer_get_short   ( io_byte_buffer * This, short * target );
util_error io_byte_buffer_get_ushort  ( io_byte_buffer * This, unsigned short * target );
util_error io_byte_buffer_put_int     ( io_byte_buffer * This, int value );
util_error io_byte_buffer_put_uint    ( io_byte_buffer * This, unsigned int value );
util_error io_byte_buffer_put_uintAt  ( io_byte_buffer * This, unsigned int value, size_t index );
util_error io_byte_buffer_get_int     ( io_byte_buffer * This, int * target );
util_error io_byte_buffer_get_uint    ( io_byte_buffer * This, unsigned int * target );
util_error io_byte_buffer_put_long    ( io_byte_buffer * This, int64_t value );
util_error io_byte_buffer_put_ulong   ( io_byte_buffer * This, uint64_t value );
util_error io_byte_buffer_get_long    ( io_byte_buffer * This, int64_t * target );
util_error io_byte_buffer_get_ulong   ( io_byte_buffer * This, uint64_t * target );
util_error io_byte_buffer_put_float   ( io_byte_buffer * This, float value );
util_error io_byte_buffer_get_float   ( io_byte_buffer * This, float * target );
util_error io_byte_buffer_put_double  ( io_byte_buffer * This, double value );
util_error io_byte_buffer_get_double  ( io_byte_buffer * This, double * target );
util_error io_byte_buffer_put_string  ( io_byte_buffer * This, const char * src );
util_error io_byte_buffer_get_string  ( io_byte_buffer * This, char * target, size_t size_of_target );
util_error io_byte_buffer_put_buffer  ( io_byte_buffer * This, io_byte_buffer source );

#ifdef __cplusplus
}
#endif
