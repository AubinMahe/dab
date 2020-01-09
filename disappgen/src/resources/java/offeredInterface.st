offeredInterface( package, ifaceName, facets, types ) ::= <<
package <package>;
// offeredInterface

import java.io.IOException;

public interface I<ifaceName> {

<facets:{facet|<
if( facet.isData )><
else>
   void <facet.name>(<
   if( facet.isRequest )><
      if( facet.arguments )> <common/signature( facet.arguments.field )>, <types.(facet.type)> <facet.name> <
      else> <types.(facet.type)> <facet.name> <
      endif><
   else><common/signature( facet.field )><
   endif>) throws IOException;<
endif>
}>
}

>>
