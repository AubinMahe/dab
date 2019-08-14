#include <util/map.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#ifdef UTIL_USE_HEAP

typedef struct pair_tag {
   const void * key;
   const void * value;
} pair_t;

typedef struct map_tag {
   util_map_compare_t cmp;
   size_t             grow;
   size_t             capacity;
   size_t             size;
   pair_t *           pairs;
} map_t;

util_error util_map_new( util_map_compare_t cmp, size_t initial_capacity, size_t grow, util_map_t ** target ) {
   if( ! cmp || ! target  ) {
      return UTIL_NULL_ARG;
   }
   map_t * This = (map_t *)malloc( sizeof( map_t ));
   if( ! This ) {
      return UTIL_MEMORY_FULL;
   }
   This->cmp      = cmp;
   This->grow     = grow;
   This->capacity = initial_capacity;
   This->size     = 0;
   This->pairs    = (pair_t *)malloc( This->capacity * sizeof( pair_t ));
   if( ! This->pairs ) {
      free( This );
      return UTIL_MEMORY_FULL;
   }
   map_t ** map = (map_t **)target;
   *map = This;
   return UTIL_NO_ERROR;
}

util_error util_map_delete( util_map_t ** target ) {
   if( ! target ) {
      return UTIL_NULL_ARG;
   }
   if( *target ) {
      map_t * This = *((map_t **)target);
      if( ! This ) {
         return UTIL_NULL_ARG;
      }
      free( This->pairs );
      free( This );
      *target = NULL;
   }
   return UTIL_NO_ERROR;
}

static bool map_grow( map_t * This ) {
   size_t capacity = This->capacity + This->grow;
   This->pairs = (pair_t *)realloc( This->pairs, capacity * sizeof( pair_t ));
   if( ! This->pairs ) {
      return false;
   }
   This->capacity = capacity;
   return true;
}

util_error util_map_put( util_map_t * map, const void * key, const void * value ) {
   if( ! map || ! key || ! value ) {
      return UTIL_NULL_ARG;
   }
   map_t * This = (map_t *)map;
   if( This->size == This->capacity ) {
      if( ! map_grow( This )) {
         return UTIL_MEMORY_FULL;
      }
   }
   pair_t * pair = This->pairs + This->size;
   pair->key   = key;
   pair->value = value;
   qsort( This->pairs, ++This->size, sizeof( pair_t ), This->cmp );
   return UTIL_NO_ERROR;
}

util_error util_map_get( const util_map_t * map, const void * key, const void ** target ) {
   if( ! map || ! key || ! target ) {
      return UTIL_NULL_ARG;
   }
   *target = NULL;
   const map_t * This = (const map_t *)map;
   pair_t   pk   = { key, NULL };
   pair_t * pair = (pair_t *)bsearch( &pk, This->pairs, This->size, sizeof( pair_t ), This->cmp );
   if( ! pair ) {
      return UTIL_NOT_FOUND;
   }
   *target = pair->value;
   return UTIL_NO_ERROR;
}

util_error util_map_size( const util_map_t * map, size_t * target ) {
   if( ! map || ! target ) {
      return UTIL_NULL_ARG;
   }
   const map_t * This = (const map_t *)map;
   *target = This->size;
   return UTIL_NO_ERROR;
}

#else

util_error util_map_init( util_map_t * This, util_map_compare_t cmp ) {
   if( ! This || ! cmp ) {
      return UTIL_NULL_ARG;
   }
   memset( This, 0, sizeof( util_map_t ));
   This->cmp = cmp;
   return UTIL_NO_ERROR;
}

util_error util_map_put( util_map_t * This, const void * key, const void * value ) {
   if( ! This || ! key || ! value ) {
      return UTIL_NULL_ARG;
   }
   if( This->size == UTIL_MAP_PAIRS_SIZE_MAX ) {
      return UTIL_MEMORY_FULL;
   }
   util_pair_t * pair = This->pairs + This->size;
   pair->key   = key;
   pair->value = value;
   qsort( This->pairs, ++This->size, sizeof( util_pair_t ), This->cmp );
   return UTIL_NO_ERROR;
}

util_error util_map_get( const util_map_t * This, const void * key, const void ** target ) {
   if( ! This || ! key || ! target ) {
      return UTIL_NULL_ARG;
   }
   *target = NULL;
   util_pair_t   pk   = { key, NULL };
   util_pair_t * pair = (util_pair_t *)bsearch( &pk, This->pairs, This->size, sizeof( util_pair_t ), This->cmp );
   if( ! pair ) {
      return UTIL_NOT_FOUND;
   }
   *target = pair->value;
   return UTIL_NO_ERROR;
}

util_error util_map_size( const util_map_t * This, size_t * target ) {
   if( ! This || ! target ) {
      return UTIL_NULL_ARG;
   }
   *target = This->size;
   return UTIL_NO_ERROR;
}

#endif
