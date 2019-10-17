#include <types.hpp>

namespace dab {

   class Date {
   public:

      Date( void ) :
         _month( 0 ),
         _year ( 0 )
      {}

      void set( byte month, unsigned short year ) {
         _month = month;
         _year  = year;
      }

      inline bool isValid() const {
         return( 0 < _month )&&( _month < 13 )&&( _year > 2018 );
      }

      byte           _month;
      unsigned short _year;

   private:
      Date( const Date & ) = delete;
      Date & operator = ( const Date & ) = delete;
   };
}
