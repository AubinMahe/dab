#include <io/Console.hpp>

using namespace io;

#ifdef _WIN32
#include <conio.h>

const Console & Console::getConsole() {
   static Console theConsole;
   return theConsole;
}

Console::Console( void ) {
   /**/
}

bool Console::kbhit( void ) const {
   return kbhit();
}

int Console::getch( void ) const {
   return getch();
}

#else

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <termios.h>
#include <unistd.h>
#include <time.h>
#include <sys/time.h>

extern void cfmakeraw( struct termios * termios_p );

static struct termios orig_termios;

static void reset_terminal_mode( void ) {
   tcsetattr( 0, TCSANOW, &orig_termios );
}

const Console & Console::getConsole() {
   static Console theConsole;
   return theConsole;
}

Console::Console( void ) {
   static struct termios new_termios;

   /* take two copies - one for now, one for later */
   tcgetattr( 0, &orig_termios );
   memcpy( &new_termios, &orig_termios, sizeof( new_termios ));

   /* register cleanup handler, and set the new terminal mode */
   atexit( reset_terminal_mode );
   cfmakeraw( &new_termios );
   tcsetattr( 0, TCSANOW, &new_termios );
}

bool Console::kbhit( void ) const {
   struct timeval tv = { 0L, 0L };
   fd_set fds;
   FD_ZERO( &fds );
   FD_SET( 0, &fds );
   int status = select( 1, &fds, NULL, NULL, &tv );
   if( status < 0 ) {
      perror( "select" );
   }
   return status;
}

int Console::getch( void ) const {
   unsigned char c;
   ssize_t status = read( 0, &c, sizeof( c ));
   if( status < 0 ) {
      perror( "read" );
      return 0;
   }
   return c;
}
#endif
