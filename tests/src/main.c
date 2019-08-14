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

#ifdef UTIL_USE_HEAP
#  define MAP map
#else
#  define MAP &map
#endif

static void util_map_tests( void ) {
   printf( "--- util_map ---\n" );
   int error_code;
#ifdef UTIL_USE_HEAP
   util_map_t * map = NULL;
   error_code = util_map_new( pair_cmp, 0, 1, &map );
#else
   util_map_t map;
   error_code = util_map_init( &map, pair_cmp );
#endif
   if( error_code ) {
      printf( "FAIL: util_map_init() returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_init()\n" );
   }
   error_code = util_map_put( MAP, "boolean", "true" );
   if( error_code ) {
      printf( "FAIL: util_map_put( 'boolean' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_put( 'boolean' )\n" );
   }
   error_code = util_map_put( MAP, "byte"   , "123" );
   if( error_code ) {
      printf( "FAIL: util_map_put( 'byte' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_put( 'byte' )\n" );
   }
   const void * s = NULL;
   error_code = util_map_get( MAP, "boolean", &s );
   if( error_code ) {
      printf( "FAIL: util_map_get( 'boolean' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_get( 'boolean' ) = %s\n", (const char *)s );
   }
   error_code = util_map_get( MAP, "byte", &s );
   if( error_code ) {
      printf( "FAIL: util_map_get( 'byte' ) returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_get( 'byte' ) = %s\n", (const char *)s );
   }
   size_t size = 0;
   error_code = util_map_size( MAP, &size );
   if( error_code ) {
      printf( "FAIL: util_map_size() returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_size() = %lu\n", size );
   }
#ifdef UTIL_USE_HEAP
   error_code = util_map_delete( &map );
   if( error_code ) {
      printf( "FAIL: util_map_delete() returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_delete()\n" );
   }
#endif
}

#define TEST(T,F) {\
   T value = 0;\
   error_code = util_args_get_ ## T( MAP, #T, &value );\
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
#ifdef UTIL_USE_HEAP
   util_map_t * map = NULL;
   int error_code = util_args_parse( argc, argv, &map );
#else
   util_map_t map;
   int error_code = util_args_parse( &map, argc, argv );
#endif
   if( error_code ) {
      printf( "FAIL: util_args_new() returns %s (%d)\n", util_error_messages[error_code], error_code );
      return;
   }
   {
      bool value = 0;
#ifdef UTIL_USE_HEAP
      error_code = util_args_get_bool( map, "bool", &value );
#else
      error_code = util_args_get_bool( MAP, "bool", &value );
#endif
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

#ifdef UTIL_USE_HEAP
   error_code = util_map_delete( &map );
   if( error_code ) {
      printf( "FAIL: util_map_delete() returns %s (%d)\n", util_error_messages[error_code], error_code );
   }
   else {
      printf( "PASS: util_map_delete()\n" );
   }
#endif
}

extern char * strdup( const char * src );

int main( void ) {
   char * empty[] = {
      "main.c",
   };
   char * argv[] = {
      strdup( "main.c" ),
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
   util_args_tests( sizeof(empty)/sizeof(empty[0]), empty, true  );
   util_args_tests( sizeof(argv )/sizeof(argv [0]), argv , false );
   for( size_t i = 0; i < sizeof(argv)/sizeof(argv[0]); ++i ) {
      free( argv[i] );
   }
   return 0;
}
