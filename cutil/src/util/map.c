#include <util/map.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

util_error util_map_init( util_map * This, util_map_compare cmp, size_t capacity, util_pair * pairs ) {
   if( ! This || ! cmp || ! pairs ) {
      return UTIL_NULL_ARG;
   }
   This->cmp      = cmp;
   This->capacity = capacity;
   This->size     = 0;
   This->pairs    = pairs;
   return UTIL_NO_ERROR;
}

util_error util_map_put( util_map * This, const void * key, const void * value ) {
   if( ! This || ! key || ! value ) {
      return UTIL_NULL_ARG;
   }
   if( This->size == This->capacity ) {
      return UTIL_MEMORY_FULL;
   }
   util_pair * pair = This->pairs + This->size;
   pair->key   = key;
   pair->value = value;
   qsort( This->pairs, ++This->size, sizeof( util_pair ), This->cmp );
   return UTIL_NO_ERROR;
}

util_error util_map_get( const util_map * This, const void * key, const void ** target ) {
   if( ! This || ! key || ! target ) {
      return UTIL_NULL_ARG;
   }
   *target = NULL;
   util_pair   pk   = { key, NULL };
   util_pair * pair = (util_pair *)bsearch( &pk, This->pairs, This->size, sizeof( util_pair ), This->cmp );
   if( ! pair ) {
      return UTIL_NOT_FOUND;
   }
   *target = pair->value;
   return UTIL_NO_ERROR;
}

util_error util_map_size( const util_map * This, size_t * target ) {
   if( ! This || ! target ) {
      return UTIL_NULL_ARG;
   }
   *target = This->size;
   return UTIL_NO_ERROR;
}
