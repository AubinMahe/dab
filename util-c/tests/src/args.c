#include <util/map.h>
#include <util/args.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef int64_t         int64;
typedef uint64_t        uint64;
typedef const char *    string;

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

static void test( int argc, char * argv[], bool empty ) {
   printf( "--- util_args ---\n" );
   util_pair  pairs[14];
   util_map   map;
   util_error error_code = util_args_parse( &map, ARRAY_SIZE(pairs), pairs, argc, argv );
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

extern char *strdup( const char *s );

void util_args_tests( bool perf ) {
   if( perf ) {
      return;
   }
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

   test( ARRAY_SIZE(empty), empty, true  );
   test( ARRAY_SIZE(argv ), argv , false );
   for( size_t i = 1; i < ARRAY_SIZE(argv); ++i ) {
      free( argv[i] );
   }
}
