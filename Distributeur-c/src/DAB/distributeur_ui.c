#include <DAB/distributeur_ui.h>

#include <io/console.h>
#include <os/sleep.h>
#include <util/log.h>

#include <time.h>
#include <ctype.h>

util_error DAB_distributeur_create_ui( DAB_distributeur * This ) {
   UTIL_LOG_HERE();
   io_console_init();
   DAB_business_logic_data * bl = (DAB_business_logic_data *)This->user_context;
   int c = 0;
   while( c != 'Q' && This->dispatcher->running ) {
      printf( IO_ED IO_HOME );
      printf( "+-------------------------------------------\n\r\r" );
      printf( "| Etat du controlleur : %s\n\r\r", DBT_etat_to_string( This->etat_du_dab.etat ));
      printf( "| Solde caisse        : %7.2f\n\r\r", This->etat_du_dab.solde_caisse );
      bl->refresh = false;
      printf( "+-------------------------------------------\n\r\r" );
      printf( "|\n\r\r" );
      printf( "|                   MENU\n\r\r" );
      printf( "+-------------------------------------------\n\r\r" );
      printf( "| 0 : Maintenance "IO_BOLD"ON"IO_SGR_OFF"\n\r\r" );
      printf( "| 1 : Maintenance "IO_BOLD"OFF"IO_SGR_OFF"\n\r\r" );
      printf( "| 2 : Recharger la caisse de "IO_BOLD"10000"IO_SGR_OFF"\n\r\r" );
      printf( "| 3 : Recharger la caisse de "IO_BOLD"-10000"IO_SGR_OFF"\n\r\r" );
      printf( "| 4 : Anomalie "IO_BOLD"ON"IO_SGR_OFF"\n\r\r" );
      printf( "| 5 : Anomalie "IO_BOLD"OFF"IO_SGR_OFF"\n\r" );
      printf( "| 6 : Insérer la carte "IO_BOLD"A123"IO_SGR_OFF"\n\r" );
      printf( "| 7 : Insérer la carte "IO_BOLD"B456"IO_SGR_OFF"\n\r" );
      printf( "| 8 : Insérer la carte inconnue "IO_BOLD"Toto"IO_SGR_OFF"\n\r" );
      printf( "| 9 : Saisir le code "IO_BOLD"1230"IO_SGR_OFF"\n\r" );
      printf( "| A : Saisir le code "IO_BOLD"4560"IO_SGR_OFF"\n\r" );
      printf( "| B : Saisir le (mauvais) code "IO_BOLD"9999"IO_SGR_OFF"\n\r" );
      printf( "| C : Saisir le montant "IO_BOLD"120"IO_SGR_OFF"\n\r" );
      printf( "| D : Saisir le montant "IO_BOLD"4000"IO_SGR_OFF"\n\r" );
      printf( "| E : Retirer "IO_BOLD"la carte"IO_SGR_OFF"\n\r" );
      printf( "| F : Retirer "IO_BOLD"les billets"IO_SGR_OFF"\n\r" );
      printf( "| G : "IO_BOLD"Annuler"IO_SGR_OFF" la transaction\n\r" );
      printf( "+-------------------------------------------\n\r" );
      printf( "| Q : "IO_BOLD"Quitter"IO_SGR_OFF"\n\r" );
      printf( "+-------------------------------------------\n\r" );
      printf( "                Votre choix : " );
      fflush( stdout );
      while( ! io_console_kbhit() && ! bl->refresh ) {
         os_sleep( 20 );
      }
      if( io_console_kbhit()) {
         c = toupper( io_console_getch());
         UTIL_LOG_ARGS( "Command = %c", (char)c );
         switch( c ) {
         case '0': UTIL_ERROR_CHECK( DAB_maintenable_maintenance                ( This->maintenable, true   )); break;
         case '1': UTIL_ERROR_CHECK( DAB_maintenable_maintenance                ( This->maintenable, false  )); break;
         case '2': UTIL_ERROR_CHECK( DAB_unite_de_traitement_recharger_la_caisse( This->unite_de_traitement, +10000 )); break;
         case '3': UTIL_ERROR_CHECK( DAB_unite_de_traitement_recharger_la_caisse( This->unite_de_traitement, -10000 )); break;
         case '4': UTIL_ERROR_CHECK( DAB_unite_de_traitement_anomalie           ( This->unite_de_traitement, true   )); break;
         case '5': UTIL_ERROR_CHECK( DAB_unite_de_traitement_anomalie           ( This->unite_de_traitement, false  )); break;
         case '6': UTIL_ERROR_CHECK( DAB_unite_de_traitement_carte_inseree      ( This->unite_de_traitement, "A123" )); break;
         case '7': UTIL_ERROR_CHECK( DAB_unite_de_traitement_carte_inseree      ( This->unite_de_traitement, "B456" )); break;
         case '8': UTIL_ERROR_CHECK( DAB_unite_de_traitement_carte_inseree      ( This->unite_de_traitement, "Toto" )); break;
         case '9': UTIL_ERROR_CHECK( DAB_unite_de_traitement_code_saisi         ( This->unite_de_traitement, "1230" )); break;
         case 'A': UTIL_ERROR_CHECK( DAB_unite_de_traitement_code_saisi         ( This->unite_de_traitement, "4560" )); break;
         case 'B': UTIL_ERROR_CHECK( DAB_unite_de_traitement_code_saisi         ( This->unite_de_traitement, "9999" )); break;
         case 'C': UTIL_ERROR_CHECK( DAB_unite_de_traitement_montant_saisi      ( This->unite_de_traitement,    120 )); break;
         case 'D': UTIL_ERROR_CHECK( DAB_unite_de_traitement_montant_saisi      ( This->unite_de_traitement,   4000 )); break;
         case 'E': UTIL_ERROR_CHECK( DAB_unite_de_traitement_carte_retiree      ( This->unite_de_traitement         )); break;
         case 'F': UTIL_ERROR_CHECK( DAB_unite_de_traitement_billets_retires    ( This->unite_de_traitement         )); break;
         case 'G': UTIL_ERROR_CHECK( DAB_unite_de_traitement_annulation_demandee_par_le_client( This->unite_de_traitement )); break;
         case 'Q': UTIL_ERROR_CHECK( DAB_unite_de_traitement_arret              ( This->unite_de_traitement         ));
                   UTIL_ERROR_CHECK( DAB_distributeur_dispatcher_terminate      ( This->dispatcher ));                  break;
         }
      }
   }
   return UTIL_NO_ERROR;
}
