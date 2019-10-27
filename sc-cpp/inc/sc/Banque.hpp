#include <sc/Repository.hpp>
#include <sc/BanqueUI.hpp>
#include <sc/BanqueComponent.hpp>

namespace sc {

   class Banque : public BanqueComponent {
   public:

      Banque( const char * name );

   public:

      virtual void getInformations( const char * carteID, dabtypes::SiteCentralGetInformationsResponse & response );

      virtual void incrNbEssais( const char * carteID );

      virtual void retrait( const char * carteID, const double & montant );

      virtual void shutdown( void );

   public:

      const Repository & getRepository( void ) const {
         return _repository;
      }

   private:

      Repository _repository;
      BanqueUI   _ui;
   };
}
