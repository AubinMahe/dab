requiredInterface( package, iface ) ::= <<
package <package>;
// requiredInterface

import java.io.IOException;

public interface I<iface.name> {

<iface.eventOrRequestOrData:{facet|< if( facet.isData )>< else>
   void <facet.name; format="argument">(<
if( facet.isRequest )><
      common/signature( facet.arguments.field )><
else><
      common/signature( facet.field )><
endif>) throws IOException;< endif>
}>
}

>>
