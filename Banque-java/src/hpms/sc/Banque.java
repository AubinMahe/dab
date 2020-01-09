package hpms.sc;

import java.io.IOException;

import da.InstanceID;
import util.Log;

public final class Banque extends BanqueComponent {

   private static final long BANQUE_DELAI_DE_TRAITEMENT = 3000;

   private IRepository _repository;

   public Banque( InstanceID instanceID, da.IMainLoop mainLoop ) {
      super( instanceID, mainLoop );
   }

   public void setRepository ( IRepository repository ) {
      _repository = repository;
   }

   @Override
   public void informations( String carteID, hpms.dabtypes.Information response ) throws IOException {
      Log.printf( "carteID = '%s'", carteID );
      final ICarte  iCarte  = _repository.getCarte ( carteID );
      final ICompte iCompte = _repository.getCompte( carteID );
      if(( iCarte != null )&&( iCompte != null )) {
         _repository.printStatusOf( carteID );
         try { Thread.sleep( BANQUE_DELAI_DE_TRAITEMENT ); } catch( final InterruptedException x ) {/**/}
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
      Log.printf( "carteID = '%s'", carteID );
      final ICarte carte = _repository.getCarte( carteID );
      if( carte != null ) {
         carte.incrNbEssais();
         _repository.printStatusOf( carteID );
      }
   }

   @Override
   public void retrait( String carteID, double montant ) {
      Log.printf( "montant = %7.2f, carteID = '%s'", montant, carteID );
      final ICompte compte = _repository.getCompte( carteID );
      if( compte != null ) {
         compte.retrait( montant );
         _repository.printStatusOf( carteID );
      }
   }

   @Override
   public void arret() {
      _repository.close();
   }
}
