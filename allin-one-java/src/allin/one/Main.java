package allin.one;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;

import fx.IController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

   private static <T> void start( Stage stage, T component, Class<? extends IController<T>> uiClass, String title ) throws IOException, BackingStoreException {
      final FXMLLoader loader =
         new FXMLLoader( uiClass.getResource( "ui.fxml" ), ResourceBundle.getBundle( uiClass.getPackageName() + "/messages" ));
      stage.setScene( new Scene( loader.load()));
      stage.setTitle( title );
      final IController<T> ctrl = loader.getController();
      ctrl.init( stage, title, component );
      stage.show();
   }

   @Override
   public void start( Stage stage ) throws IOException, BackingStoreException {
      final ComponentFactory factory = new ComponentFactory();

      start( stage      , factory.getSc()  , hpms.sc .ui.Controller.class, "Banque-sc" );
      start( new Stage(), factory.getIhm1(), hpms.dab.ui.Controller.class, "Distributeur-ihm1" );
      start( new Stage(), factory.getIhm2(), hpms.dab.ui.Controller.class, "Distributeur-ihm2" );
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
