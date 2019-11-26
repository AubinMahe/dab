package dab.ui;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;

import dab.Distributeur;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

   public void start( Distributeur component, String name, Stage stage ) throws IOException, BackingStoreException {
      final Class<? extends Main> clazz = getClass();
      final FXMLLoader loader =
         new FXMLLoader(
            clazz.getResource( "ui.fxml" ),
            ResourceBundle.getBundle( clazz.getPackageName() + "/messages" ));
      stage.setScene( new Scene( loader.load()));
      stage.setTitle( "IHM " + name );
      final Controller ctrl = loader.getController();
      ctrl.init( stage, name, component );
      stage.show();
   }

   @Override
   public void start( Stage stage ) throws Exception {
      final String name = getParameters().getNamed().get( "name" );
      if( name == null ) {
         stage.setScene(
            new Scene( new BorderPane( new Label(
               "Mandatory argument :\n\t--name=<instance name as defined in XML application file>\n" )),
            600, 200 ));
         stage.setTitle( getClass().getPackageName() + " usage" );
         stage.show();
         return;
      }
      final Distributeur component;
      switch( name ) {
      case "isolated.ihm1": component = isolated.ihm1.ComponentFactory.get().getIhm1(); break;
      case "isolated.ihm2": component = isolated.ihm2.ComponentFactory.get().getIhm2(); break;
      default: throw new IllegalStateException( name + " isn't a valid deployment.process name");
      }
      start( component, name, stage );
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
