componentImplementation(
   namespace, component, responses, requires, instancesByName, instances, processes, offData, reqData
) ::= <<
//componentImplementation
#include \<<namespace; format="NamespaceToPath">/<component.name>Component.hpp>

using namespace <namespace>;

< if( component.timeout )>
void <component.name>Component::loopback( <component.name>Dispatcher::LoopbackEvent event ) {
   _dispatcher->loopback( event );
}
< endif>

>>
