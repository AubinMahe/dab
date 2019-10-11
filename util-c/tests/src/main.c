#include <types.h>
#include <string.h>

void       util_map_tests    ( bool perf );
void       util_args_tests   ( bool perf );
util_error util_timeout_tests( bool perf );

int main( int argc, char * argv[] ) {
   bool perf = ( argc > 1 )&&( 0 == strcmp( argv[1], "--perf=true" ));
   util_map_tests    ( perf );
   util_args_tests   ( perf );
   util_timeout_tests( perf );
   return 0;
}
