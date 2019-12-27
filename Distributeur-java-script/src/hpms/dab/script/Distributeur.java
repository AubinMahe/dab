package hpms.dab.script;

import java.io.IOException;

public class Distributeur extends hpms.dab.script.DistributeurComponent {

   public Distributeur( byte instanceID ) {
      super( instanceID );
   }

   @Override
   public void etatDuDabPublished() throws IOException {
   }

   @Override
   public void confisquerLaCarte() throws IOException {
   }

   @Override
   public void placerLesBilletsDansLaCorbeille() throws IOException {
   }

   @Override
   public void ejecterLaCarte() throws IOException {
   }

   @Override
   public void arret() throws IOException {
   }

   @Override
   public void ejecterLesBillets( double montant ) throws IOException {
   }
}
