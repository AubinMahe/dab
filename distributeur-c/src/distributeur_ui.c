#include <distributeur_ui.h>

#include <io/console.h>

#include <time.h>
#include <ctype.h>

#ifndef _WIN32
extern int nanosleep( const struct timespec * requested_time, struct timespec * remaining );
#endif

util_error dab_distributeur_create_ui( dab_distributeur * This ) {
   console_init();
#ifdef _WIN32
   const DWORD period = 20UL;
#else
   const struct timespec period = { 0, 20*1000000 };
#endif
   business_logic_data * bl = (business_logic_data *)This->user_context;
   int c = 0;
   while( c != 'Q' && ! bl->shutdown ) {
      printf( IO_ED IO_HOME );
      printf( "+-------------------------------------------\n\r\r" );
      printf( "| Etat du controlleur : %s\n\r\r", dabtypes_etat_to_string( This->etat_du_dab.etat ));
      printf( "| Solde caisse        : %7.2f\n\r\r", This->etat_du_dab.solde_caisse );
      bl->etat_du_dab_published = false;
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
      while( ! console_kbhit() && ! bl->etat_du_dab_published ) {
#ifdef _WIN32
         Sleep( period );
#else
         nanosleep( &period, NULL );
#endif
      }
      if( console_kbhit()) {
         c = toupper( console_getch());
         switch( c ) {
         case '0': UTIL_ERROR_CHECK( dab_unite_de_traitement_maintenance                      ( &This->unite_de_traitement, true   )); break;
         case '1': UTIL_ERROR_CHECK( dab_unite_de_traitement_maintenance                      ( &This->unite_de_traitement, false  )); break;
         case '2': UTIL_ERROR_CHECK( dab_unite_de_traitement_recharger_la_caisse              ( &This->unite_de_traitement, +10000 )); break;
         case '3': UTIL_ERROR_CHECK( dab_unite_de_traitement_recharger_la_caisse              ( &This->unite_de_traitement, -10000 )); break;
         case '4': UTIL_ERROR_CHECK( dab_unite_de_traitement_anomalie                         ( &This->unite_de_traitement, true   )); break;
         case '5': UTIL_ERROR_CHECK( dab_unite_de_traitement_anomalie                         ( &This->unite_de_traitement, false  )); break;
         case '6': UTIL_ERROR_CHECK( dab_unite_de_traitement_carte_inseree                    ( &This->unite_de_traitement, "A123" )); break;
         case '7': UTIL_ERROR_CHECK( dab_unite_de_traitement_carte_inseree                    ( &This->unite_de_traitement, "B456" )); break;
         case '8': UTIL_ERROR_CHECK( dab_unite_de_traitement_carte_inseree                    ( &This->unite_de_traitement, "Toto" )); break;
         case '9': UTIL_ERROR_CHECK( dab_unite_de_traitement_code_saisi                       ( &This->unite_de_traitement, "1230" )); break;
         case 'A': UTIL_ERROR_CHECK( dab_unite_de_traitement_code_saisi                       ( &This->unite_de_traitement, "4560" )); break;
         case 'B': UTIL_ERROR_CHECK( dab_unite_de_traitement_code_saisi                       ( &This->unite_de_traitement, "9999" )); break;
         case 'C': UTIL_ERROR_CHECK( dab_unite_de_traitement_montant_saisi                    ( &This->unite_de_traitement,    120 )); break;
         case 'D': UTIL_ERROR_CHECK( dab_unite_de_traitement_montant_saisi                    ( &This->unite_de_traitement,   4000 )); break;
         case 'E': UTIL_ERROR_CHECK( dab_unite_de_traitement_carte_retiree                    ( &This->unite_de_traitement         )); break;
         case 'F': UTIL_ERROR_CHECK( dab_unite_de_traitement_billets_retires                  ( &This->unite_de_traitement         )); break;
         case 'G': UTIL_ERROR_CHECK( dab_unite_de_traitement_annulation_demandee_par_le_client( &This->unite_de_traitement         )); break;
         }
      }
   }
   dab_unite_de_traitement_shutdown( &This->unite_de_traitement );
   return UTIL_NO_ERROR;
}
