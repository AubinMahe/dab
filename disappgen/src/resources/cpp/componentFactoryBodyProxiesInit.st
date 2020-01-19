componentFactoryBodyProxiesInit() ::= <<
//-- proxies init --
<processesImpl:{process|
sockaddr_in <process.name; format="argument">;
io::DatagramSocket::init( "<process.address>", <process.port>, <process.name> );
}><proxies:{proxy|
{
<proxy.to:{process|
   static da::InstanceID to<i0>[< length( proxy.to.(process))>];
}>   static da::Targets  targets[< length( proxy.to )>];
<proxy.to:{process|
   targets[<i0>].process        = <process>;
   targets[<i0>].instancesCount = < length(proxy.to.(process))>;
   targets[<i0>].instances      = to<i0>;
}>   <proxy.to:{process|<componentFactoryInitProxy( i0, proxy.to.(process) )
>}>   _<proxy.interface; format="argument">For<proxy.from; format="cap"> = new <
proxy.module>::<proxy.interface>( _socket, <proxy.from; format="ID">, <length(proxy.to)>, targets );
   _<proxy.from; format="argument">.connect( *_<proxy.interface; format="argument">For<proxy.from; format="cap"> );
\}
}><dataPublishers:{proxy|
{
<proxy.to:{process|
   static da::InstanceID to<i0>[< length( proxy.to.(process))>];
}>   static da::Targets  targets[< length( proxy.to )>];
<proxy.to:{process|
   targets[<i0>].process        = <process>;
   targets[<i0>].instancesCount = < length(proxy.to.(process))>;
   targets[<i0>].instances      = to<i0>;
}>   <proxy.to:{process|<componentFactoryInitProxy( i0, proxy.to.(process) )
>}>   _<proxy.interface; format="argument">Publisher = new <
proxy.module>::<proxy.interface>Publisher( _socket, <proxy.from; format="ID">, <length(proxy.to)>, targets );
   _<proxy.from; format="argument">.connect( *_<proxy.interface; format="argument">Publisher );
\}
}>
>>
