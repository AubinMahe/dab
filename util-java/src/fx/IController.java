package fx;

import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javafx.stage.Stage;

public interface IController<T> {

   void init( Stage stage, String instanceName, T instance ) throws BackingStoreException, IOException;
}
