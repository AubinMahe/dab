package sc.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sc.ICompte;

public class Compte implements ICompte {

   private final StringProperty  _id       = new SimpleStringProperty( "" );
   private final DoubleProperty  _solde    = new SimpleDoubleProperty( 0.0 );
   private final BooleanProperty _autorise = new SimpleBooleanProperty( true );

   public StringProperty idProperty() {
      return _id;
   }

   public void setId( String id ) {
      _id.set( id );
   }

   @Override
   public String getId() {
      return _id.get();
   }

   public DoubleProperty soldeProperty() {
      return _solde;
   }

   public void setSolde( double solde ) {
      _solde.set( solde );
   }

   @Override
   public void retrait( double montant ) {
      setSolde( getSolde() - montant );
   }

   @Override
   public double getSolde() {
      return _solde.get();
   }

   public BooleanProperty autoriseProperty() {
      return _autorise;
   }

   public void setAutorise( boolean autorise ) {
      _autorise.set(autorise );
   }

   @Override
   public boolean getAutorise() {
      return _autorise.get();
   }

   @Override
   public void copyTo( dabtypes.Compte out ) {
      out.id       = getId();
      out.solde    = getSolde();
      out.autorise = getAutorise();
   }
}
