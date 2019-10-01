package dab.ui;

import java.io.IOException;

import dab.Etat;
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

   @Override
   public void setStatus(Etat etat ) throws IOException {
      Platform.runLater(() -> _controller.setStatus( etat ));
   }

   @Override
   public void setSoldeCaisse( double solde ) throws IOException {
      Platform.runLater(() -> _controller.setSoldeCaisse( solde ));
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
