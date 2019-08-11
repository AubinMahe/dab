package sc;

public interface ICarte {

   String getId();
   String getCode();
   String getCompte();
   byte   getExpirationMonth();
   short  getExpirationYear();
   byte   getNbEssais();
   void   incrNbEssais();
}
