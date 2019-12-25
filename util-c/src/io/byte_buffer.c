#ifdef _WIN32
#  include <os/win32.h>
#else
#  include <arpa/inet.h>
#endif

#include <io/byte_buffer.h>

#include <string.h>

#ifdef _MSC_VER
#  define ssize_t int
#endif

static bool init            = true;
static bool hostIsBigEndian = false;

util_error io_byte_buffer_wrap( io_byte_buffer * This, size_t capacity, byte * array ) {
   if( ! This || ! array ) {
      return UTIL_NULL_ARG;
   }
   This->order    = io_byte_order_BIG_ENDIAN;
   This->position = 0;
   This->limit    = capacity;
   This->capacity = capacity;
   This->mark     = capacity + 1;
   This->bytes    = array;
   memset( This->bytes, 0, capacity );
   if( init ) {
      init            = false;
      hostIsBigEndian = ( htonl(1) == 1 );
   }
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_array( io_byte_buffer * This, byte ** array ) {
   if( ! This || ! array ) {
      return UTIL_NULL_ARG;
   }
   *array = This->bytes;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_clear( io_byte_buffer * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   This->position = 0;
   This->limit    = This->capacity;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_mark( io_byte_buffer * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   This->mark = This->position;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_reset( io_byte_buffer * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   if( This->mark < This->limit ) {
      This->position = This->mark;
      This->mark     = This->capacity + 1;
      return UTIL_NO_ERROR;
   }
   return UTIL_NOT_APPLICABLE;
}

util_error io_byte_buffer_rewind( io_byte_buffer * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   This->position = 0;
   This->mark     = This->capacity + 1;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_flip( io_byte_buffer * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   This->limit    = This->position;
   This->position = 0;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_get_position( io_byte_buffer * This, size_t * position ) {
   if( ! This || ! position ) {
      return UTIL_NULL_ARG;
   }
   *position = This->position;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_set_position( io_byte_buffer * This, size_t position ) {
   if( ! This || ! position ) {
      return UTIL_NULL_ARG;
   }
   if( position <= This->limit ) {
      This->position = position;
      return UTIL_NO_ERROR;
   }
   return UTIL_OVERFLOW;
}

util_error io_byte_buffer_getLimit( io_byte_buffer * This, size_t * limit ) {
   if( ! This || ! limit ) {
      return UTIL_NULL_ARG;
   }
   *limit = This->limit;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_remaining( io_byte_buffer * This, size_t * remaining ) {
   if( ! This || ! remaining ) {
      return UTIL_NULL_ARG;
   }
   *remaining = This->limit - This->position;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_put( io_byte_buffer * This, const byte * src, size_t from, size_t to ) {
   if( ! This || ! src ) {
      return UTIL_NULL_ARG;
   }
   const size_t count = to - from;
   if( This->position + count > This->limit ) {
      return UTIL_OVERFLOW;
   }
   memcpy( This->bytes + This->position, src + from, count );
   This->position += count;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_get( io_byte_buffer * This, byte * target, size_t from, size_t to ) {
   if( ! This || ! target ) {
      return UTIL_NULL_ARG;
   }
   const size_t count = to - from;
   if( This->position + count > This->limit ) {
      return UTIL_UNDERFLOW;
   }
   memcpy( target+from, This->bytes + This->position, count );
   This->position += count;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_put_byte( io_byte_buffer * This, byte value ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   if( This->position + 1 > This->limit ) {
      return UTIL_OVERFLOW;
   }
   This->bytes[This->position] = value;
   This->position += 1;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_get_byte( io_byte_buffer * This, byte * target ) {
   if( ! This || ! target ) {
      return UTIL_NULL_ARG;
   }
   if( This->position + 1 > This->limit ) {
      return UTIL_UNDERFLOW;
   }
   *target = This->bytes[This->position];
   This->position += 1;
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_put_bool( io_byte_buffer * This, bool value ) {
   return io_byte_buffer_put_byte( This, value ? 1 : 0 );
}

util_error io_byte_buffer_get_bool( io_byte_buffer * This, bool * value ) {
   byte b;
   util_error err = io_byte_buffer_get_byte( This, &b );
   if( UTIL_NO_ERROR == err ) {
      *value = ( b != 0 );
   }
   return err;
}

/* Endianness solution:
 * http://stackoverflow.com/questions/2182002/convert-big-endian-to-little-endian-in-c-without-using-provided-func
 */

util_error io_byte_buffer_put_short( io_byte_buffer * This, short value ) {
   return io_byte_buffer_put_ushort( This, (unsigned short)value );
}

util_error io_byte_buffer_get_short( io_byte_buffer * This, short * target ) {
   return io_byte_buffer_get_ushort( This, (unsigned short *)target );
}

util_error io_byte_buffer_put_ushort( io_byte_buffer * This, unsigned short value ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   if( This->position + sizeof( unsigned short ) > This->limit ) {
      return UTIL_OVERFLOW;
   }
   if( ( This->order == io_byte_order_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( This->order == io_byte_order_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (unsigned short)((( value & 0xFF00 ) >> 8 )|(( value & 0x00FF ) << 8 ));
   }
   memcpy( This->bytes + This->position, &value, sizeof( unsigned short ));
   This->position += sizeof( unsigned short );
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_get_ushort( io_byte_buffer * This, unsigned short * target ) {
   if( ! This || ! target ) {
      return UTIL_NULL_ARG;
   }
   unsigned short value;
   if( This->position + sizeof( unsigned short ) > This->limit ) {
      return UTIL_UNDERFLOW;
   }
   memcpy( &value, This->bytes + This->position, sizeof( unsigned short ));
   if( ( This->order == io_byte_order_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( This->order == io_byte_order_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (unsigned short)(
             (( value & 0xFF00 ) >> 8 )
            |(( value & 0x00FF ) << 8 ));
   }
   *target = value;
   This->position += sizeof( short );
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_put_int( io_byte_buffer * This, int value ) {
   return io_byte_buffer_put_uint( This, (unsigned int)value );
}

util_error io_byte_buffer_get_int( io_byte_buffer * This, int * target ) {
   return io_byte_buffer_get_uint( This, (unsigned int *)target );
}

util_error io_byte_buffer_put_uint( io_byte_buffer * This, unsigned int value ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   if( This->position + sizeof( unsigned int ) > This->limit ) {
      return UTIL_OVERFLOW;
   }
   if( ( This->order == io_byte_order_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( This->order == io_byte_order_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (( value & 0xFF000000 ) >> 24 )
             |(( value & 0x00FF0000 ) >>  8 )
             |(( value & 0x0000FF00 ) <<  8 )
             |(  value                << 24 );
   }
   memcpy( This->bytes + This->position, &value, sizeof( unsigned int ));
   This->position += sizeof( unsigned int );
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_put_uintAt( io_byte_buffer * This, unsigned int value, size_t index ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   if( index + sizeof( unsigned int ) > This->limit ) {
      return UTIL_OVERFLOW;
   }
   if( ( This->order == io_byte_order_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( This->order == io_byte_order_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (( value & 0xFF000000 ) >> 24 )
             |(( value & 0x00FF0000 ) >>  8 )
             |(( value & 0x0000FF00 ) <<  8 )
             |(  value                << 24 );
   }
   memcpy( This->bytes + index, &value, sizeof( unsigned int ));
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_get_uint( io_byte_buffer * This, unsigned int * target ) {
   if( ! This || ! target ) {
      return UTIL_NULL_ARG;
   }
   unsigned int       value;
   if( This->position + sizeof( unsigned int ) > This->limit ) {
      return UTIL_UNDERFLOW;
   }
   memcpy( &value, This->bytes + This->position, sizeof( int ));
   if( ( This->order == io_byte_order_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( This->order == io_byte_order_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (( value & 0xFF000000 ) >> 24 )
             |(( value & 0x00FF0000 ) >>  8 )
             |(( value & 0x0000FF00 ) <<  8 )
             |(  value                << 24 );
   }
   *target = value;
   This->position += sizeof( unsigned int );
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_put_long( io_byte_buffer * This, int64_t value ) {
   return io_byte_buffer_put_ulong( This, (uint64_t)value );
}

util_error io_byte_buffer_get_long( io_byte_buffer * This, int64_t * target ) {
   return io_byte_buffer_get_ulong( This, (uint64_t *)target );
}

util_error io_byte_buffer_put_ulong( io_byte_buffer * This, uint64_t value ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   if( This->position + sizeof( uint64_t ) > This->limit ) {
      return UTIL_OVERFLOW;
   }
   if( ( This->order == io_byte_order_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( This->order == io_byte_order_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (( value & 0xFF00000000000000LL ) >> 56 )
             |(( value & 0x00FF000000000000LL ) >> 40 )
             |(( value & 0x0000FF0000000000LL ) >> 24 )
             |(( value & 0x000000FF00000000LL ) >>  8 )
             |(( value & 0x00000000FF000000LL ) <<  8 )
             |(( value & 0x0000000000FF0000LL ) << 24 )
             |(( value & 0x000000000000FF00LL ) << 40 )
             |(  value                          << 56 );
   }
   memcpy( This->bytes + This->position, &value, sizeof( uint64_t ));
   This->position += sizeof( uint64_t );
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_get_ulong( io_byte_buffer * This, uint64_t * target ) {
   if( ! This || ! target ) {
      return UTIL_NULL_ARG;
   }
   uint64_t value;
   if( This->position + sizeof( uint64_t ) > This->limit ) {
      return UTIL_UNDERFLOW;
   }
   memcpy( &value, This->bytes + This->position, sizeof( int64_t ));
   if( ( This->order == io_byte_order_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( This->order == io_byte_order_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (( value & 0xFF00000000000000LL ) >> 56 )
             |(( value & 0x00FF000000000000LL ) >> 40 )
             |(( value & 0x0000FF0000000000LL ) >> 24 )
             |(( value & 0x000000FF00000000LL ) >>  8 )
             |(( value & 0x00000000FF000000LL ) <<  8 )
             |(( value & 0x0000000000FF0000LL ) << 24 )
             |(( value & 0x000000000000FF00LL ) << 40 )
             |(  value                          << 56 );
   }
   *target = value;
   This->position += sizeof( uint64_t );
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_put_float( io_byte_buffer * This, float value ) {
   void * ptr = &value;
   return io_byte_buffer_put_uint( This, *(unsigned int*)ptr );
}

util_error io_byte_buffer_get_float( io_byte_buffer * This, float * target ) {
   return io_byte_buffer_get_uint( This, (unsigned int *)target );
}

util_error io_byte_buffer_put_double( io_byte_buffer * This, double value ) {
   void * ptr = &value;
   return io_byte_buffer_put_ulong( This, *(uint64_t*)ptr );
}

util_error io_byte_buffer_get_double( io_byte_buffer * This, double * target ) {
   return io_byte_buffer_get_ulong( This, (uint64_t *)target );
}

util_error io_byte_buffer_put_string( io_byte_buffer * This, const char * source ) {
   if( ! This || ! source ) {
      return UTIL_NULL_ARG;
   }
   size_t     len    = strlen( source );
   util_error ret = io_byte_buffer_put_uint( This, (unsigned int)len );
   if( UTIL_NO_ERROR != ret ) {
      return ret;
   }
   return io_byte_buffer_put( This, (const byte *)source, 0U, len );
}

util_error io_byte_buffer_get_string( io_byte_buffer * This, char * dest, size_t size ) {
   if( ! This || ! dest ) {
      return UTIL_NULL_ARG;
   }
   unsigned int len = 0U;
   util_error   ret = io_byte_buffer_get_uint( This, &len );
   if( UTIL_NO_ERROR != ret ) {
      return ret;
   }
   if( len >= size ) {
      return UTIL_UNDERFLOW;
   }
   ret = io_byte_buffer_get( This, (byte *)dest, 0, len );
   if( UTIL_NO_ERROR != ret ) {
      return ret;
   }
   dest[len] = '\0';
   return UTIL_NO_ERROR;
}

util_error io_byte_buffer_putBuffer( io_byte_buffer * This, io_byte_buffer * source ) {
   if( ! This || ! source ) {
      return UTIL_NULL_ARG;
   }
   size_t count = source->limit - source->position;
   if( This->position + count > This->limit ) {
      return UTIL_OVERFLOW;
   }
   memcpy( This->bytes + This->position, source->bytes + source->position, count );
   source->position += count;
   This  ->position += count;
   return UTIL_NO_ERROR;
}
