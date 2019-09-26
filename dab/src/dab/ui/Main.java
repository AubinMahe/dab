package dab.ui;

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
         Integer.parseInt( named.get( "dab-port" ));
         Integer.parseInt( named.get( "udt-port" ));
      }
      catch( final Throwable t ) {
         nan = true;
      }
      if(  ( ! named.containsKey( "id"          ))
         ||( ! named.containsKey( "dab-port"    ))
         ||( ! named.containsKey( "udt-address" ))
         ||( ! named.containsKey( "udt-port"    ))
         || nan )
      {
         stage.setScene( new Scene( new BorderPane( new Label(
            "Arguments obligatoires :\n"
            + "\t--id=<text>\n"
            + "\t--udt-address=<hôte de l'unité de traitement>\n"
            + "\t--udt-port=<port de l'unité de traitement>\n"
            + "\t--dab-port=<this port>"
         )), 420, 200 ));
         stage.setTitle( getClass().getPackageName() + " usage" );
         stage.show();
         return;
      }
      final String dabID      = named.get( "id" );
      final int    dabPort    = Integer.parseInt( named.get( "dab-port" ));
      final String udtAddress =                   named.get( "udt-address" );
      final int    udtPort    = Integer.parseInt( named.get( "udt-port" ));
      stage.setTitle( "IHM du DAB '" + dabID + "'" );
      final Class<? extends Main> clazz = getClass();
      final FXMLLoader loader =
         new FXMLLoader(
            clazz.getResource( "ui.fxml" ),
            ResourceBundle.getBundle( clazz.getPackageName() + "/messages" ));
      stage.setScene( new Scene( loader.load()));
      final Controller ctrl = loader.getController();
      ctrl.init( stage, dabPort, udtAddress, udtPort );
      stage.show();
   }

   public static void main( String[] args ) {
      launch( args );
   }
}
