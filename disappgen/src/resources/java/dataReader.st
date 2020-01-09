dataReader( package, interface, data  ) ::= <<
package <package>;
// dataReader

import java.io.IOException;

public interface I<interface.name>Data {

<data:{dataType|
   void <dataType.name; format="argument">Published() throws IOException;
}>}

>>
