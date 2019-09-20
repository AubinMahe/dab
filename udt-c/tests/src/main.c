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

static void util_map_tests( void ) {
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

#define TEST(T,F) {\
   T value = 0;\
   error_code = util_args_get_ ## T( &map, #T, &value );\
   if( error_code ) {\
      if( empty &&( error_code == UTIL_NOT_FOUND )) {\
         printf( "PASS: util_args_get_" #T "( '" #T "' ) returns UTIL_NOT_FOUND\n" );\
      }\
      else {\
         printf( "FAIL: util_args_get_" #T "( '" #T "' ) returns %s (%d)\n", util_error_messages[error_code], error_code );\
      }\
   }\
   else {\
      printf( "PASS: util_args_get_" #T "( '" #T "' ) = " F "\n", value );\
   }\
}

static void util_args_tests( int argc, char * argv[], bool empty ) {
   printf( "--- util_args ---\n" );
   util_pair pairs[14];
   util_map  map;
   util_error  error_code = util_args_parse( &map, ARRAY_SIZE(pairs), pairs, argc, argv );
   if( error_code ) {
      printf( "FAIL: util_args_new() returns %s (%d)\n", util_error_messages[error_code], error_code );
      return;
   }
   {
      bool value = 0;
      error_code = util_args_get_bool( &map, "bool", &value );
      if( error_code ) {
         if( empty &&( error_code == UTIL_NOT_FOUND )) {
            printf( "PASS: util_args_get_bool( 'bool' ) returns UTIL_NOT_FOUND\n" );
         }
         else {
            printf( "FAIL: util_args_get_bool( 'bool' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
         }
      }
      else {
         printf( "PASS: util_args_get_bool( 'bool' ) = %s\n", value ? "true" : "false" );
      }
   }
   TEST( byte  , "%d" )
   TEST( short , "%d" )
   TEST( ushort, "%u" )
   TEST( int   , "%d" )
   TEST( uint  , "%u" )
   TEST( long  , "%ld" )
   TEST( ulong , "%lu" )
   TEST( int64 , "%"PRId64 )
   TEST( uint64, "%"PRIu64 )
   TEST( float , "%8.4f" )
   TEST( double, "%10.7f" )
   TEST( string, "%s" )
}

extern char * strdup( const char * src );

int main( void ) {
   char * empty[] = {
      "main.c",
   };
   char * argv[] = {
      "main.c",
      strdup( "--bool=true" ),
      strdup( "--byte=1" ),
      strdup( "--short=2" ),
      strdup( "--ushort=3" ),
      strdup( "--int=4" ),
      strdup( "--uint=5" ),
      strdup( "--long=6" ),
      strdup( "--ulong=7" ),
      strdup( "--int64=8" ),
      strdup( "--uint64=9" ),
      strdup( "--float=123.456" ),
      strdup( "--double=789.123456" ),
      strdup( "--string=Une belle phrase est composé d'un sujet, d'un verbe et d'un complément." )
   };

   util_map_tests();
   util_args_tests( ARRAY_SIZE(empty), empty, true  );
   util_args_tests( ARRAY_SIZE(argv ), argv , false );
   for( size_t i = 1; i < ARRAY_SIZE(argv); ++i ) {
      free( argv[i] );
   }
   return 0;
}
