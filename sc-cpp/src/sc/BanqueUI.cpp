#include <sc/BanqueUI.hpp>
#include <sc/Banque.hpp>

#include <os/sleep.hpp>
#include <io/Console.hpp>
#include <util/Time.hpp>

#include <ctype.h>
#include <stdio.h>

using namespace sc;

BanqueUI::BanqueUI( Banque & banque ) :
   _banque ( banque ),
   _refresh( false )
{}

void BanqueUI::run( void ) {
   const io::Console & console( io::Console::getConsole());
   fprintf( stderr, "%s:%s|waiting for core\n", util::Time::now(), HPMS_FUNCNAME );
   while( ! _banque.isRunning()) {
      os::sleep( 100 );
   }
   int c = 0;
   while( c != 'Q' && _banque.isRunning()) {
      fprintf( stderr, "%s:%s|printing UI\n", util::Time::now(), HPMS_FUNCNAME );
      _refresh = false;
      printf( IO_ED IO_HOME );
      printf( "+------------------------------------------+\r\n" );
      printf( "|                  Cartes                  |\r\n" );
      printf( "+------+------+------+-------+-------------+\r\n" );
      printf( "|  id  | code | mois | année | nbr d'essai |\r\n" );
      printf( "+------+------+------+-------+-------------+\r\n" );
      unsigned cartesCount = 0;
      const dabtypes::Carte * cartes = _banque.getRepository().getCartes( cartesCount );
      for( unsigned i = 0; i < cartesCount; ++i ) {
         printf( "| %4s | %4s | %4d | %5d | %11d |\r\n",
            cartes[i].id,
            cartes[i].code,
            cartes[i].month,
            cartes[i].year,
            cartes[i].nbEssais );
      }
      printf( "+------+------+------+-------+-------------+\r\n" );
      printf( "\r\n" );
      printf( "+---------------------------+\r\n" );
      printf( "|          Comptes          |\r\n" );
      printf( "+------+---------+----------+\r\n" );
      printf( "|  id  |  solde  | autorisé |\r\n" );
      printf( "+------+---------+----------+\r\n" );
      unsigned comptesCount = 0;
      const dabtypes::Compte * comptes = _banque.getRepository().getComptes( comptesCount );
      fprintf( stderr, "%s:%s|comptesCount = %d\n", util::Time::now(), HPMS_FUNCNAME, comptesCount );
      for( unsigned i = 0; i < comptesCount; ++i ) {
         printf( "| %4s | %7.2f | %8s |\r\n",
            comptes[i].id,
            comptes[i].solde,
            comptes[i].autorise ? "Oui" : "Non" );
      }
      printf( "+------+---------+----------+\r\n" );
      printf( "\r\n" );
      printf( "    'Q' pour " IO_BOLD "Quitter" IO_SGR_OFF " : " );
      fflush( stdout );
      while( ! console.kbhit() && _banque.isRunning() && ! _refresh ) {
         os::sleep( 20 );
      }
      if( console.kbhit() && _banque.isRunning()) {
         fprintf( stderr, "%s:%s|console.getch()\n", util::Time::now(), HPMS_FUNCNAME );
         c = toupper( console.getch());
         fprintf( stderr, "%s:%s|executing command '%c'\n", util::Time::now(), HPMS_FUNCNAME, (char)c );
      }
   }
   _banque.terminate();
   fprintf( stderr, "%s:%s|UI thread terminated\n", util::Time::now(), HPMS_FUNCNAME );
}

void BanqueUI::refresh( void ) {
   _refresh = true;
}
