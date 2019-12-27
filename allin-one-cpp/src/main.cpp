#include <allin/one/ComponentFactory.hpp>

#include <util/Log.hpp>

#include <stdio.h>
#include <stdlib.h>

#include <exception>

int main( void ) {
   try {
      fprintf( stderr, "\n" );
      UTIL_LOG_HERE();
      allin::one::ComponentFactory factory;
      hpms::dab::Distributeur & distributeur1( factory.getIhm1());
      hpms::dab::DistributeurUI distributeurUI1( distributeur1 );
      distributeur1.setUI( distributeurUI1 );
      hpms::dab::Distributeur & distributeur2( factory.getIhm2());
      hpms::dab::DistributeurUI distributeurUI2( distributeur2 );
      distributeur2.setUI( distributeurUI2 );
      distributeurUI1.run();
      // Le code suivant est commenté car les deux IHM, en mode texte, partagent le même tty.
      // L'affichage serait totalement anarchique.
      // distributeurUI2.run();
      UTIL_LOG_DONE();
      return EXIT_SUCCESS;
   }
   catch( const std::exception & x ) {
      fprintf( stderr, "%s\n", x.what());
   }
   return EXIT_FAILURE;
}
