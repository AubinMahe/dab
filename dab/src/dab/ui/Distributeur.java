package dab.ui;

import java.io.IOException;

import dab.IUniteDeTraitement;
import dabtypes.EtatDuDab;
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
   public void placerLesBilletsDansLaCorbeille() throws IOException {
      Platform.runLater(() -> _controller.placerLesBilletsDansLaCorbeille());
   }

   @Override
   public void ejecterLaCarte() throws IOException {
      Platform.runLater(() -> _controller.ejecterLaCarte());
   }

   @Override
   public void shutdown() throws IOException {
      Platform.runLater(() -> _controller.shutdown());
   }

   public IUniteDeTraitement uniteDeTraitement() {
      return _uniteDeTraitement;
   }

   @Override
   public void ejecterLesBillets( double montant ) throws IOException {
      Platform.runLater(() -> _controller.ejecterLesBillets( montant ));
   }
}
