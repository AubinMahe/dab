package udt;

class Compte {

   Compte() {
      _isValid  = false;
      _id       = "";
      _solde    = 0.0;
      _autorise = false;
   }

   void set( String id, double  solde, boolean autorise ) {
      _id       = id;
      _solde    = solde;
      _autorise = autorise;
      _isValid  = true;
   }

   void invalidate() {
      System.err.print( "Compte.invalidate\n" );
      _isValid = false;
   }

   boolean isValid() { return _isValid; }

   String getId() { return _id; }

   double  getSolde() { return _solde; }

   boolean _isValid;
   String  _id;
   double  _solde;
   boolean _autorise;
}