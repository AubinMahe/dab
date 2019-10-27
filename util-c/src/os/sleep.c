#include <os/sleep.h>
#ifdef _WIN32
#  include <os/win32.h>
#else
#  include <time.h>
   extern int nanosleep( const struct timespec * requested_time, struct timespec * remaining );
#endif

void os_sleep( unsigned milliseconds ) {
#ifdef _WIN32
   Sleep((DWORD)milliseconds );
#else
   const struct timespec period = { 0, milliseconds*1000000 };
   nanosleep( &period, NULL );
#endif
}
