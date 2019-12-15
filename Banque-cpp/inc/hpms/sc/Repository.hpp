#pragma once

#include <hpms/dabtypes/Carte.hpp>
#include <hpms/dabtypes/Compte.hpp>

namespace hpms::sc {

   class Repository {
   public:

      Repository( void );

   public:

      hpms::dabtypes::Carte *  getCarte ( const char * carte_id );
      hpms::dabtypes::Compte * getCompte( const char * carte_id );

      const hpms::dabtypes::Carte *  getCartes ( unsigned & count ) const;
      const hpms::dabtypes::Compte * getComptes( unsigned & count ) const;

   private:

      struct CartesCompte {
         hpms::dabtypes::Carte *  carte;
         hpms::dabtypes::Compte * compte;
      };

   private:

      hpms::dabtypes::Carte  _cartes [5];
      hpms::dabtypes::Compte _comptes[3];
      CartesCompte           _cartes_compte[sizeof( _cartes )/sizeof( _cartes[0] )];

   private:

      Repository( const Repository &) = delete;
      Repository & operator = ( const Repository &) = delete;
   };
}
