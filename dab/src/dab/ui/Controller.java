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

   @FXML private Label     _status;
   @FXML private Pane      _left;
   @FXML private Pane      _right;
   @FXML private TextArea  _screen;
   @FXML private TextField _carteID;
   @FXML private Button    _insererCarte;
   @FXML private Label     _etatDuDistributeur;
   @FXML private CheckBox  _maintenance;
   @FXML private Pane      _maintenanceIHM;
   @FXML private Button    _prendreLesBillets;
   @FXML private Button    _prendreLaCarte;
   @FXML private CheckBox  _anomalie;
   @FXML private Label     _caisse;
   @FXML private TextField _ajouterALaCaisse;

   @Override
   public void setStatus( Etat etat ) {
      _etat = etat;
      _insererCarte.setDisable( etat != Etat.EN_SERVICE );
      final boolean maintenance = ( etat == Etat.MAINTENANCE );
      final boolean hs          = ( etat == Etat.HORS_SERVICE );
      if( ! ( maintenance || hs )) {
         _status.setText( "DAB en service" );
      }
      switch( etat ) {
      default:
         System.err.printf( "Etat DAB inattendu : %s\n", etat );
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
      _maintenanceIHM.setVisible( maintenance );
      _left          .setDisable( maintenance );
      _right         .setDisable( maintenance );
      if(  ( etat == Etat.RETRAIT_CARTE_BILLETS      )
         ||( etat == Etat.RETRAIT_CARTE_SOLDE_CAISSE )
         ||( etat == Etat.RETRAIT_CARTE_SOLDE_COMPTE ))
      {
         _etatDuDistributeur.setText( "Fermé" );
         _prendreLaCarte   .setVisible( true );
         _prendreLesBillets.setVisible( false );
      }
      else if( etat == Etat.RETRAIT_BILLETS ) {
         _etatDuDistributeur.setText( "Ouvert" );
         _prendreLaCarte   .setVisible( false );
         _prendreLesBillets.setVisible( true );
      }
      else {
         _etatDuDistributeur.setText( "Fermé" );
         _prendreLaCarte   .setVisible( false );
         _prendreLesBillets.setVisible( false );
      }
   }

   @Override
   public void setSoldeCaisse( double valeur ) {
      _caisse.setText( "Caisse : " + NumberFormat.getCurrencyInstance().format( valeur ));
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
   public void confisquerLaCarte( ) {
      // TODO Faire apparaître la carte dans le magasin des cartes confisquées.
   }

   @Override
   public void ejecterLaCarte() {
      // TODO Afficher un message sur deux lignes
   }

   @Override
   public void shutdown( ) {
      ((Stage)_status.getScene().getWindow()).close();
   }

   private void done( Stage stage, String instanceName ) {
      final Preferences prefs = Preferences.userNodeForPackage( getClass());
      prefs.putDouble( instanceName + "-x", stage.getX());
      prefs.putDouble( instanceName + "-y", stage.getY());
      _component.done();
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
   private void key( ActionEvent ae ) {
      final Button btn  = (Button)ae.getSource();
      final String text = btn.getText().strip();
      if( text.length() == 1 ) {
         _saisie += text;
      }
      else {
         switch( text.strip()) {
         case "Annuler":
            _saisie = "";
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
               _component.codeSaisi( _saisie );
            }
            else if( _etat == Etat.SAISIE_MONTANT ) {
               _component.montantSaisi( Double.parseDouble( _saisie ));
            }
            _saisie = "";
            break;
         }
      }
      refreshScreen();
   }

   @FXML
   private void carteInseree() {
      _component.carteInseree( _carteID.getText());
   }

   @FXML
   private void maintenance() {
      _component.maintenance( _maintenance.isSelected());
   }

   @FXML
   private void rechargerLaCaisse() {
      _component.rechargerLaCaisse( Double.parseDouble( _ajouterALaCaisse.getText()));
   }

   @FXML
   private void prendreLaCarte() {
      _component.carteRetiree();
   }

   @FXML
   private void prendreLesBillets() {
      _component.billetsRetires();
   }

   @FXML
   private void anomalie() {
      _component.anomalie( _anomalie.isSelected());
   }
}
