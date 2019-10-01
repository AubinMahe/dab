package udt;

class Compte {

   boolean _isValid;
   String  _id;
   double  _solde;
   boolean _autorise;

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
      _isValid  = ( _id.length() > 0 )&&( _solde > 0.0 );
   }

   public void set( dab.Compte compte ) {
      _id       = compte.id;
      _solde    = compte.solde;
      _autorise = compte.autorise;
      _isValid  = ( _id.length() > 0 )&&( _solde > 0.0 );
   }

   void invalidate() {
      System.err.print( "Compte.invalidate\n" );
      _isValid = false;
   }

   boolean isValid() { return _isValid; }

   String getId() { return _id; }

   double  getSolde() { return _solde; }
}