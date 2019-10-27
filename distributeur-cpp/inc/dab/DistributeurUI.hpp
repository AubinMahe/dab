#pragma once

namespace dab {

   class Distributeur;
   class DistributeurUI {
   public:

      DistributeurUI( Distributeur & distributeur );

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
