package sc.ui;

import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import fx.IController;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import sc.Banque;
import sc.ICarte;
import sc.ICompte;
import sc.IRepository;

/**
 * Les informations détenues par le Site Central sont :
 *
 *  Les numéros de cartes connues de la banque.
 *  Le solde du compte correspondant à chaque numéro de carte.
 *  le statut du compte correspondant à chaque numéro de carte (autorisé ou interdit).
 */
public class Controller implements IRepository, IController<Banque> {

   @FXML private TableView<Carte>  _cartes;
   @FXML private TableView<Compte> _comptes;

   private boolean _selectionInProgress;

   @SuppressWarnings("unused")
   private void carteSelected( ObservableValue<? extends Number> o, Number p, Number n ) {
      if( _selectionInProgress ) {
         return;
      }
      _selectionInProgress = true;
      final String                 compteID = _cartes .getItems().get( n.intValue()).getCompte();
      final ObservableList<Compte> comptes  = _comptes.getItems();
      for( int row = 0, count = comptes.size(); row < count; ++row ) {
         final ICompte compte = comptes.get( row );
         final String id     = compte.getId();
         if( id.equals( compteID )) {
            _comptes.getSelectionModel().clearAndSelect( row );
            break;
         }
      }
      _selectionInProgress = false;
   }

   @SuppressWarnings("unused")
   private void compteSelected( ObservableValue<? extends Number> o, Number p, Number n ) {
      if( _selectionInProgress ) {
         return;
      }
      _selectionInProgress = true;
      final String                compteID = _comptes.getItems().get( n.intValue()).getId();
      final ObservableList<Carte> cartes   = _cartes .getItems();
      _cartes.getSelectionModel().clearSelection();
      for( int row = 0, count = cartes.size(); row < count; ++row ) {
         final ICarte  carte = cartes.get( row );
         final String id    = carte.getCompte();
         if( id.equals( compteID )) {
            _cartes.getSelectionModel().select( row );
         }
      }
      _selectionInProgress = false;
   }

   @FXML
   @SuppressWarnings("unchecked")
   private void initialize() {
      final TableColumn<Carte, Integer> expirationMonth =
         (TableColumn<Carte, Integer>)_cartes.getColumns().get( 3 );
      expirationMonth.setCellFactory(
         TextFieldTableCell.forTableColumn( new IntegerStringConverter()));

      final TableColumn<Carte, Integer> expirationYear =
         (TableColumn<Carte, Integer>)_cartes.getColumns().get( 4 );
      expirationYear.setCellFactory(
         TextFieldTableCell.forTableColumn( new IntegerStringConverter()));

      final TableColumn<Carte, Integer> nbEssais =
         (TableColumn<Carte, Integer>)_cartes.getColumns().get( 5 );
      nbEssais.setCellFactory( TextFieldTableCell.forTableColumn( new IntegerStringConverter()));

      final TableColumn<Compte, Double> solde =
         (TableColumn<Compte, Double>)_comptes.getColumns().get( 1 );
      solde.setCellFactory( TextFieldTableCell.forTableColumn( new DoubleStringConverter()));

      final TableColumn<Compte, Boolean> autoriseColumn =
         (TableColumn<Compte, Boolean>)_comptes.getColumns().get( 2 );
      autoriseColumn.setCellFactory( CheckBoxTableCell.forTableColumn( autoriseColumn ));

      final TableViewSelectionModel<Carte> cartesSelModel = _cartes.getSelectionModel();
      cartesSelModel.setSelectionMode( SelectionMode.MULTIPLE );
      cartesSelModel.selectedIndexProperty().addListener( this::carteSelected );

      final TableViewSelectionModel<Compte> comptesSelModel = _comptes.getSelectionModel();
      comptesSelModel.selectedIndexProperty().addListener( this::compteSelected );
      _cartes.getItems().get(0).setExpirationMonth((byte )    6 );
      _cartes.getItems().get(0).setExpirationYear ((short) 2022 );
      _cartes.getItems().get(1).setExpirationMonth((byte )    1 );
      _cartes.getItems().get(1).setExpirationYear ((short) 2021 );
      _cartes.getItems().get(2).setExpirationMonth((byte )    2 );
      _cartes.getItems().get(2).setExpirationYear ((short) 2022 );
      _cartes.getItems().get(3).setExpirationMonth((byte )    3 );
      _cartes.getItems().get(3).setExpirationYear ((short) 2021 );
      _cartes.getItems().get(4).setExpirationMonth((byte )    4 );
      _cartes.getItems().get(4).setExpirationYear ((short) 2022 );
   }

   public void done( Stage stage ) {
      final Preferences prefs = Preferences.userNodeForPackage( getClass());
      prefs.putDouble( "x", stage.getX());
      prefs.putDouble( "y", stage.getY());
   }

   @Override
   public void init( Stage stage, String name, Banque component ) throws BackingStoreException, IOException {
      final Preferences prefs = Preferences.userNodeForPackage( getClass());
      if( prefs.nodeExists( "" )) {
         stage.setX( prefs.getDouble( "x", -4.0 ));
         stage.setY( prefs.getDouble( "y", -4.0 ));
      }
      stage.setOnCloseRequest( e -> done( stage ));
      component.setRepository( this );
   }

   @Override
   public Carte getCarte( String carteID ) {
      System.err.printf( getClass().getName() + ".getCarte|%s\n", carteID );
      final ObservableList<Carte> cartes = _cartes.getItems();
      for( int row = 0, count = cartes.size(); row < count; ++row ) {
         final Carte  carte = cartes.get( row );
         final String id    = carte.getId();
         if( id.equals( carteID )) {
            return carte;
         }
      }
      return null;
   }

   @Override
   public Compte getCompte( String carteID ) {
      System.err.printf( getClass().getName() + ".getCompte|carteID = %s\n", carteID );
      final ICarte carte = getCarte( carteID );
      if( carte != null ) {
         final String                 compteID = carte.getCompte();
         final ObservableList<Compte> comptes  = _comptes.getItems();
         for( int row = 0, count = comptes.size(); row < count; ++row ) {
            final Compte compte = comptes.get( row );
            final String id     = compte.getId();
            if( id.equals( compteID )) {
               return compte;
            }
         }
      }
      return null;
   }

   @Override
   public void close() {
      Platform.runLater(() -> ((Stage)_cartes.getScene().getWindow()).close());
   }
}
