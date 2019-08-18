#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include "../types.h"
#include "error_codes.h"

typedef int ( * util_map_compare )( const void * left, const void * right );

typedef struct util_pair_s {

   const void * key;
   const void * value;

} util_pair;

typedef struct util_map_s {

   util_map_compare cmp;
   size_t             capacity;
   size_t             size;
   util_pair *      pairs;

} util_map;

util_error util_map_init(       util_map * This, util_map_compare cmp, size_t capacity, util_pair * pairs );
util_error util_map_put (       util_map * This, const void * key, const void *  value );
util_error util_map_get ( const util_map * This, const void * key, const void ** target );
util_error util_map_size( const util_map * This, size_t * target );

#ifdef __cplusplus
}
#endif
