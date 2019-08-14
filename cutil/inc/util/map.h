#pragma once

#include "../types.h"
#include "error_codes.h"

typedef int ( * util_map_compare_t )( const void * left, const void * right );

#ifdef UTIL_USE_HEAP

typedef struct util_map_tag { int unused; } util_map_t; // Abstract Data Type, hidden implementation but dynamically allocated

util_error util_map_new   ( util_map_compare_t cmp, size_t initial_capacity, size_t grow, util_map_t ** target );
util_error util_map_delete( util_map_t ** target );
util_error util_map_put   (       util_map_t * map, const void * key, const void *  value );
util_error util_map_get   ( const util_map_t * map, const void * key, const void ** target );
util_error util_map_size  ( const util_map_t * map, size_t * target );

#else

typedef struct util_pair_tag {
   const void * key;
   const void * value;
} util_pair_t;

#define UTIL_MAP_PAIRS_SIZE_MAX 100

typedef struct util_map_tag {
   util_map_compare_t cmp;
   size_t             size;
   util_pair_t        pairs[UTIL_MAP_PAIRS_SIZE_MAX];
} util_map_t;

util_error util_map_init(       util_map_t * This, util_map_compare_t cmp );
util_error util_map_put (       util_map_t * This, const void * key, const void *  value );
util_error util_map_get ( const util_map_t * This, const void * key, const void ** target );
util_error util_map_size( const util_map_t * This, size_t * target );

#endif
