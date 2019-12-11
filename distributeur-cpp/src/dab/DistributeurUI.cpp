#include <hpms/dab/DistributeurUI.hpp>
#include <hpms/dab/Distributeur.hpp>

#include <os/sleep.hpp>
#include <io/Console.hpp>
#include <util/Time.hpp>
#include <util/Log.hpp>

#include <ctype.h>
#include <stdio.h>

using namespace hpms::dab;

DistributeurUI::DistributeurUI( Distributeur & distributeur ) :
   _distributeur( distributeur ),
   _refresh( false )
{}

void DistributeurUI::run( void ) {
   const io::Console & console( io::Console::getConsole());
   UTIL_LOG_MSG( "waiting for core" );
//   while( ! _distributeur.isRunning()) { TODO
      os::sleep( 100 );
//   }
   int c = 0;
   while( c != 'Q' /*&& _distributeur.isRunning()*/) {// TODO
      UTIL_LOG_MSG( "printing UI" );
      printf( IO_ED IO_HOME );
      printf( "+-------------------------------------------\n\r" );
      printf( "| Etat du controlleur : %s\n\r"   , dabtypes::toString( _distributeur.getEtatDuDab().etat ));
      printf( "| Solde caisse        : %7.2f\n\r", _distributeur.getEtatDuDab().soldeCaisse );
      _refresh = false;
      printf( "+-------------------------------------------\n\r" );
      printf( "|\n\r" );
      printf( "|                   MENU\n\r" );
      printf( "+-------------------------------------------\n\r" );
      printf( "| 0 : Maintenance " IO_BOLD "ON"  IO_SGR_OFF "\n\r" );
      printf( "| 1 : Maintenance " IO_BOLD "OFF" IO_SGR_OFF "\n\r" );
      printf( "| 2 : Recharger la caisse de " IO_BOLD "10000"  IO_SGR_OFF "\n\r" );
      printf( "| 3 : Recharger la caisse de " IO_BOLD "-10000" IO_SGR_OFF "\n\r" );
      printf( "| 4 : Anomalie " IO_BOLD "ON"  IO_SGR_OFF "\n\r" );
      printf( "| 5 : Anomalie " IO_BOLD "OFF" IO_SGR_OFF "\n\r" );
      printf( "| 6 : Insérer la carte " IO_BOLD "A123" IO_SGR_OFF "\n\r" );
      printf( "| 7 : Insérer la carte " IO_BOLD "B456" IO_SGR_OFF "\n\r" );
      printf( "| 8 : Insérer la carte inconnue " IO_BOLD "Toto" IO_SGR_OFF "\n\r" );
      printf( "| 9 : Saisir le code " IO_BOLD "1230" IO_SGR_OFF "\n\r" );
      printf( "| A : Saisir le code " IO_BOLD "4560" IO_SGR_OFF "\n\r" );
      printf( "| B : Saisir le (mauvais) code " IO_BOLD "9999" IO_SGR_OFF "\n\r" );
      printf( "| C : Saisir le montant " IO_BOLD "120"  IO_SGR_OFF "\n\r" );
      printf( "| D : Saisir le montant " IO_BOLD "4000" IO_SGR_OFF "\n\r" );
      printf( "| E : Retirer " IO_BOLD "la carte" IO_SGR_OFF "\n\r" );
      printf( "| F : Retirer " IO_BOLD "les billets" IO_SGR_OFF "\n\r" );
      printf( "| G : " IO_BOLD "Annuler" IO_SGR_OFF " la transaction\n\r" );
      printf( "+-------------------------------------------\n\r" );
      printf( "| Q : " IO_BOLD "Quitter" IO_SGR_OFF "\n\r" );
      printf( "+-------------------------------------------\n\r" );
      printf( "                Votre choix : " );
      fflush( stdout );
      while( ! console.kbhit() /*&& _distributeur.isRunning()*/ && ! _refresh ) {// TODO
         os::sleep( 20 );
      }
      if( console.kbhit() /*&& _distributeur.isRunning()*/) {// TODO
         c = toupper( console.getch());
         switch( c ) {
         case '0': _distributeur.udt().maintenance                  ( true   ); break;
         case '1': _distributeur.udt().maintenance                  ( false  ); break;
         case '2': _distributeur.udt().rechargerLaCaisse            ( +10000 ); break;
         case '3': _distributeur.udt().rechargerLaCaisse            ( -10000 ); break;
         case '4': _distributeur.udt().anomalie                     ( true   ); break;
         case '5': _distributeur.udt().anomalie                     ( false  ); break;
         case '6': _distributeur.udt().carteInseree                 ( "A123" ); break;
         case '7': _distributeur.udt().carteInseree                 ( "B456" ); break;
         case '8': _distributeur.udt().carteInseree                 ( "Toto" ); break;
         case '9': _distributeur.udt().codeSaisi                    ( "1230" ); break;
         case 'A': _distributeur.udt().codeSaisi                    ( "4560" ); break;
         case 'B': _distributeur.udt().codeSaisi                    ( "9999" ); break;
         case 'C': _distributeur.udt().montantSaisi                 (    120 ); break;
         case 'D': _distributeur.udt().montantSaisi                 (   4000 ); break;
         case 'E': _distributeur.udt().carteRetiree                 (        ); break;
         case 'F': _distributeur.udt().billetsRetires               (        ); break;
         case 'G': _distributeur.udt().annulationDemandeeParLeClient(        ); break;
         }
      }
   }
   UTIL_LOG_MSG( "sending shutdown to UniteDeTraitement" );
   _distributeur.udt().shutdown();
//   _distributeur.terminate(); TODO
   UTIL_LOG_DONE();
}

void DistributeurUI::refresh( void ) {
   UTIL_LOG_HERE();
   _refresh = true;
}
