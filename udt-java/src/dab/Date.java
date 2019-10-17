package dab;

class Date {

   void set( byte month, short year ) {
      _month = month;
      _year  = year;
   }

   boolean isValid() {
      return( 0 < _month )&&( _month < 13 )&&( _year > 2018 );
   }

   byte  _month;
   short _year;
}