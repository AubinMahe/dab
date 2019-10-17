package dab;

class Carte {

   final Date    _peremption = new Date();
   /* */ boolean _isValid;
   /* */ String  _id;
   /* */ String  _code;
   /* */ String  _compte;
   /* */ byte    _nbEssais;

   Carte() {
      _isValid = false;
   }

   void set( String carteID, String code, byte month, short year, byte nbEssais ) {
      _id       = carteID;
      _code     = code;
      _nbEssais = nbEssais;
      _peremption.set( month, year );
      _isValid  = ( _id.length() > 0 )&&( _code.length() > 0 )&&( _nbEssais < 4 )&& _peremption.isValid() ;
   }

   void set( dabtypes.Carte carte ) {
      _id       = carte.id;
      _code     = carte.code;
      _nbEssais = carte.nbEssais;
      _peremption.set( carte.month, carte.year );
      _isValid  = ( _id.length() > 0 )&&( _code.length() > 0 )&&( _nbEssais < 4 )&& _peremption.isValid() ;
   }

   void incrementeNbEssais() {
      ++_nbEssais;
   }

   void invalidate() {
      _isValid = false;
   }

   boolean isValid() {
      return _isValid;
   }

   String getId() {
      return _id;
   }

   boolean compareCode( String code ) {
      return _code.equals( code );
   }

   byte getNbEssais() { return _nbEssais; }
}