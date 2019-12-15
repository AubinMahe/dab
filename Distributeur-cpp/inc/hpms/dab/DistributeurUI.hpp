#pragma once

namespace hpms::dab {

   class Distributeur;
   class DistributeurUI {
   public:

      DistributeurUI( Distributeur & distributeur );
      ~ DistributeurUI( void );

   public:

      void run( void );
      void refresh( void );

   private:

      Distributeur & _distributeur;
      bool           _refresh;
      const char *   _action;
      double         _montant;

      DistributeurUI( const DistributeurUI & );
      DistributeurUI & operator = ( const DistributeurUI & );
   };
}
