#include <SC/banque_ui.h>
#include <SC/repository.h>

#include <io/console.h>
#include <os/sleep.h>
#include <util/log.h>

#include <time.h>
#include <ctype.h>

util_error SC_banque_create_ui( SC_banque * This ) {
   UTIL_LOG_HERE();
   UTIL_CHECK_NON_NULL( This );
   UTIL_CHECK_NON_NULL( This->dispatcher );
   UTIL_CHECK_NON_NULL( This->user_context );
   io_console_init();
   SC_repository * repository = (SC_repository *)This->user_context;
   int c = 0;
   while( c != 'Q' && *This->dispatcher->running ) {
      printf( IO_ED IO_HOME );
      printf( "+------------------------------------------+\r\n" );
      printf( "|                  Cartes                  |\r\n" );
      printf( "+------+------+------+-------+-------------+\r\n" );
      printf( "|  id  | code | mois | année | nbr d'essai |\r\n" );
      printf( "+------+------+------+-------+-------------+\r\n" );
      for( unsigned i = 0; i < sizeof( repository->cartes )/sizeof( repository->cartes[0]); ++i ) {
         printf( "| %4s | %4s | %4d | %5d | %11d |\r\n",
            repository->cartes[i].id,
            repository->cartes[i].code,
            repository->cartes[i].month,
            repository->cartes[i].year,
            repository->cartes[i].nb_essais );
      }
      printf( "+------+------+------+-------+-------------+\r\n" );
      printf( "\r\n" );
      printf( "+---------------------------+\r\n" );
      printf( "|          Comptes          |\r\n" );
      printf( "+------+---------+----------+\r\n" );
      printf( "|  id  |  solde  | autorisé |\r\n" );
      printf( "+------+---------+----------+\r\n" );
      for( unsigned i = 0; i < sizeof( repository->comptes )/sizeof( repository->comptes[0]); ++i ) {
         printf( "| %4s | %7.2f | %8s |\r\n",
            repository->comptes[i].id,
            repository->comptes[i].solde,
            repository->comptes[i].autorise ? "Oui" : "Non" );
      }
      printf( "+------+---------+----------+\r\n" );
      printf( "\r\n" );
      printf( "    'Q' pour "IO_BOLD"Quitter"IO_SGR_OFF" : " );
      fflush( stdout );
      while( ! io_console_kbhit() && *This->dispatcher->running && ! repository->has_changed ) {
         os_sleep( 20 );
      }
      repository->has_changed = false;
      if( io_console_kbhit()) {
         c = toupper( io_console_getch());
      }
   }
   if( c == 'Q' ) {
      UTIL_ERROR_CHECK( SC_banque_dispatcher_terminate( This->dispatcher ));
   }
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}
