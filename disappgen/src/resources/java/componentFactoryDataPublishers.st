componentFactoryDataPublishers() ::= <<
//-- data publishers --
<dataPublishers:{proxy|_<proxy.from>.connect(
   new <proxy.module>.<proxy.interface>Publisher( _channel, Instances.<proxy.from; format="ID">,
      <proxy.to:{p|new Targets\<>( <p; format="argument">, <
         proxy.to.(p):{to|Instances.<to; format="ID">}; separator=", "> )}; separator=",\n">
   ));}; separator="\n">
>>
