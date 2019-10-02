#include <util/map.h>
#include <util/args.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef int64_t         int64;
typedef uint64_t        uint64;
typedef const char *    string;

static int pair_cmp( const void * left, const void * right ) {
   const char ** pl = (const char **)left;
   const char ** pr = (const char **)right;
   const char *  sl = *pl;
   const char *  sr = *pr;
   return strcmp( sl, sr );
}

void util_map_tests( bool perf ) {
   if( perf ) {
      return;
   }
   printf( "--- util_map ---\n" );
   util_pair pairs[14];
   util_map  map;
   util_error  error_code = util_map_init( &map, pair_cmp, ARRAY_SIZE(pairs), pairs );
   if( error_code ) {
      printf( "FAIL: util_map_init() returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_init()\n" );
   }
   error_code = util_map_put( &map, "boolean", "true" );
   if( error_code ) {
      printf( "FAIL: util_map_put( 'boolean' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_put( 'boolean' )\n" );
   }
   error_code = util_map_put( &map, "byte"   , "123" );
   if( error_code ) {
      printf( "FAIL: util_map_put( 'byte' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_put( 'byte' )\n" );
   }
   const void * s = NULL;
   error_code = util_map_get( &map, "boolean", &s );
   if( error_code ) {
      printf( "FAIL: util_map_get( 'boolean' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_get( 'boolean' ) = %s\n", (const char *)s );
   }
   error_code = util_map_get( &map, "byte", &s );
   if( error_code ) {
      printf( "FAIL: util_map_get( 'byte' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_get( 'byte' ) = %s\n", (const char *)s );
   }
   size_t size = 0;
   error_code = util_map_size( &map, &size );
   if( error_code ) {
      printf( "FAIL: util_map_size() returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_size() = %lu\n", size );
   }
}
