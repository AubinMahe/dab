responses( package, ifaceName, requests, types ) ::= <<
package <package>;
// responses

import java.io.IOException;

public interface I<ifaceName>Responses {

<requests:{request|
   public void <request.name>Response( <types.(request.type)> <request.name> ) throws IOException;
}>}

>>
