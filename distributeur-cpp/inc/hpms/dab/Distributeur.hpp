#pragma once

#include <hpms/dab/DistributeurUI.hpp>
#include <hpms/dab/DistributeurComponent.hpp>

namespace hpms::dab {

   class Distributeur : public DistributeurComponent {
   public:

      Distributeur( void );

      virtual ~ Distributeur( void ) {}

   public:

      virtual void ejecterLaCarte( void );
      virtual void ejecterLesBillets( const double & montant );
      virtual void confisquerLaCarte( void );
      virtual void placerLesBilletsDansLaCorbeille( void );
      virtual void shutdown( void );
      virtual void etatDuDabPublished( void );

   public:

      const hpms::dabtypes::EtatDuDab & getEtatDuDab( void ) { return *_etatDuDab; }
      UniteDeTraitement & udt() { return *_uniteDeTraitement; }

   private:

      DistributeurUI _ui;
   };
}
