componentFactoryInstancesConnect() ::= <<
//-- instances connect --
<process.instance:{instance|
<instance.requires:{pair|_<instance.name; format="argument">.connect( <
    pair.interface.name; format="argument">For<instance.name; format="cap"> );
}>}>
>>
