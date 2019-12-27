#pragma once

#include <hpms/sc/Repository.hpp>
#include <hpms/sc/BanqueUI.hpp>
#include <hpms/sc/BanqueComponent.hpp>

namespace hpms::sc {

   class Banque : public BanqueComponent {
   public:

      Banque( void ) : _ui( nullptr ) {}
      virtual ~ Banque( void ) {}

   public:

      virtual void informations( const char * carteID, hpms::dabtypes::Information & response );
      virtual void incrNbEssais( const char * carteID );
      virtual void retrait( const char * carteID, const double & montant );
      virtual void arret( void );
      virtual void afterDispatch( bool dispatched );

   public:

      void setUI( BanqueUI & ui ) {
         _ui = &ui;
      }

      const Repository & getRepository( void ) const {
         return _repository;
      }

   private:

      Repository _repository;
      BanqueUI * _ui;
   };
}
