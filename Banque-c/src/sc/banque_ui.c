#include <sc/banque_ui.h>

#include <io/console.h>
#include <os/sleep.h>

#include <time.h>
#include <ctype.h>

util_error sc_banque_create_ui( sc_banque * This ) {
   io_console_init();
   business_logic_data * bl = (business_logic_data *)This->user_context;
   int c = 0;
   while( c != 'Q' && ! bl->shutdown ) {
      bl->refresh_needed = false;
      printf( IO_ED IO_HOME );
      printf( "+------------------------------------------+\r\n" );
      printf( "|                  Cartes                  |\r\n" );
      printf( "+------+------+------+-------+-------------+\r\n" );
      printf( "|  id  | code | mois | année | nbr d'essai |\r\n" );
      printf( "+------+------+------+-------+-------------+\r\n" );
      for( unsigned i = 0; i < sizeof( bl->repository.cartes )/sizeof( bl->repository.cartes[0]); ++i ) {
         printf( "| %4s | %4s | %4d | %5d | %11d |\r\n",
            bl->repository.cartes[i].id,
            bl->repository.cartes[i].code,
            bl->repository.cartes[i].month,
            bl->repository.cartes[i].year,
            bl->repository.cartes[i].nb_essais );
      }
      printf( "+------+------+------+-------+-------------+\r\n" );
      printf( "\r\n" );
      printf( "+---------------------------+\r\n" );
      printf( "|          Comptes          |\r\n" );
      printf( "+------+---------+----------+\r\n" );
      printf( "|  id  |  solde  | autorisé |\r\n" );
      printf( "+------+---------+----------+\r\n" );
      for( unsigned i = 0; i < sizeof( bl->repository.comptes )/sizeof( bl->repository.comptes[0]); ++i ) {
         printf( "| %4s | %7.2f | %8s |\r\n",
            bl->repository.comptes[i].id,
            bl->repository.comptes[i].solde,
            bl->repository.comptes[i].autorise ? "Oui" : "Non" );
      }
      printf( "+------+---------+----------+\r\n" );
      printf( "\r\n" );
      printf( "    'Q' pour "IO_BOLD"Quitter"IO_SGR_OFF" : " );
      fflush( stdout );
      while( ! io_console_kbhit() && ! bl->shutdown && ! bl->refresh_needed ) {
         os_sleep( 20 );
      }
      if( io_console_kbhit()) {
         c = toupper( io_console_getch());
      }
   }
   return UTIL_NO_ERROR;
}
