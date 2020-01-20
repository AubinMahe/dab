package da;

import java.nio.ByteBuffer;

public interface InstanceID {

   void put( ByteBuffer target );

   byte value();
}
