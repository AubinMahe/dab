package mixed;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

   @Override
   public void start( Stage stage ) throws Exception {
      final mixed.dab.ComponentFactory factory = new mixed.dab.ComponentFactory();
      new dab.ui.Main().start( factory.getIhm1(), "mixed.ihm1", stage );
      new dab.ui.Main().start( factory.getIhm2(), "mixed.ihm2", new Stage());
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
