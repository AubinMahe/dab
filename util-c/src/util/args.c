#include <util/args.h>

#include <limits.h>
#include <string.h>
#include <stdlib.h>

#define RETURN_IF_ERROR( CALL ) {\
   util_error error_code = CALL;\
   if( error_code ) {\
      return error_code;\
   }\
}

static int pair_cmp( const void * left, const void * right ) {
   const char ** pl = (const char **)left;
   const char ** pr = (const char **)right;
   const char *  sl = *pl;
   const char *  sr = *pr;
   return strcmp( sl, sr );
}

util_error util_args_parse( util_map * map, size_t capacity, util_pair * pairs, int argc, char * argv[] ) {
   if( ! map || ! argv ) {
      return UTIL_NULL_ARG;
   }
   util_map_init( map, pair_cmp, capacity, pairs );
   for( int i = 1; i < argc; ++i ) {
      char * arg = argv[i];
      if(( arg[0] == '-' )&&( arg[1] == '-' )) {
         char * eok = strchr( arg, '=' );
         if( ! eok ) {
            return UTIL_PARSE_ERROR;
         }
         *eok = '\0'; // on modifie la ligne de commande, on remplace '=' par '\0'
         char * key   = arg + 2;
         char * value = eok + 1;
         RETURN_IF_ERROR( util_map_put( map, key, value ))
      }
      else {
         return UTIL_PARSE_ERROR;
      }
   }
   return UTIL_NO_ERROR;
}

util_error util_args_get_bool( const util_map * map, const char * key, bool * target ) {
   if( ! map || ! key || ! target ) {
      return UTIL_NULL_ARG;
   }
   const char * s = 0;
   RETURN_IF_ERROR( util_args_get_string( map, key, &s ))
   *target = (( 0 == strcmp( s, "true" ))
      ||      ( 0 == strcmp( s, "yes"  )));
   return UTIL_NO_ERROR;
}

util_error util_args_get_char( const util_map * map, const char * key, char * target ) {
   if( ! map || ! key || ! target ) {
      return UTIL_NULL_ARG;
   }
   const char * s = 0;
   RETURN_IF_ERROR( util_args_get_string( map, key, &s ))
   *target = *s;
   return UTIL_NO_ERROR;
}

static util_error get_int64( const util_map * map, const char * key, void * target, int64_t min, int64_t max, int64_t * value ) {
   if( ! target ) {
      return UTIL_NULL_ARG;
   }
   const char * s = 0;
   RETURN_IF_ERROR( util_args_get_string( map, key, &s ))
   char * error = 0;
   *value = strtoll( s, &error, 10 );
   if(( error && *error )||( *value < min )||( *value > max )) {
      return UTIL_PARSE_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error util_args_get_byte( const util_map * map, const char * key, byte * target ) {
   int64_t value = 0;
   RETURN_IF_ERROR( get_int64( map, key, target, 0, 255, &value ))
   *target = (byte)value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_short( const util_map * map, const char * key, short * target ) {
   int64_t value = 0;
   RETURN_IF_ERROR( get_int64( map, key, target, SHRT_MIN, SHRT_MAX, &value ))
   *target = (short)value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_ushort( const util_map * map, const char * key, ushort * target ) {
   int64_t value = 0;
   RETURN_IF_ERROR( get_int64( map, key, target, 0, USHRT_MAX, &value ))
   *target = (unsigned short)value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_int( const util_map * map, const char * key, int * target ) {
   int64_t value = 0;
   RETURN_IF_ERROR( get_int64( map, key, target, INT_MIN, INT_MAX, &value ))
   *target = (int)value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_uint( const util_map * map, const char * key, uint * target ) {
   int64_t value = 0;
   RETURN_IF_ERROR( get_int64( map, key, target, 0, UINT_MAX, &value ))
   *target = (uint)value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_long( const util_map * map, const char * key, long * target ) {
   int64_t value = 0;
   RETURN_IF_ERROR( get_int64( map, key, target, LONG_MIN, LONG_MAX, &value ))
   *target = (long)value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_ulong( const util_map * map, const char * key, ulong * target ) {
   const char * s = 0;
   RETURN_IF_ERROR( util_args_get_string( map, key, &s ))
   char * error = 0;
   uint64_t value = strtoull( s, &error, 10 );
   if(( error && *error )||( value > ULONG_MAX )) {
      return UTIL_PARSE_ERROR;
   }
   *target = (ulong)value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_int64( const util_map * map, const char * key, int64_t * target ) {
   int64_t value = 0;
   RETURN_IF_ERROR( get_int64( map, key, target, LLONG_MIN, LLONG_MAX, &value ))
   *target = value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_uint64( const util_map * map, const char * key, uint64_t * target ) {
   const char * s = 0;
   RETURN_IF_ERROR( util_args_get_string( map, key, &s ))
   char * error = 0;
   uint64_t value = strtoull( s, &error, 10 );
   if( error && *error ) {
      return UTIL_PARSE_ERROR;
   }
   *target = value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_float( const util_map * map, const char * key, float * target ) {
   const char * s = 0;
   RETURN_IF_ERROR( util_args_get_string( map, key, &s ))
   char * error = 0;
   float  value = strtof( s, &error );
   if( error && *error ) {
      return UTIL_PARSE_ERROR;
   }
   *target = value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_double( const util_map * map, const char * key, double * target ) {
   const char * s = 0;
   RETURN_IF_ERROR( util_args_get_string( map, key, &s ))
   char * error = 0;
   double value = strtod( s, &error );
   if( error && *error ) {
      return UTIL_PARSE_ERROR;
   }
   *target = value;
   return UTIL_NO_ERROR;
}

util_error util_args_get_string( const util_map * map, const char * key, const char ** target ) {
   if( ! map || ! key || ! target ) {
      return UTIL_NULL_ARG;
   }
   const void * value = 0;
   RETURN_IF_ERROR( util_map_get( map, key, &value ))
   *target = (const char *)value;
   return UTIL_NO_ERROR;
}
