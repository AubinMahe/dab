#pragma once

namespace sc {

   class Banque;
   class BanqueUI {
   public:

      BanqueUI( Banque & banque );

   public:

      void run( void );

      void refresh( void );

   private:

      Banque & _banque;
      bool     _refresh;

      BanqueUI( const BanqueUI & ) = delete;
      BanqueUI & operator = ( const BanqueUI & ) = delete;
   };
}
