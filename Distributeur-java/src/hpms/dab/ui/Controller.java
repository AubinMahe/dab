package hpms.dab.ui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import fx.IController;
import hpms.dab.Distributeur;
import hpms.dab.DistributeurComponent;
import hpms.dab.IIHM;
import hpms.dab.IUniteDeTraitementData;
import hpms.dabtypes.Etat;
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

public class Controller implements IIHM, IUniteDeTraitementData, IController<Distributeur> {

   private Distributeur _component;
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
      System.err.printf( "%s.etatDuDabPublished|etat = %s, soldeCaisse = %7.2f\n", getClass().getName(),
         _component.getEtatDuDab().etat, _component.getEtatDuDab().soldeCaisse );
      _caisse.setText( "Caisse : " + NumberFormat.getCurrencyInstance().format( _component.getEtatDuDab().soldeCaisse ));
      _status.setText( "DAB en service" );
      switch( _component.getEtatDuDab().etat ) {
      default:
         System.err.printf( "Etat DAB inattendu : %s\n", _component.getEtatDuDab().etat );
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
      final boolean maintenance =     _component.getEtatDuDab().etat == Etat.MAINTENANCE;
      _insererCarte     .setDisable(  _component.getEtatDuDab().etat != Etat.EN_SERVICE );
      _maintenanceIHM   .setDisable( ! maintenance );
      _screen           .setDisable(   maintenance );
      _numpad           .setDisable(   maintenance );
      _right            .setDisable(   maintenance );
      _prendreLaCarte   .setDisable(( _component.getEtatDuDab().etat != Etat.RETRAIT_CARTE_BILLETS      )
         &&                         ( _component.getEtatDuDab().etat != Etat.RETRAIT_CARTE_SOLDE_CAISSE )
         &&                         ( _component.getEtatDuDab().etat != Etat.RETRAIT_CARTE_SOLDE_COMPTE ));
      _prendreLesBillets.setDisable(  _component.getEtatDuDab().etat != Etat.RETRAIT_BILLETS );
   }

   private void refreshScreen() {
      final boolean saisieCode =
         ( _component.getEtatDuDab().etat == Etat.SAISIE_CODE_1 )||
         ( _component.getEtatDuDab().etat == Etat.SAISIE_CODE_2 )||
         ( _component.getEtatDuDab().etat == Etat.SAISIE_CODE_3 );
      final String user = saisieCode ? _saisie.replaceAll( ".", "*" ) : _saisie;
      _screen.setText( _text + user );
   }

   private void setScreenText( String text ) {
      _text = text;
      refreshScreen();
   }

   @Override
   public void ejecterLaCarte() {
      System.err.printf( "%s.ejecterLaCarte\n", getClass().getName());
   }

   @Override
   public void ejecterLesBillets( double montant ) {
      System.err.printf( "%s.ejecterLesBillets\n", getClass().getName());
   }

   @Override
   public void confisquerLaCarte( ) {
      System.err.printf( "%s.confisquerLaCarte\n", getClass().getName());
      _magasin.getItems().add( "Carte n°" + _carteID.getText());
      _carteID.setText( "" );
   }

   @Override
   public void placerLesBilletsDansLaCorbeille() {
      System.err.printf( "%s.placerLesBilletsDansLaCorbeille|%7.2f, carte : %s\n", getClass().getName(),
         _dernierMontantSaisi, _carteID.getText());
      _corbeille.getItems().add( _dernierMontantSaisi + " €, carte n°" + _carteID.getText());
      _carteID.setText( "" );
   }

   @Override
   public void arret( ) {
      ((Stage)_status.getScene().getWindow()).close();
   }

   private void done( Stage stage, String instanceName ) {
      try {
         final Preferences prefs = Preferences.userNodeForPackage( getClass());
         prefs.putDouble( instanceName + "-x", stage.getX());
         prefs.putDouble( instanceName + "-y", stage.getY());
         _component.getUniteDeTraitement().arret();
      }
      catch( final IOException e ){
         e.printStackTrace();
      }
   }

   @Override
   public void init( Stage stage, String instanceName, Distributeur component ) throws BackingStoreException, IOException {
      final Preferences prefs = Preferences.userNodeForPackage( getClass());
      if( prefs.nodeExists( "" )) {
         stage.setX( prefs.getDouble( instanceName + "-x", -4.0 ));
         stage.setY( prefs.getDouble( instanceName + "-y", -4.0 ));
      }
      stage.setOnCloseRequest( e -> done( stage, instanceName ));
      _component = component;
      _component.setController( this );
      _carteID.textProperty().addListener( e -> _insererCarte.setDisable( _carteID.getText().isBlank()));
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
            _component.getUniteDeTraitement().annulationDemandeeParLeClient();
            break;
         case "Effacer":
            if( _saisie.length() > 0 ) {
               _saisie = _saisie.substring( 0, _saisie.length() - 1 );
            }
            break;
         case "Entrer":
            final boolean saisieCode =
               ( _component.getEtatDuDab().etat == Etat.SAISIE_CODE_1 )||
               ( _component.getEtatDuDab().etat == Etat.SAISIE_CODE_2 )||
               ( _component.getEtatDuDab().etat == Etat.SAISIE_CODE_3 );
            if( saisieCode ) {
               _component.getUniteDeTraitement().codeSaisi( _saisie );
            }
            else if( _component.getEtatDuDab().etat == Etat.SAISIE_MONTANT ) {
               _dernierMontantSaisi = Double.parseDouble( _saisie );
               _component.getUniteDeTraitement().montantSaisi( _dernierMontantSaisi );
            }
            _saisie = "";
            break;
         }
      }
      refreshScreen();
   }

   @FXML
   private void carteInseree() throws IOException {
      _component.getUniteDeTraitement().carteInseree( _carteID.getText());
   }

   @FXML
   private void maintenance() throws IOException {
      _component.getMaintenable().maintenance( _maintenance.isSelected());
   }

   @FXML
   private void rechargerLaCaisse() throws IOException {
      final double montant = Double.parseDouble( _ajouterALaCaisse.getText());
      _component.getUniteDeTraitement().rechargerLaCaisse( montant );
   }

   @FXML
   private void prendreLaCarte() throws IOException {
      _component.getUniteDeTraitement().carteRetiree();
   }

   @FXML
   private void prendreLesBillets() throws IOException {
      _component.getUniteDeTraitement().billetsRetires();
   }

   @FXML
   private void anomalie() throws IOException {
      _component.getUniteDeTraitement().anomalie( _anomalie.isSelected());
   }

   @FXML
   private void viderLaCorbeille() {
      _corbeille.getItems().clear();
   }

   @FXML
   private void viderLeMagasin() {
      _magasin.getItems().clear();
   }

   public DistributeurComponent getComponent() {
      return _component;
   }
}
