package hpms.sc;

public interface IRepository {

   ICarte  getCarte ( String carteID );
   ICompte getCompte( String carteID );
   void close();
}
