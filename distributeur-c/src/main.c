#include <dab/distributeur.h>
#include <dabtypes/evenement.h>

#include <util/args.h>
#include <util/timeout.h>
#include <os/thread.h>
#include <os/errors.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ncurses.h>

typedef struct business_logic_data_s {
} business_logic_data;

util_error dab_distributeur_etat_du_dab_published( dab_distributeur * This ) {
   fprintf( stderr, "%s\n", __func__ );
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_ejecter_la_carte( dab_distributeur * This ) {
   fprintf( stderr, "%s\n", __func__ );
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_ejecter_les_billets( dab_distributeur * This, double montant ) {
   fprintf( stderr, "%s\n", __func__ );
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_confisquer_la_carte( dab_distributeur * This ) {
   fprintf( stderr, "%s\n", __func__ );
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_placer_les_billets_dans_la_corbeille( dab_distributeur * This ) {
   fprintf( stderr, "%s\n", __func__ );
   return UTIL_NO_ERROR;
}

util_error dab_distributeur_shutdown( dab_distributeur * This ) {
   This->running = false;
   return UTIL_NO_ERROR;
}

static int usage( const char * exename ) {
   fprintf( stderr, "\nusage: %s --name=<name as defined in XML application file>\n\n", exename );
   return 1;
}

static void create_ui( dab_distributeur * This ) {
   initscr();
   cbreak();
   keypad( stdscr, TRUE );
   refresh();

   WINDOW * top = newwin( 3, 100, 0, 0 );
   box      ( top, 0, 0 );
   mvwprintw( top, 1, 23, "DAB en maintenance" );
   mvwprintw( top, 1, 58, "Anomalie | |" );
   wrefresh ( top );

   WINDOW * screen = newwin( 16, 60, 3, 0 );
   box      ( screen, 0, 0 );
   mvwprintw( screen, 1, 1, "Veuillez insérer une carte..." );
   wrefresh ( screen );

   WINDOW * numpad = newwin( 6, 60, 19, 0 );
   box      ( numpad, 0, 0 );
   mvwprintw( numpad, 1, 1, "|7| |8| |9| |Annuler|" );
   mvwprintw( numpad, 2, 1, "|4| |5| |6| |Effacer|" );
   mvwprintw( numpad, 3, 1, "|1| |2| |3| |Entrer |" );
   mvwprintw( numpad, 4, 1, "|0|" );
   wrefresh ( numpad );

   WINDOW * maintenance = newwin( 6, 60, 25, 0 );
   box      ( maintenance, 0, 0 );
   mvwprintw( maintenance, 1, 22, "Maintenance |X|" );
   mvwprintw( maintenance, 2,  1, "Montant : " );
   mvwprintw( maintenance, 2, 11, "10000" );
   mvwprintw( maintenance, 2, 20, "Recharger la caisse" );
   mvwprintw( maintenance, 3, 20, "Vider la corbeille" );
   mvwprintw( maintenance, 4, 20, "Vider le magasin" );
   wrefresh ( maintenance );

   WINDOW * card = newwin( 5, 40, 3, 60 );
   box      ( card, 0, 0 );
   mvwprintw( card, 1,  1, "A123" );
   mvwprintw( card, 2,  6, "Inserer la carte ci-dessus" );
   mvwprintw( card, 3, 12, "Prendre la carte" );
   wrefresh ( card );

   WINDOW * billets = newwin( 4, 40, 8, 60 );
   box      ( billets, 0, 0 );
   mvwprintw( billets, 1,  8, "Distributeur de billets" );
   mvwprintw( billets, 2, 10, "Prendre les billets" );
   wrefresh ( billets );

   WINDOW * corbeilleTitle = newwin( 3, 40, 12, 60 );
   box      ( corbeilleTitle, 0, 0 );
   mvwprintw( corbeilleTitle, 1, 16, "Corbeille" );
   wrefresh ( corbeilleTitle );

   WINDOW * corbeille = newwin( 5, 40, 15, 60 );
   box      ( corbeille, 0, 0 );
   wrefresh ( corbeille );

   WINDOW * magasinTitle = newwin( 3, 40, 20, 60 );
   box      ( magasinTitle, 0, 0 );
   mvwprintw( magasinTitle, 1, 6, "Magasin à carte confisquee" );
   wrefresh ( magasinTitle );

   WINDOW * magasin = newwin( 5, 40, 23, 60 );
   box      ( magasin, 0, 0 );
   wrefresh ( magasin );

   WINDOW * caisse = newwin( 3, 40, 28, 60 );
   box      ( caisse, 0, 0 );
   mvwprintw( caisse, 1, 8, "Caisse : 0.00" );
   wrefresh ( caisse );

   getch();
   endwin();
}

typedef struct background_thread_context_s {
   dab_distributeur distributeur;
   util_error       err;
} background_thread_context;

static void * background_thread_routine( void * ctxt ) {
   background_thread_context * context = (background_thread_context *)ctxt;
   fprintf( stderr, "dab_distributeur_run\n" );
   context->err = dab_distributeur_run( &context->distributeur );
   return NULL;
}

int main( int argc, char * argv[] ) {
   util_pair    pairs[argc];
   util_map     map;
   const char * name = NULL;
   util_args_parse( &map, (size_t)argc, pairs, argc, argv );
   if( UTIL_NO_ERROR != util_args_get_string( &map, "name", &name )) {
      return usage( argv[0] );
   }
   io_winsock_init();
   business_logic_data d;
   memset( &d, 0, sizeof( d ));
   background_thread_context context;
   fprintf( stderr, "dab_distributeur_init\n" );
   context.err = dab_distributeur_init( &context.distributeur, name, &d );
   if( UTIL_NO_ERROR == context.err ) {
      os_thread thread;
      context.err = os_thread_create( &thread, background_thread_routine, &context );
      if( context.err == UTIL_NO_ERROR ) {
         create_ui( &context.distributeur );
      }
      else {
         OS_ERROR_PRINT( "os_thread_create", 6 );
         context.err = UTIL_NO_ERROR;
      }
   }
   if( UTIL_OS_ERROR == context.err ) {
      perror( util_error_messages[context.err] );
   }
   else if( UTIL_NO_ERROR != context.err ) {
      fprintf( stderr, "%s\n", util_error_messages[context.err] );
   }
   dab_distributeur_shutdown( &context.distributeur );
   fprintf( stderr, "end of main\n" );
   return 0;
}
