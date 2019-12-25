package hpms.dab.ui;

import java.util.ResourceBundle;

import hpms.dab.Distributeur;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mixed.dab1.ComponentFactory;

public class Main extends Application {

   @Override
   public void start( Stage stage ) throws Exception {
      final Distributeur ihm1 = new ComponentFactory().getIhm1();
      final Class<? extends Main> clazz = getClass();
      final FXMLLoader loader =
         new FXMLLoader( clazz.getResource( "ui.fxml" ), ResourceBundle.getBundle( clazz.getPackageName() + "/messages" ));
      stage.setScene( new Scene( loader.load()));
      stage.setTitle( "Site Central 'sc'" );
      final Controller ctrl = loader.getController();
      ctrl.init( stage, "Distributeur-ihm1", ihm1 );
      stage.show();
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
