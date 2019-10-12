#include <util/Exceptions.hpp>
#include <stdio.h>
#include <string.h>

#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  include <io.h>
#  define close _close
#else
#  include <unistd.h>
#endif

class C1 {
public:

   C1() : _i( 1234 ) {}

public:

   void thisMethodRaiseUnexpected( void ) {
      throw util::Unexpected( UTIL_CTXT, "C'est pas cool, i = %d !", _i );
   }

   void thisMethodRaiseOutOfRange( void ) {
      throw util::OutOfRange( UTIL_CTXT, "i(%d) n'est pas dans l'intervale [1..5]", _i );
   }

   void thisMethodRaiseRuntime( void ) {
      if( close( 1234 )) {
         throw util::Runtime( UTIL_CTXT, "close" );
      }
   }

   void thisMethodRaiseNullArg( void ) {
      void * p = 0;
      util::nullCheck( UTIL_CTXT, p, "p" );
   }

private:

   int _i;
};

void timeoutTests( bool perf );

int main( int argc, char * argv[] ) {
   bool perf = ( argc > 1 )&&( 0 == strcmp( argv[1], "--perf=true" ));
   try {
      C1().thisMethodRaiseUnexpected();
   }
   catch( const util::Exception & x ) {
      fprintf( stderr, "%s\n", x.what());
   }
   try {
      C1().thisMethodRaiseOutOfRange();
   }
   catch( const util::Exception & x ) {
      fprintf( stderr, "%s\n", x.what());
   }
   try {
      C1().thisMethodRaiseRuntime();
   }
   catch( const util::Exception & x ) {
      fprintf( stderr, "%s\n", x.what());
   }
   try {
      C1().thisMethodRaiseNullArg();
   }
   catch( const util::Exception & x ) {
      fprintf( stderr, "%s\n", x.what());
   }
   fprintf( stderr, "%s\n",
      util::Runtime( UTIL_CTXT, "printing test" ).what());
   timeoutTests( perf );
}
