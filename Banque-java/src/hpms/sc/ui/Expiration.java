package hpms.sc.ui;

import javafx.beans.property.IntegerProperty;

public class Expiration {

   private final IntegerProperty _expirationMonth;
   private final IntegerProperty _expirationYear;

   public Expiration( IntegerProperty expirationMonth, IntegerProperty expirationYear ) {
      _expirationMonth = expirationMonth;
      _expirationYear  = expirationYear;
   }

   public byte getMonth() {
      return (byte)_expirationMonth.get();
   }

   public short getYear() {
      return (short)_expirationYear.get();
   }
}
