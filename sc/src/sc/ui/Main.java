package sc.ui;

import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

   @Override
   public void start( Stage stage ) throws Exception {
      final Map<String, String> named = getParameters().getNamed();
      boolean nan = false;
      try {
         Integer.parseInt( named.get( "sc-port" ));
      }
      catch( final Throwable t ) {
         nan = true;
      }
      if(  ( ! named.containsKey( "sc-port" ))
         || nan )
      {
         stage.setScene( new Scene( new BorderPane( new Label(
            "Arguments obligatoires :\n"
            + "\t--sc-port=<this port>"
         )), 420, 200 ));
         stage.setTitle( "Usage" );
         stage.show();
         return;
      }
      final int scPort = Integer.parseInt( named.get( "sc-port" ));
      stage.setTitle( "Site Central (SC)" );
      final Class<? extends Main> clazz = getClass();
      final FXMLLoader loader =
         new FXMLLoader(
            clazz.getResource( "ui.fxml" ),
            ResourceBundle.getBundle( clazz.getPackageName() + "/messages" ));
      stage.setScene( new Scene( loader.load()));
      final Controller ctrl = loader.getController();
      ctrl.init( stage, scPort );
      stage.show();
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
