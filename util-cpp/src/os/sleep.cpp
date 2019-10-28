#include <os/sleep.hpp>
#ifdef _WIN32
#  include <os/win32.hpp>
#else
#  include <time.h>
   extern int nanosleep( const struct timespec * requested_time, struct timespec * remaining );
#endif

void os::sleep( unsigned milliseconds ) {
#ifdef _WIN32
   Sleep((DWORD)milliseconds );
#else
   struct timespec period = { 0, 0 };
   period.tv_sec  = milliseconds / 1000;
   period.tv_nsec = ( milliseconds % 1000 )*1000000;
   nanosleep( &period, NULL );
#endif
}
