package sc;

import dabtypes.Carte;

public interface ICarte {

   String getId();
   String getCode();
   String getCompte();
   byte   getExpirationMonth();
   short  getExpirationYear();
   byte   getNbEssais();
   void   incrNbEssais();
   void   copyTo( Carte out );
}
