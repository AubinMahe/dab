publisherBodyMethodBody() ::= <<
byte           raw[13];
io::ByteBuffer out( raw, sizeof( raw ));
hpms::interfaces::<iface.name>Interface::<facet.name; format="argument">( <
      facet.name; format="argument">, _instance, _instance, out );
for( unsigned i = 0; i \< _targetsCount; ++i ) {
   da::Targets * targets = _targets + i;
   for( unsigned j = 0; j \< targets->instancesCount; ++j ) {
      const da::InstanceID & to = targets->instances[j];
      out.rewind();
      out.putByte( 2, (byte)to );
      _socket.sendTo( out, targets->process );
      unsigned count = (unsigned)out.remaining();
      UTIL_LOG_ARGS( "%u byte(s) sent from instance %s to instance %s @ %s",
         count, _instance.toString(), to.toString(), ::inet_ntoa( targets->process.sin_addr ));
   }
}
>>
