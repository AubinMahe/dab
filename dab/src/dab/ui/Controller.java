package dab.ui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import dab.Etat;
import dab.IIHM;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Les informations détenues par le Site Central sont :
 *
 *  Les numéros de cartes connues de la banque.
 *  Le solde du compte correspondant à chaque numéro de carte.
 *  le statut du compte correspondant à chaque numéro de carte (autorisé ou interdit).
 */
public class Controller extends Thread implements IIHM {

   private Distributeur _component;
   private Etat         _etat   = Etat.HORS_SERVICE;
   private String       _text   = "";
   private String       _saisie = "";
   private double       _dernierMontantSaisi = 0.0;

   @FXML private Label            _status;
   @FXML private TextArea         _screen;
   @FXML private Pane             _numpad;
   @FXML private Pane             _right;
   @FXML private TextField        _carteID;
   @FXML private Button           _insererCarte;
   @FXML private CheckBox         _maintenance;
   @FXML private Pane             _maintenanceIHM;
   @FXML private Button           _prendreLesBillets;
   @FXML private Button           _prendreLaCarte;
   @FXML private CheckBox         _anomalie;
   @FXML private ListView<String> _corbeille;
   @FXML private ListView<String> _magasin;
   @FXML private Label            _caisse;
   @FXML private TextField        _ajouterALaCaisse;

   @Override
   public void etatDuDabPublished() {
      _etat = _component.getEtatDuDab().etat;
      _caisse.setText( "Caisse : " + NumberFormat.getCurrencyInstance().format( _component.getEtatDuDab().soldeCaisse ));
      _status.setText( "DAB en service" );
      switch( _etat ) {
      default:
         System.err.printf( "Etat DAB inattendu : %s\n", _etat );
         //$FALL-THROUGH$
      case HORS_SERVICE:
         _status.setText( "DAB hors service"   );
         setScreenText( "Distributeur indisponible." );
         break;
      case MAINTENANCE:
         _status.setText( "DAB en maintenance" );
         setScreenText( "Distributeur en maintenance, veuillez patienter." );
      break;
      case EN_SERVICE:
         setScreenText( "Veuillez insérer une carte..." );
         break;
      case LECTURE_CARTE:
         setScreenText( "Lecture de la carte, veuillez patienter." );
         break;
      case SAISIE_CODE_1:
         setScreenText( "Veuillez entrer le code de la carte, premier essai : " );
         break;
      case SAISIE_CODE_2:
         setScreenText( "Veuillez entrer le code de la carte, deuxième essai : " );
         break;
      case SAISIE_CODE_3:
         setScreenText( "Veuillez entrer le code de la carte, dernier essai : " );
         break;
      case SAISIE_MONTANT:
         setScreenText( "Veuillez entrer le montant du retrait : " );
      break;
      case RETRAIT_CARTE_BILLETS:
         setScreenText( "Veuillez prendre la carte pour obtenir les billets..." );
         break;
      case RETRAIT_CARTE_SOLDE_CAISSE:
         setScreenText( "Solde caisse insuffisant, veuillez reprendre votre carte..." );
         break;
      case RETRAIT_CARTE_SOLDE_COMPTE:
         setScreenText( "Solde compte insuffisant, veuillez reprendre votre carte..." );
         break;
      case RETRAIT_BILLETS:
         setScreenText( "Veuillez prendre les billets..." );
         break;
      }
      final boolean maintenance =     _etat == Etat.MAINTENANCE;
      _insererCarte     .setDisable(  _etat != Etat.EN_SERVICE );
      _maintenanceIHM   .setDisable( ! maintenance );
      _screen           .setDisable(   maintenance );
      _numpad           .setDisable(   maintenance );
      _right            .setDisable(   maintenance );
      _prendreLaCarte   .setDisable(( _etat != Etat.RETRAIT_CARTE_BILLETS      )
         &&                         ( _etat != Etat.RETRAIT_CARTE_SOLDE_CAISSE )
         &&                         ( _etat != Etat.RETRAIT_CARTE_SOLDE_COMPTE ));
      _prendreLesBillets.setDisable(  _etat != Etat.RETRAIT_BILLETS );
   }

   private void refreshScreen() {
      final boolean saisieCode =
         ( _etat == Etat.SAISIE_CODE_1 )||
         ( _etat == Etat.SAISIE_CODE_2 )||
         ( _etat == Etat.SAISIE_CODE_3 );
      final String user = saisieCode ? _saisie.replaceAll( ".", "*" ) : _saisie;
      _screen.setText( _text + user );
   }

   private void setScreenText( String text ) {
      _text = text;
      refreshScreen();
   }

   @Override
   public void ejecterLaCarte() {
      System.err.println( "Ejection de la carte..." );
   }

   @Override
   public void ejecterLesBillets( double montant ) {
      System.err.println( "Ejection des billets..." );
   }

   @Override
   public void confisquerLaCarte( ) {
      System.err.println( "Placement de la carte dans le magasin interne." );
      _magasin.getItems().add( "Carte n°" + _carteID.getText());
      _carteID.setText( null );
   }

   @Override
   public void placerLesBilletsDansLaCorbeille() {
      System.err.println(
         "Placement des billets oubliés dans la corbeille interne : " + _dernierMontantSaisi + ", carte " + _carteID.getText());
      _corbeille.getItems().add( _dernierMontantSaisi + " €, carte n°" + _carteID.getText());
      _carteID.setText( null );
   }

   @Override
   public void shutdown( ) {
      ((Stage)_status.getScene().getWindow()).close();
   }

   private void done( Stage stage, String instanceName ) {
      try {
         final Preferences prefs = Preferences.userNodeForPackage( getClass());
         prefs.putDouble( instanceName + "-x", stage.getX());
         prefs.putDouble( instanceName + "-y", stage.getY());
         _component.uniteDeTraitement().shutdown();
      }
      catch( final IOException e ){
         e.printStackTrace();
      }
   }

   public void init( Stage stage, String instanceName )
      throws BackingStoreException, IOException
   {
      final Preferences prefs = Preferences.userNodeForPackage( getClass());
      if( prefs.nodeExists( "" )) {
         stage.setX( prefs.getDouble( instanceName + "-x", -4.0 ));
         stage.setY( prefs.getDouble( instanceName + "-y", -4.0 ));
      }
      stage.setOnCloseRequest( e -> done( stage, instanceName ));
      _component = new Distributeur( instanceName, this );
      setDaemon( true );
      start();
   }

   @FXML
   private void key( ActionEvent ae ) throws IOException {
      final Button btn  = (Button)ae.getSource();
      final String text = btn.getText().strip();
      if( text.length() == 1 ) {
         _saisie += text;
      }
      else {
         switch( text.strip()) {
         case "Annuler":
            _saisie = "";
            _component.uniteDeTraitement().annulationDemandeeParLeClient();
            break;
         case "Effacer":
            if( _saisie.length() > 0 ) {
               _saisie = _saisie.substring( 0, _saisie.length() - 1 );
            }
            break;
         case "Entrer":
            final boolean saisieCode =
               ( _etat == Etat.SAISIE_CODE_1 )||
               ( _etat == Etat.SAISIE_CODE_2 )||
               ( _etat == Etat.SAISIE_CODE_3 );
            if( saisieCode ) {
               _component.uniteDeTraitement().codeSaisi( _saisie );
            }
            else if( _etat == Etat.SAISIE_MONTANT ) {
               _dernierMontantSaisi = Double.parseDouble( _saisie );
               _component.uniteDeTraitement().montantSaisi( _dernierMontantSaisi );
            }
            _saisie = "";
            break;
         }
      }
      refreshScreen();
   }

   @FXML
   private void carteInseree() throws IOException {
      _component.uniteDeTraitement().carteInseree( _carteID.getText());
   }

   @FXML
   private void maintenance() throws IOException {
      _component.uniteDeTraitement().maintenance( _maintenance.isSelected());
   }

   @FXML
   private void rechargerLaCaisse() throws IOException {
      _component.uniteDeTraitement().rechargerLaCaisse( Double.parseDouble( _ajouterALaCaisse.getText()));
   }

   @FXML
   private void prendreLaCarte() throws IOException {
      _component.uniteDeTraitement().carteRetiree();
   }

   @FXML
   private void prendreLesBillets() throws IOException {
      _component.uniteDeTraitement().billetsRetires();
   }

   @FXML
   private void anomalie() throws IOException {
      _component.uniteDeTraitement().anomalie( _anomalie.isSelected());
   }

   @FXML
   private void viderLaCorbeille() {
      _corbeille.getItems().clear();
   }

   @FXML
   private void viderLeMagasin() {
      _magasin.getItems().clear();
   }
}
