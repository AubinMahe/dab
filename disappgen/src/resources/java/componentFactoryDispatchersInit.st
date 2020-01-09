componentFactoryDispatchersInit() ::= <<
//-- dispatchers init --
<process.instance:{instance|
_<instance.name; format="argument">Dispatcher = new <
 modules.(instance.component)>.<instance.component.name>Dispatcher( _channel, _<instance.name; format="argument"> );
}>
>>
