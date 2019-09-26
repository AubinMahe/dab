package udt;

class Carte {

   Carte() {
      _isValid = false;
   }

   void set( String carteID, String code, byte month, short year, byte nbEssais ) {
      _id       = carteID;
      _code     = code;
      _nbEssais = nbEssais;
      _isValid  = true;
      _peremption.set( month, year );
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

   final Date    _peremption = new Date();
   /* */ boolean _isValid;
   /* */ String  _id;
   /* */ String  _code;
   /* */ String  _compte;
   /* */ byte    _nbEssais;
}