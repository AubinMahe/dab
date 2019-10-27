#pragma once

#include <dabtypes/Carte.hpp>
#include <dabtypes/Compte.hpp>

namespace sc {

   class Repository {
   public:

      Repository( void );

   public:

      dabtypes::Carte *  getCarte ( const char * carte_id );
      dabtypes::Compte * getCompte( const char * carte_id );

      const dabtypes::Carte *  getCartes ( unsigned & count ) const;
      const dabtypes::Compte * getComptes( unsigned & count ) const;

   private:

      struct CartesCompte {
         dabtypes::Carte *  carte;
         dabtypes::Compte * compte;
      };

   private:

      dabtypes::Carte  _cartes [5];
      dabtypes::Compte _comptes[3];
      CartesCompte     _cartes_compte[sizeof( _cartes )/sizeof( _cartes[0] )];

   private:

      Repository( const Repository &) = delete;
      Repository & operator = ( const Repository &) = delete;
   };
}
