#include <io/console.h>

#ifdef _WIN32
#include <conio.h>

void console_init( void ) {
   /**/
}

int console_kbhit( void ) {
   return kbhit();
}

int console_getch( void ) {
   return getch();
}
#else
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

void console_init( void ) {
   static struct termios new_termios;

   /* take two copies - one for now, one for later */
   tcgetattr( 0, &orig_termios );
   memcpy( &new_termios, &orig_termios, sizeof( new_termios ));

   /* register cleanup handler, and set the new terminal mode */
   atexit( reset_terminal_mode );
   cfmakeraw( &new_termios );
   tcsetattr( 0, TCSANOW, &new_termios );
}

int console_kbhit( void ) {
   struct timeval tv = { 0L, 0L };
   fd_set fds;
   FD_ZERO(&fds);
   FD_SET(0, &fds);
   return select(1, &fds, NULL, NULL, &tv);
}

int console_getch( void ) {
   ssize_t       r;
   unsigned char c;
   if(( r = read( 0, &c, sizeof( c ))) < 0 ) {
      return (int)r;
   }
   return c;
}
#endif
