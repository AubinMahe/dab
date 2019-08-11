module dab {

   requires javafx.base;
   requires javafx.controls;
   requires javafx.fxml;
   requires transitive javafx.graphics;
   requires java.prefs;
   requires util;

   exports dab;
   exports dab.ui;
   opens dab.ui to javafx.fxml;
}
