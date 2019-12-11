package sc.ui;

import java.util.ResourceBundle;

import hpms.sc.Banque;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

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
      stage.setTitle( "Site Central (" + name + ")" );
      final Class<? extends Main> clazz = getClass();
      final FXMLLoader loader =
         new FXMLLoader( clazz.getResource( "ui.fxml" ), ResourceBundle.getBundle( clazz.getPackageName() + "/messages" ));
      stage.setScene( new Scene( loader.load()));
      stage.setTitle( "Site Central '" + name + "'" );
      final Controller ctrl = loader.getController();
      final Banque instance;
      switch( name ) {
      case "isolated.sc": instance = new isolated.sc.ComponentFactory().getSc(); break;
//      case "mixed.sc"   : instance = new mixed.sc.ComponentFactory().getSc(); break;
      default: throw new IllegalStateException( "'" + name + "' isn't a valid deployement.process name" );
      }
      ctrl.init( stage, name, instance );
      stage.show();
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
