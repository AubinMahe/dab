componentImplementation( namespace, ifacesNamespace, component ) ::= <<
//componentImplementation
#include \<<namespace; format="NamespaceToPath">/<component.name>Component.hpp>

using namespace <namespace>;

< if( component.timeout )>
void <component.name>Component::timeout( <ifacesNamespace>::TimeoutInterface::Facets event ) {
   _dispatcher->timeout( event );
}
< endif>

>>
