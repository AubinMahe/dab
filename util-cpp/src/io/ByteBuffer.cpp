#include <io/ByteBuffer.hpp>
#include <os/StdApiException.hpp>

#include <stdexcept>

#include <string.h>

using namespace io;

#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  include <winsock2.h>
#else
#  include <arpa/inet.h>
#endif

#ifdef _MSC_VER
#  define ssize_t int
#endif

static bool init            = true;
static bool hostIsBigEndian = false;

static const char * buildWhat( const char * file, unsigned line, const char * classMethod, const char * message ) {
   static char what[1000] = "";
   ::sprintf( what, "%s:%d: %s:%s\n", file, line, classMethod, message );
   return what;
}

ByteBuffer::ByteBuffer( size_t capacity, byte * array ) :
   _order   ( ByteOrder_BIG_ENDIAN ),
   _position( 0 ),
   _limit   ( capacity ),
   _capacity( capacity ),
   _mark    ( capacity + 1 ),
   _bytes   ( array ? array : new byte[capacity] )
{
   if( init ) {
      init            = false;
      hostIsBigEndian = ( htonl(1) == 1 );
   }
   memset( _bytes, 0, _capacity );
}

ByteBuffer::ByteBuffer( const ByteBuffer & right ) :
   _order   ( right._order ),
   _position( right._position ),
   _limit   ( right._limit ),
   _capacity( right._capacity ),
   _mark    ( right._mark ),
   _bytes   ( new byte[right._capacity] )
{
   if( init ) {
      init            = false;
      hostIsBigEndian = ( htonl(1) == 1 );
   }
   memcpy( _bytes, right._bytes, _capacity );
}

ByteBuffer & ByteBuffer::operator = ( const ByteBuffer & right ) {
   delete [] _bytes;
   _order    = right._order;
   _position = right._position;
   _limit    = right._limit;
   _capacity = right._capacity ;
   _mark     = right._mark;
   _bytes    = new byte[right._capacity];
   memcpy( _bytes, right._bytes, _capacity );
   return *this;
}

ByteBuffer:: ~ ByteBuffer( void ) {
   delete [] _bytes;
}

byte * ByteBuffer::array( void ) {
   return _bytes;
}

ByteBuffer & ByteBuffer::clear( void ) {
   _position = 0;
   _limit    = _capacity;
   _mark     = _capacity + 1;
   return *this;
}

ByteBuffer & ByteBuffer::mark( void ) {
   _mark = _position;
   return *this;
}

ByteBuffer & ByteBuffer::reset( void ) {
   if( _mark < _limit ) {
      _position = _mark;
   }
   else {
      throw std::runtime_error( buildWhat( __FILE__, __LINE__, "ByteBuffer.reset", "invalid mark" ));
   }
   return *this;
}

ByteBuffer & ByteBuffer::flip( void ) {
   _limit    = _position;
   _position = 0;
   return *this;
}

size_t ByteBuffer::position( void ) const {
   return _position;
}

ByteBuffer & ByteBuffer::position( size_t position ) {
   if( position <= _limit ) {
      _position = position;
   }
   else {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.position", "OVERFLOW" ));
   }
   return *this;
}

size_t ByteBuffer::limit( void ) const {
   return _limit;
}

size_t ByteBuffer::remaining( void ) const {
   return _limit - _position;
}

ByteBuffer & ByteBuffer::put( const byte * src, size_t from, size_t to ) {
   const size_t count = to - from;
   if( _position + count > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.put", "OVERFLOW" ));
   }
   memcpy( _bytes + _position, src + from, count );
   _position += count;
   return *this;
}

ByteBuffer & ByteBuffer::get( byte * target, size_t from, size_t to ) {
   const size_t count = to - from;
   if( _position + count > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.get", "UNDERFLOW" ));
   }
   memcpy( target+from, _bytes + _position, count );
   _position += count;
   return *this;
}

ByteBuffer & ByteBuffer::putByte( byte value ) {
   if( _position + 1 > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.putByte", "OVERFLOW" ));
   }
   _bytes[_position] = value;
   _position += 1;
   return *this;
}

byte ByteBuffer::getByte( void ) {
   if( _position + 1 > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.getByte", "UNDERFLOW" ));
   }
   byte b = _bytes[_position];
   _position += 1;
   return b;
}

ByteBuffer & ByteBuffer::putBool( bool value ) {
   if( _position + 1 > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.putBool", "OVERFLOW" ));
   }
   _bytes[_position] = value ? 1 : 0;
   _position += 1;
   return *this;
}

bool ByteBuffer::getBool( void ) {
   if( _position + 1 > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.getBool", "UNDERFLOW" ));
   }
   byte b = _bytes[_position];
   _position += 1;
   return b != 0;
}


/* Endianness solution:
 * http://stackoverflow.com/questions/2182002/convert-big-endian-to-little-endian-in-c-without-using-provided-func
 */

ByteBuffer & ByteBuffer::putShort( short value ) {
   return putUShort((unsigned short)value );
}

short ByteBuffer::getShort( void ) {
   return (short)getUShort();
}

ByteBuffer & ByteBuffer::putUShort( unsigned short value ) {
   if( _position + sizeof( unsigned short ) > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.putUShort", "OVERFLOW" ));
   }
   if( ( _order == ByteOrder_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( _order == ByteOrder_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (unsigned short)((( value & 0xFF00 ) >> 8 )|(( value & 0x00FF ) << 8 ));
   }
   memcpy( _bytes + _position, &value, sizeof( unsigned short ));
   _position += sizeof( unsigned short );
   return *this;
}

unsigned short ByteBuffer::getUShort( void ) {
   unsigned short value;
   if( _position + sizeof( unsigned short ) > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.getUShort", "UNDERFLOW" ));
   }
   memcpy( &value, _bytes + _position, sizeof( unsigned short ));
   if( ( _order == ByteOrder_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( _order == ByteOrder_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (unsigned short)(
             (( value & 0xFF00 ) >> 8 )
            |(( value & 0x00FF ) << 8 ));
   }
   _position += sizeof( short );
   return value;
}

ByteBuffer & ByteBuffer::putInt( int value ) {
   return putUInt((unsigned int)value );
}

int ByteBuffer::getInt( void ) {
   return (int)getUInt();
}

ByteBuffer & ByteBuffer::putUInt( unsigned int value ) {
   if( _position + sizeof( unsigned int ) > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.putUInt", "OVERFLOW" ));
   }
   if( ( _order == ByteOrder_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( _order == ByteOrder_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (( value & 0xFF000000 ) >> 24 )
             |(( value & 0x00FF0000 ) >>  8 )
             |(( value & 0x0000FF00 ) <<  8 )
             |(  value                << 24 );
   }
   memcpy( _bytes + _position, &value, sizeof( unsigned int ));
   _position += sizeof( unsigned int );
   return *this;
}

ByteBuffer & ByteBuffer::putUInt( size_t index, unsigned int value ) {
   if( index + sizeof( unsigned int ) > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.putUInt", "OVERFLOW" ));
   }
   if( ( _order == ByteOrder_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( _order == ByteOrder_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (( value & 0xFF000000 ) >> 24 )
             |(( value & 0x00FF0000 ) >>  8 )
             |(( value & 0x0000FF00 ) <<  8 )
             |(  value                << 24 );
   }
   memcpy( _bytes + index, &value, sizeof( unsigned int ));
   return *this;
}

unsigned int ByteBuffer::getUInt( void ) {
   unsigned int value;
   if( _position + sizeof( unsigned int ) > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.getUInt", "UNDERFLOW" ));
   }
   memcpy( &value, _bytes + _position, sizeof( int ));
   if( ( _order == ByteOrder_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( _order == ByteOrder_BIG_ENDIAN    && !hostIsBigEndian ))
   {
      value = (( value & 0xFF000000 ) >> 24 )
             |(( value & 0x00FF0000 ) >>  8 )
             |(( value & 0x0000FF00 ) <<  8 )
             |(  value                << 24 );
   }
   _position += sizeof( unsigned int );
   return value;
}

ByteBuffer & ByteBuffer::putLong( int64_t value ) {
   return putULong((uint64_t)value );
}

int64_t ByteBuffer::getLong( void ) {
   return (int64_t)getULong();
}

ByteBuffer & ByteBuffer::putULong( uint64_t value ) {
   if( _position + sizeof( uint64_t ) > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.putULong", "OVERFLOW" ));
   }
   if( ( _order == ByteOrder_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( _order == ByteOrder_BIG_ENDIAN    && !hostIsBigEndian ))
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
   memcpy( _bytes + _position, &value, sizeof( uint64_t ));
   _position += sizeof( uint64_t );
   return *this;
}

uint64_t ByteBuffer::getULong( void ) {
   uint64_t value;
   if( _position + sizeof( uint64_t ) > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.getULong", "UNDERFLOW" ));
   }
   memcpy( &value, _bytes + _position, sizeof( int64_t ));
   if( ( _order == ByteOrder_LITTLE_ENDIAN &&  hostIsBigEndian )
     ||( _order == ByteOrder_BIG_ENDIAN    && !hostIsBigEndian ))
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
   _position += sizeof( uint64_t );
   return value;
}

ByteBuffer & ByteBuffer::putFloat( float value ) {
   void * ptr = &value;
   return putUInt(*(unsigned int*)ptr );
}

float ByteBuffer::getFloat( void ) {
   unsigned int value = getUInt();
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wstrict-aliasing"
    return *((float *)&value);
#pragma GCC diagnostic pop
}

ByteBuffer & ByteBuffer::putDouble( double value ) {
   void * ptr = &value;
   return putULong(*(uint64_t*)ptr );
}

double ByteBuffer::getDouble( void ) {
   uint64_t value = getULong();
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wstrict-aliasing"
   return *((double *)&value);
#pragma GCC diagnostic pop
}

ByteBuffer & ByteBuffer::putString( const char * source ) {
   size_t len = strlen( source );
   putUInt((unsigned int)len );
   put((const byte *)source, 0U, len );
   return *this;
}

std::string ByteBuffer::getString() {
   unsigned int len  = getUInt();
   char *       dest = new char[len];
   get((byte *)dest, 0, len );
   dest[len] = '\0';
   return std::string( dest );
}

ByteBuffer & ByteBuffer::put( ByteBuffer & source ) {
   size_t count = source._limit - source._position;
   if( _position + count > _limit ) {
      throw std::out_of_range( buildWhat( __FILE__, __LINE__, "ByteBuffer.put", "OVERFLOW" ));
   }
   memcpy( _bytes + _position, source._bytes + source._position, count );
   source._position += count;
   _position += count;
   return *this;
}
