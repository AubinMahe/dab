package sc.ui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sc.ICarte;

public class Carte implements ICarte {

   private final StringProperty  _id              = new SimpleStringProperty();
   private final StringProperty  _code            = new SimpleStringProperty();
   private final StringProperty  _compte          = new SimpleStringProperty();
   private final IntegerProperty _expirationMonth = new SimpleIntegerProperty();
   private final IntegerProperty _expirationYear  = new SimpleIntegerProperty();
   private final IntegerProperty _nbEssais        = new SimpleIntegerProperty();

   private final Expiration _expiration = new Expiration( _expirationMonth, _expirationYear );

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

   public StringProperty codeProperty() {
      return _code;
   }

   public void setCode( String code ) {
      _code.set( code );
   }

   @Override
   public String getCode() {
      return _code.get();
   }

   public StringProperty compteProperty() {
      return _compte;
   }

   public void setCompte( String compte ) {
      _compte.set( compte );
   }

   @Override
   public String getCompte() {
      return _compte.get();
   }

   public IntegerProperty expirationMonthProperty() {
      return _expirationMonth;
   }

   public void setExpirationMonth( byte month ) {
      _expirationMonth.set( Math.min( Math.max( 1, month ), 12 ));
   }

   @Override
   public byte getExpirationMonth() {
      return (byte)_expirationMonth.get();
   }

   public IntegerProperty expirationYearProperty() {
      return _expirationYear;
   }

   public void setExpirationYear( short year ) {
      _expirationYear.set( year );
   }

   @Override
   public short getExpirationYear() {
      return (short)_expirationYear.get();
   }

   public Expiration getExpiration() {
      return _expiration;
   }

   public IntegerProperty nbEssaisProperty() {
      return _nbEssais;
   }

   public void setNbEssais( int essais ) {
      _nbEssais.set( essais );
   }

   @Override
   public void incrNbEssais() {
      _nbEssais.set( _nbEssais.get() + 1 );
   }

   @Override
   public byte getNbEssais() {
      return (byte)_nbEssais.get();
   }
}
