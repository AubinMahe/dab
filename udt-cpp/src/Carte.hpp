#include "Date.hpp"

#include <dab/Carte.hpp>

#include <string.h>

namespace udt {

   class Carte {
   public:

      Carte() :
         _isValid( false )
      {}

      void set(
         const char *   carteID,
         const char *   code,
         byte           month,
         unsigned short year,
         byte           nbEssais )
      {
         strncpy( _id  , carteID, sizeof( _id   )); _id  [sizeof( _id   )-1] = '\0';
         strncpy( _code, code   , sizeof( _code )); _code[sizeof( _code )-1] = '\0';
         _nbEssais = nbEssais;
         _peremption.set( month, year );
         _isValid  = ( strlen( _id ) > 0 )&&( strlen( _code ) > 0 )&&( _nbEssais < 4 )&&( _peremption.isValid());
      }

      void set( const dab::Carte & carte ) {
         set( carte.id, carte.code, carte.month, carte.year, carte.nbEssais );
      }

      inline void incrementeNbEssais( void ) { ++_nbEssais; }

      inline void invalidate( void ) { _isValid = false; }

      inline bool isValid( void ) const { return _isValid; }

      inline const char * getId( void ) const { return _id; }

      inline bool compareCode( const char * code ) const { return 0 == strcmp( _code, code ); }

      inline byte getNbEssais( void ) const { return _nbEssais; }

   private:

      bool _isValid;
      char _id  [4+1];
      char _code[4+1];
      Date _peremption;
      byte _nbEssais;
   };
}
