#include <hpms/dab/Distributeur.hpp>

#include <os/Thread.hpp>
#include <util/Time.hpp>
#include <util/Log.hpp>

#include <stdio.h>

using namespace hpms::dab;

Distributeur::Distributeur( void ) :
   _ui( nullptr )
{
   UTIL_LOG_HERE();
}

Distributeur::~Distributeur( void ) {
   UTIL_LOG_HERE();
}

void Distributeur::init( void ) {
   UTIL_LOG_HERE();
   _etatDuDab.etat        = dabtypes::Etat::MAINTENANCE;
   _etatDuDab.soldeCaisse = 0.0;
}

void Distributeur::etatDuDabPublished( void ) {
   UTIL_LOG_HERE();
}

void Distributeur::ejecterLaCarte( void ) {
   UTIL_LOG_HERE();
}

void Distributeur::ejecterLesBillets( const double & montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f", montant );
}

void Distributeur::confisquerLaCarte( void ) {
   UTIL_LOG_HERE();
}

void Distributeur::placerLesBilletsDansLaCorbeille( void ) {
   UTIL_LOG_HERE();
}

void Distributeur::arret( void ) {
   UTIL_LOG_HERE();
   _dispatcher->terminate();
}

void Distributeur::afterDispatch( void ) {
   UTIL_LOG_HERE();
   if( _ui ) {
      _ui->refresh();
   }
}
