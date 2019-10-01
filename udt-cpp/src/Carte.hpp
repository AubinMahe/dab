#include "Date.hpp"

#include <string>

#include <dab/Carte.hpp>

namespace udt {

   class Carte {
   public:

      Carte() :
         _isValid( false )
      {}

      void set(
         const std::string & carteID,
         const std::string & code,
         byte                month,
         ushort              year,
         byte                nbEssais )
      {
         _id       = carteID;
         _code     = code;
         _nbEssais = nbEssais;
         _peremption.set( month, year );
         _isValid  = ( _id.length() > 0 )&&( _code.length() > 0 )&&( _nbEssais < 4 )&&( _peremption.isValid());
      }

      void set( const dab::Carte & carte ) {
         _id       = carte.id;
         _code     = carte.code;
         _nbEssais = carte.nbEssais;
         _peremption.set( carte.month, carte.year );
         _isValid  = ( _id.length() > 0 )&&( _code.length() > 0 )&&( _nbEssais < 4 )&&( _peremption.isValid());
      }

      inline void incrementeNbEssais( void ) { ++_nbEssais; }

      inline void invalidate( void ) { _isValid = false; }

      inline bool isValid( void ) const { return _isValid; }

      inline const std::string & getId( void ) const { return _id; }

      inline bool compareCode( const std::string & code ) const { return _code == code; }

      inline byte getNbEssais( void ) const { return _nbEssais; }

   private:

      bool        _isValid;
      std::string _id;
      std::string _code;
      std::string _compte;
      Date        _peremption;
      byte        _nbEssais;
   };
}
