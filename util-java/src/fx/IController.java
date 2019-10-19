package fx;

import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javafx.stage.Stage;

public interface IController {

   void init( Stage stage, String instanceName ) throws BackingStoreException, IOException;
}
