#include <dab/DistributeurUI.hpp>
#include <dab/DistributeurComponent.hpp>

namespace dab {

   class Distributeur : public DistributeurComponent {
   public:

      Distributeur( const char * name );

   public:

      virtual void ejecterLaCarte( void );
      virtual void ejecterLesBillets( const double & montant );
      virtual void confisquerLaCarte( void );
      virtual void placerLesBilletsDansLaCorbeille( void );
      virtual void shutdown( void );
      virtual void etatDuDabPublished( void );

   public:

      const dabtypes::EtatDuDab & getEtatDuDab( void ) { return _etatDuDab; }
      UniteDeTraitement & udt() { return _uniteDeTraitement; }

   private:

      DistributeurUI _ui;
   };
}
