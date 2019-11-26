package da;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface IDispatcher {

   void beforeDispatch() throws IOException;

   boolean hasDispatched( byte intrfc, byte event, SocketAddress from, ByteBuffer in ) throws IOException;

   void afterDispatch( boolean dispatched ) throws IOException;
}
