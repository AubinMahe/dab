package sc;

import java.io.IOException;

public final class Banque extends BanqueComponent {

   private final IRepository _repository;

   public Banque( String name, IRepository repository ) throws IOException {
      super( name );
      _repository = repository;
      final Thread networkThread = new Thread( this );
      networkThread.setDaemon( true );
      networkThread.start();
   }

   @Override
   public void getInformations( String carteID, SiteCentralGetInformationsResponse response ) throws IOException {
      System.err.printf( getClass().getName() + ".getInformations|carteID = '%s'\n", carteID );
      final ICarte  iCarte  = _repository.getCarte ( carteID );
      final ICompte iCompte = _repository.getCompte( carteID );
      if(( iCarte != null )&&( iCompte != null )) {
         try { Thread.sleep( 3000 ); } catch( final InterruptedException x ) {/**/}
         iCarte .copyTo( response.carte );
         iCompte.copyTo( response.compte );
      }
      else {
         response.carte .id       = "";
         response.carte .code     = "";
         response.carte .month    = 0;
         response.carte .year     = 0;
         response.carte .nbEssais = 3;
         response.compte.id       = "";
         response.compte.solde    = 0.0;
         response.compte.autorise = false;
      }
   }

   @Override
   public void incrNbEssais( String carteID ) {
      System.err.printf( getClass().getName() + ".incrNbEssais( '%s' )\n", carteID );
      final ICarte carte = _repository.getCarte( carteID );
      if( carte != null ) {
         carte.incrNbEssais();
      }
   }

   @Override
   public void retrait( String carteID, double montant ) {
      System.err.printf( getClass().getName() + ".retrait de " + montant + " € à partir de la carte " + carteID + "\n" );
      final ICompte compte = _repository.getCompte( carteID );
      if( compte != null ) {
         compte.retrait( montant );
      }
   }

   @Override
   public void shutdown() {
      _repository.close();
   }

   @Override
   protected void afterDispatch() {
      // Nothing to do
   }
}
