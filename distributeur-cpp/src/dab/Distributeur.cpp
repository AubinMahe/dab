#include <hpms/dab/Distributeur.hpp>

#include <os/Thread.hpp>
#include <util/Time.hpp>
#include <util/Log.hpp>

#include <stdio.h>

using namespace hpms::dab;

static void * launchUI( void * arg ) {
   DistributeurUI * ui = (DistributeurUI *)arg;
   ui->run();
   return nullptr;
}

Distributeur::Distributeur( void ) :
   _ui( *this )
{
   UTIL_LOG_HERE();
   _etatDuDab->etat = dabtypes::Etat::MAINTENANCE;
   _etatDuDab->soldeCaisse = 0.0;
   os::Thread( launchUI, &_ui );
}

void Distributeur::etatDuDabPublished( void ) {
   UTIL_LOG_HERE();
   _ui.refresh();
}

void Distributeur::ejecterLaCarte( void ) {
   UTIL_LOG_HERE();
   _ui.refresh();
}

void Distributeur::ejecterLesBillets( const double & montant ) {
	UTIL_LOG_ARGS( "montant = %7.2f", montant );
   _ui.refresh();
}

void Distributeur::confisquerLaCarte( void ) {
   UTIL_LOG_HERE();
   _ui.refresh();
}

void Distributeur::placerLesBilletsDansLaCorbeille( void ) {
   UTIL_LOG_HERE();
   _ui.refresh();
}

void Distributeur::shutdown( void ) {
   UTIL_LOG_HERE();
   _ui.refresh();
}
