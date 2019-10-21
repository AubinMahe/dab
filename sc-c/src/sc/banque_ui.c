#include <sc/banque_ui.h>

#include <io/console.h>

#include <time.h>
#include <ctype.h>

#ifndef _WIN32
extern int nanosleep( const struct timespec * requested_time, struct timespec * remaining );
#endif

util_error dab_distributeur_create_ui( sc_banque * This ) {
   console_init();
#ifdef _WIN32
   const DWORD period = 20UL;
#else
   const struct timespec period = { 0, 20*1000000 };
#endif
   business_logic_data * bl = (business_logic_data *)This->user_context;
   int c = 0;
   while( c != 'Q' && ! bl->shutdown ) {
      bl->refresh_needed = false;
      printf( IO_ED IO_HOME );
      printf( "+-------------------------------------------\r\n" );
      printf( "|                   MENU\r\n" );
      printf( "+-------------------------------------------\r\n" );
      printf( "+-------------------------------------------\r\n" );
      printf( "| Q : "IO_BOLD"Quitter"IO_SGR_OFF"\r\n" );
      printf( "+-------------------------------------------\r\n" );
      printf( "                Votre choix : " );
      fflush( stdout );
      while( ! console_kbhit() && ! bl->refresh_needed ) {
#ifdef _WIN32
         Sleep( period );
#else
         nanosleep( &period, NULL );
#endif
      }
      if( console_kbhit()) {
         c = toupper( console_getch());
      }
   }
   return UTIL_NO_ERROR;
}
