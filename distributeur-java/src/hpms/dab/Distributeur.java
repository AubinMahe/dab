package hpms.dab;

import java.io.IOException;

import dab.ui.Controller;
import javafx.application.Platform;

public class Distributeur extends hpms.dab.DistributeurComponent {

   private Controller _controller;

   public void setController( Controller controller ) {
      _controller = controller;
   }

   @Override
   public void etatDuDabPublished() throws IOException {
      Platform.runLater( _controller::etatDuDabPublished );
   }

   @Override
   public void confisquerLaCarte() throws IOException {
      Platform.runLater( _controller::confisquerLaCarte );
   }

   @Override
   public void placerLesBilletsDansLaCorbeille() throws IOException {
      Platform.runLater( _controller::placerLesBilletsDansLaCorbeille );
   }

   @Override
   public void ejecterLaCarte() throws IOException {
      Platform.runLater( _controller::ejecterLaCarte );
   }

   @Override
   public void shutdown() throws IOException {
      Platform.runLater( _controller::shutdown );
   }

   @Override
   public void ejecterLesBillets( double montant ) throws IOException {
      Platform.runLater(() -> _controller.ejecterLesBillets( montant ));
   }
}
