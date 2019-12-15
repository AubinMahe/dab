package hpms.dab.ui;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;

import hpms.dab.Distributeur;
import isolated.ihm2.ComponentFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

   public void start( Distributeur component, Stage stage ) throws IOException, BackingStoreException {
      final Class<? extends Main> clazz = getClass();
      final FXMLLoader loader =
         new FXMLLoader(
            clazz.getResource( "ui.fxml" ),
            ResourceBundle.getBundle( clazz.getPackageName() + "/messages" ));
      stage.setScene( new Scene( loader.load()));
      stage.setTitle( "Distributeur - ihm2" );
      final Controller ctrl = loader.getController();
      ctrl.init( stage, "ihm2", component );
      stage.show();
   }

   @Override
   public void start( Stage stage ) throws Exception {
      final ComponentFactory factory   = new ComponentFactory();
      final Distributeur     component = factory.getIhm2();
      start( component, stage );
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
