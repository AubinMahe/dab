dataReaderHeader( namespace, interface, data  ) ::= <<
#pragma once
// dataReader

namespace <namespace> {

   class I<interface.name>Data {
   public:

      I<interface.name>Data( void ) = default;
      virtual ~ I<interface.name>Data( void ) = default;

   public:

<data:{dataType|
      virtual void <dataType.name; format="argument">Published( void ) = 0;
}>
   };
}

>>
