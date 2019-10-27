#include <io/console.h>

#ifdef _WIN32
#include <conio.h>

void io_console_init( void ) {
   /**/
}

int io_console_kbhit( void ) {
   return kbhit();
}

int io_console_getch( void ) {
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

void io_console_init( void ) {
   static struct termios new_termios;

   /* take two copies - one for now, one for later */
   tcgetattr( 0, &orig_termios );
   memcpy( &new_termios, &orig_termios, sizeof( new_termios ));

   /* register cleanup handler, and set the new terminal mode */
   atexit( reset_terminal_mode );
   cfmakeraw( &new_termios );
   tcsetattr( 0, TCSANOW, &new_termios );
}

int io_console_kbhit( void ) {
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

int io_console_getch( void ) {
   unsigned char c;
   ssize_t status = read( 0, &c, sizeof( c ));
   if( status < 0 ) {
      perror( "read" );
      return 0;
   }
   return c;
}
#endif
