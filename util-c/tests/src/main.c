#include <types.h>
#include <os/errors.h>

#include <string.h>
#include <unistd.h>

void       util_map_tests    ( bool perf );
void       util_args_tests   ( bool perf );
util_error util_timeout_tests( bool perf );

static util_error macros_check( int i, void * argument ) {
   close( 1234 );
   switch( i ) {
   case 1: UTIL_ERROR_CHECK( macros_check( 0, NULL )); break;
   case 2: UTIL_RETURN_ERROR( UTIL_NOT_APPLICABLE ); break;
   case 3: UTIL_CHECK_NON_NULL( argument ); break;
   case 4: UTIL_PRINT_OS_ERROR( "close", UTIL_OS_ERROR, 5 ); break;
   case 5: OS_CHECK( close( 1234 )); break;
   case 6: OS_ERROR_IF( close( 1234 ), EBADF ); break;
   case 7: OS_ASSERT( "close", 0, 8 ); break;
   }
   return UTIL_PARSE_ERROR;
}

int main( int argc, char * argv[] ) {
//   bool perf = ( argc > 1 )&&( 0 == strcmp( argv[1], "--perf=true" ));
   for( int i = 1; i < 10; ++i ) {
      macros_check( i, NULL );
   }
//   util_map_tests    ( perf );
//   util_args_tests   ( perf );
//   util_timeout_tests( perf );
   return 0;
   (void)argc;
   (void)argv;
}
