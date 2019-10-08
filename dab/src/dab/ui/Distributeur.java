package dab.ui;

import java.io.IOException;

import dab.EtatDuDab;
import javafx.application.Platform;

public class Distributeur extends dab.DistributeurComponent {

   private final Controller _controller;

   public Distributeur( String name, Controller controller ) throws IOException {
      super( name );
      _controller = controller;
      final Thread networkThread = new Thread( this );
      networkThread.setDaemon( true );
      networkThread.start();
   }

   EtatDuDab getEtatDuDab() {
      return etatDuDab;
   }

   @Override
   public void etatDuDabPublished() throws IOException {
      Platform.runLater( _controller::etatDuDabPublished );
   }

   @Override
   public void confisquerLaCarte() throws IOException {
      Platform.runLater(() -> _controller.confisquerLaCarte());
   }

   @Override
   public void ejecterLaCarte() throws IOException {
      Platform.runLater(() -> _controller.ejecterLaCarte());
   }

   @Override
   protected void afterDispatch() throws IOException {
      /* Nothing to do */
   }

   @Override
   public void shutdown() throws IOException {
      Platform.runLater(() -> _controller.shutdown());
   }

   public void done() {
      try {
         _uniteDeTraitement.shutdown();
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }

   public void codeSaisi( String code ) {
      try {
         _uniteDeTraitement.codeSaisi( code );
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }

   public void montantSaisi( double montant ) {
      try {
         _uniteDeTraitement.montantSaisi( montant );
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }

   public void carteInseree( String id ) {
      try {
         _uniteDeTraitement.carteInseree( id );
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }

   public void maintenance( boolean maintenance ) {
      try {
         _uniteDeTraitement.maintenance( maintenance );
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }

   public void rechargerLaCaisse( double montant ) {
      try {
         _uniteDeTraitement.rechargerLaCaisse( montant );
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }

   public void carteRetiree() {
      try {
         _uniteDeTraitement.carteRetiree();
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }

   public void billetsRetires() {
      try {
         _uniteDeTraitement.billetsRetires();
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }

   public void anomalie( boolean anomalie ) {
      try {
         _uniteDeTraitement.anomalie( anomalie );
      }
      catch( final Throwable t ){
         t.printStackTrace();
      }
   }
}
