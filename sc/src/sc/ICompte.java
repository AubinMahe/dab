package sc;

public interface ICompte {

   String  getId();
   double  getSolde();
   void    retrait( double montant );
   boolean getAutorise();
}
