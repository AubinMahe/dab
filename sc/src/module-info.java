module sc {

   requires javafx.base;
   requires javafx.controls;
   requires javafx.fxml;
   requires transitive javafx.graphics;
   requires java.prefs;
   requires util;

   exports sc.ui;
   opens sc.ui to javafx.fxml;

   exports sc;
}
