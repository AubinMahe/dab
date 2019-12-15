#pragma once

#include <hpms/dab/DistributeurUI.hpp>
#include <hpms/dab/DistributeurComponent.hpp>

namespace hpms::dab {

   class Distributeur : public DistributeurComponent {
   public:

      Distributeur( void );

      virtual ~ Distributeur( void );

   public:

      void setUI( DistributeurUI & ui ) {
         _ui = &ui;
      }

   public:

      virtual void init( void );
      virtual void ejecterLaCarte( void );
      virtual void ejecterLesBillets( const double & montant );
      virtual void confisquerLaCarte( void );
      virtual void placerLesBilletsDansLaCorbeille( void );
      virtual void shutdown( void );
      virtual void etatDuDabPublished( void );
      virtual void afterDispatch( bool dispatched );

   private:

      DistributeurUI * _ui;
   };
}
