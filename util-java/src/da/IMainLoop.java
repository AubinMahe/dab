package da;

import java.io.IOException;

public interface IMainLoop {

   boolean isRunning();

   void terminate() throws IOException;
}
