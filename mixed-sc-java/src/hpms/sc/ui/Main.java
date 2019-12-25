package hpms.sc.ui;

import java.util.ResourceBundle;

import hpms.sc.Banque;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mixed.sc.ComponentFactory;

public class Main extends Application {

   @Override
   public void start( Stage stage ) throws Exception {
      final Class<? extends Main> clazz = getClass();
      final FXMLLoader loader =
         new FXMLLoader( clazz.getResource( "ui.fxml" ), ResourceBundle.getBundle( clazz.getPackageName() + "/messages" ));
      stage.setScene( new Scene( loader.load()));
      stage.setTitle( "Site Central 'sc'" );
      final Controller ctrl = loader.getController();
      final Banque instance = new ComponentFactory().getSc();
      ctrl.init( stage, "Banque-sc", instance );
      stage.show();
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
