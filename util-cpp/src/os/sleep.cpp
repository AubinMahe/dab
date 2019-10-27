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
   const struct timespec period = { 0, milliseconds*1000000 };
   nanosleep( &period, NULL );
#endif
}
