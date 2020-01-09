componentFactoryProxiesInit() ::= <<
//-- proxies init --
<processesImpl:{process|
final InetSocketAddress <process.name; format="argument"> = new InetSocketAddress( "<process.address>", <process.port> );
}><proxies:{proxy|
final <proxy.module>.<proxy.interface> <proxy.interface; format="argument">For<proxy.from; format="cap"> =
   new <proxy.module>.<proxy.interface>( _channel, Instances.<proxy.from; format="ID">,
      <proxy.to:{p|new Targets\<>( <p; format="argument">, <
        proxy.to.(p):{to|Instances.<to; format="ID">}; separator=", "> )}; separator=",\n">
   );
}>
>>
