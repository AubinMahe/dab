componentFactoryBodyDispatchersInit() ::= <<
//-- dispatchers init --
<process.instance:{instance|
_<instance.name; format="argument">Dispatcher = new <modules.(instance.component)>::<instance.component.name
      >Dispatcher( _running, INSTANCES_<instance.name; format="ID">, _socket, _<instance.name; format="argument"> );
}>
>>
