#include <string>

#include <types.hpp>

namespace udt {

   class Compte {
   public:

      Compte() :
         _isValid ( false ),
         _id      ( ""    ),
         _solde   ( 0.0   ),
         _autorise( false )
      {}

      void set( const std::string id, const double & solde, bool autorise ) {
         _id       = id;
         _solde    = solde;
         _autorise = autorise;
         _isValid  = true;
      }

      void invalidate() { _isValid = false; }

      bool isValid( void ) const { return _isValid; }

      const std::string & getId( void ) const { return _id; }

      const double & getSolde( void ) const { return _solde; }

   private:

      bool        _isValid;
      std::string _id;
      double      _solde;
      bool        _autorise;
   };
}
