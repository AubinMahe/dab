#include <dabtypes/Compte.hpp>

#include <stdio.h>
#include <string.h>

#include <util/Time.hpp>

namespace dab {

   class Compte {
   public:

      Compte() :
         _isValid ( false ),
         _solde   ( 0.0   ),
         _autorise( false )
      {
         _id[0] = '\0';
      }

      void set( const char * id, const double & solde, bool autorise ) {
         strncpy( _id, id, sizeof( _id )); _id[sizeof( _id )-1] = '\0';
         _solde    = solde;
         _autorise = autorise;
         _isValid  = ( 4 == strlen( _id ));
      }

      void set( const dabtypes::Compte & compte ) {
         set( compte.id, compte.solde, compte.autorise );
      }

      void invalidate() { _isValid = false; }

      bool isValid( void ) const { return _isValid; }

      const char * getId( void ) const { return _id; }

      const double & getSolde( void ) const { return _solde; }

      void dump( void ) {
         UTIL_LOG_ARGS( "isValid : %s"   , _isValid ? "true" : "false" );
         UTIL_LOG_ARGS( "id      : %s"   , _id );
         UTIL_LOG_ARGS( "solde   : %7.2f", _solde );
         UTIL_LOG_ARGS( "autorise: %s"   , _autorise ? "true" : "false" );
      }

   private:

      bool   _isValid;
      char   _id[5];
      double _solde;
      bool   _autorise;
   };
}
