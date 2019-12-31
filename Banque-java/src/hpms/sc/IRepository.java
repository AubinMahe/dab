package hpms.sc;

public interface IRepository {

   ICarte  getCarte ( String carteID );
   ICompte getCompte( String carteID );
   void printStatusOf( String carteID ); // À des fins de test
   void close();
}
