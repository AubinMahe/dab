#include <dab/Distributeur.hpp>

#include <os/Thread.hpp>
#include <util/Time.hpp>

#include <stdio.h>

using namespace dab;

static void * launchUI( void * arg ) {
   DistributeurUI * ui = (DistributeurUI *)arg;
   ui->run();
   return nullptr;
}

Distributeur::Distributeur( const char * name ) :
   DistributeurComponent( name ),
   _ui( *this )
{
   fprintf( stderr, "%s:%s\n", util::Time::now(), HPMS_FUNCNAME );
   _etatDuDab.etat = dabtypes::Etat::MAINTENANCE;
   _etatDuDab.soldeCaisse = 0.0;
   os::Thread( launchUI, &_ui );
}

void Distributeur::etatDuDabPublished( void ) {
   fprintf( stderr, "%s:%s\n", util::Time::now(), HPMS_FUNCNAME );
   _ui.refresh();
}

void Distributeur::ejecterLaCarte( void ) {
   fprintf( stderr, "%s:%s\n", util::Time::now(), HPMS_FUNCNAME );
   _ui.refresh();
}

void Distributeur::ejecterLesBillets( const double & montant ) {
   fprintf( stderr, "%s:%s( montant = %7.2f )\n", util::Time::now(), HPMS_FUNCNAME, montant );
   _ui.refresh();
}

void Distributeur::confisquerLaCarte( void ) {
   fprintf( stderr, "%s:%s\n", util::Time::now(), HPMS_FUNCNAME );
   _ui.refresh();
}

void Distributeur::placerLesBilletsDansLaCorbeille( void ) {
   fprintf( stderr, "%s:%s\n", util::Time::now(), HPMS_FUNCNAME );
   _ui.refresh();
}

void Distributeur::shutdown( void ) {
   fprintf( stderr, "%s:%s\n", util::Time::now(), HPMS_FUNCNAME );
   _ui.refresh();
}
