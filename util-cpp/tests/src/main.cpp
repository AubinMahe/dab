#include <types.hpp>

#include <string.h>

void timeoutTests( bool perf );
void exceptionsTests( void );
int facetMessagesQueueTests( void );

int main( int argc, char * argv[] ) {
   bool perf = ( argc > 1 )&&( 0 == strcmp( argv[1], "--perf=true" ));
//   timeoutTests( perf );
//   exceptionsTests();
   return facetMessagesQueueTests();
   (void)perf;
   (void)argc;
   (void)argv;
}
