enumBody( namespace, enum ) ::= <<
#include \<<namespace; format="NamespaceToPath">/<enum.name; format="cname">.hpp>

namespace <namespace> {
   
   const char * toString( const <enum.name> & enm ) {
      switch( enm ) {
      <enum.literal:{literal|case <enum.name>::<literal.name; format="upper,width">: return "<literal.name; format="upper">";
}>      case <enum.name>::LAST: return "LAST";
      }
      return "???";
   }
}

>>
