#include <io/DatagramSocket.hpp>

#include <string.h>

#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  include <winsock2.h>
#  include <ws2tcpip.h>
#  include <mswsock.h>
#  include <iostream>

#  define SHUT_RD       SD_RECEIVE
#  define SHUT_WR       SD_SEND
#  define SHUT_RDWR     SD_BOTH

#  define NS_INADDRSZ  4
#  define NS_IN6ADDRSZ 16
#  define NS_INT16SZ   2

/**
 * inet_pton4, inet_pton6 : Author: Paul Vixie, 1996.
 */
extern "C" int inet_pton4( const char * src, char * dst ) {
    uint8_t tmp[NS_INADDRSZ], *tp;
    int saw_digit = 0;
    int octets = 0;
    *(tp = tmp) = 0;
    int ch;
    while ((ch = *src++) != '\0' ) {
        if (ch >= '0' && ch <= '9') {
            uint32_t n = *tp * 10 + (ch - '0');
            if (saw_digit && *tp == 0)
                return 0;
            if (n > 255)
                return 0;
            *tp = n;
            if (!saw_digit)
            {
                if (++octets > 4)
                    return 0;
                saw_digit = 1;
            }
        }
        else if (ch == '.' && saw_digit)
        {
            if (octets == 4)
                return 0;
            *++tp = 0;
            saw_digit = 0;
        }
        else
            return 0;
    }
    if (octets < 4)
        return 0;

    memcpy(dst, tmp, NS_INADDRSZ);

    return 1;
}

extern "C" int inet_pton6( const char * src, char * dst ) {
    static const char xdigits[] = "0123456789abcdef";
    uint8_t tmp[NS_IN6ADDRSZ];
    uint8_t *tp = (uint8_t*) memset(tmp, '\0', NS_IN6ADDRSZ);
    uint8_t *endp = tp + NS_IN6ADDRSZ;
    uint8_t *colonp = NULL;
    /* Leading :: requires some special handling. */
    if (*src == ':') {
        if (*++src != ':')
            return 0;
    }
    const char *curtok = src;
    int saw_xdigit = 0;
    uint32_t val = 0;
    int ch;
    while(( ch = tolower(*src++)) != '\0' ) {
        const char *pch = strchr(xdigits, ch);
        if( pch != NULL ) {
            val <<= 4;
            val |= (pch - xdigits);
            if (val > 0xffff)
                return 0;
            saw_xdigit = 1;
            continue;
        }
        if( ch == ':' ) {
            curtok = src;
            if (!saw_xdigit) {
                if (colonp)
                    return 0;
                colonp = tp;
                continue;
            }
            else if (*src == '\0') {
                return 0;
            }
            if (tp + NS_INT16SZ > endp)
                return 0;
            *tp++ = (uint8_t) (val >> 8) & 0xff;
            *tp++ = (uint8_t) val & 0xff;
            saw_xdigit = 0;
            val = 0;
            continue;
        }
        if (ch == '.' && ((tp + NS_INADDRSZ) <= endp) && inet_pton4(curtok, (char*) tp) > 0) {
            tp += NS_INADDRSZ;
            saw_xdigit = 0;
            break; /* '\0' was seen by inet_pton4(). */
        }
        return 0;
    }
    if (saw_xdigit) {
        if (tp + NS_INT16SZ > endp)
            return 0;
        *tp++ = (uint8_t) (val >> 8) & 0xff;
        *tp++ = (uint8_t) val & 0xff;
    }
    if (colonp != NULL) {
        /*
         * Since some memmove()'s erroneously fail to handle
         * overlapping regions, we'll do the shift by hand.
         */
        const int n = tp - colonp;
        if (tp == endp)
            return 0;
        for (int i = 1; i <= n; i++) {
            endp[-i] = colonp[n - i];
            colonp[n - i] = 0;
        }
        tp = endp;
    }
    if (tp != endp)
        return 0;
    memcpy(dst, tmp, NS_IN6ADDRSZ);
    return 1;
}

extern"C" int inet_pton( int af, const char * src, void * dst ) {
   switch( af ) {
   case AF_INET : return inet_pton4( src, (char *)dst );
   case AF_INET6: return inet_pton6( src, (char *)dst );
   default      : return -1;
   }
}

static struct WinsockInit {
   WinsockInit( void ) {
      WORD    wVersionRequested = MAKEWORD(2, 2);
      WSADATA wsaData;
      int     err = WSAStartup(wVersionRequested, &wsaData);
      if( err ) {
         std::cerr << "WSAStartup failed with error: " << err << std::endl;
      }
   }
} initWinsock;

#else

#  include <sys/socket.h>
#  include <netinet/ip.h>
#  include <arpa/inet.h>
#  include <netdb.h>
#  include <unistd.h>
#  define INVALID_SOCKET (-1)
#  define closesocket close

#endif

using namespace io;

void DatagramSocket::init( const char * hostnameOrIp, unsigned short port, sockaddr_in & target ) {
   hostent * he = ::gethostbyname( hostnameOrIp );
   if( ! he ) {
      throw os::StdApiException( "DatagramSocket.init", __FILE__, __LINE__ );
   }
   target.sin_family = AF_INET;
   target.sin_port   = htons( port );
   ::memcpy( &target.sin_addr, he->h_addr_list[0], (size_t)he->h_length );
}

DatagramSocket::DatagramSocket( void ) :
   _socket( socket( AF_INET, SOCK_DGRAM, 0 ))
{
   if( _socket == INVALID_SOCKET ) {
      throw os::StdApiException( "DatagramSocket.<ctor>", __FILE__, __LINE__ );
   }
}

DatagramSocket:: ~ DatagramSocket( void ) {
   closesocket( _socket );
}

DatagramSocket & DatagramSocket::bind( const char * intrfc, unsigned short port ) {
   sockaddr_in localAddr;
   ::memset( &localAddr, 0, sizeof( localAddr ));
   localAddr.sin_family = AF_INET;
   ::inet_pton( AF_INET, intrfc, &localAddr.sin_addr.s_addr );
   localAddr.sin_port = htons( port );
   if( ::bind( _socket, (sockaddr *)&localAddr, sizeof( localAddr ))) {
      ::closesocket( _socket );
      throw os::StdApiException( "DatagramSocket.bind", __FILE__, __LINE__ );
   }
   return *this;
}

DatagramSocket & DatagramSocket::connect( const char * hostnameOrIp, unsigned short port ) {
   sockaddr_in target;
   init( hostnameOrIp, port, target );
   if( ::connect( _socket, (sockaddr *)&target, sizeof( target ))) {
      ::closesocket( _socket );
      throw os::StdApiException( "DatagramSocket.connect", __FILE__, __LINE__ );
   }
   return *this;
}

bool DatagramSocket::receive( ByteBuffer & bb ) {
   size_t  max    = bb.limit() - bb.position();
   void *  buffer = bb.array() + bb.position();
   int     flags  = 0;
   ssize_t count  = ::recvfrom( _socket, (char *)buffer, max, flags, 0, 0 );
   if( count < 0 ) {
      throw os::StdApiException( "DatagramSocket.receive", __FILE__, __LINE__ );
   }
   bb.position( bb.position() + (size_t)count );
   return true;
}

bool DatagramSocket::receive( ByteBuffer & bb, sockaddr_in & from ) {
   size_t    max    = bb.limit() - bb.position();
   void *    buffer = bb.array() + bb.position();
   socklen_t len    = sizeof( from );
   int       flags  = 0;
   ssize_t   count  = ::recvfrom( _socket, (char *)buffer, max, flags, (sockaddr *)&from, &len );
   if( count < 0 ) {
      throw os::StdApiException( "DatagramSocket.receive(from)", __FILE__, __LINE__ );
   }
   bb.position( bb.position() + (size_t)count );
   return true;
}

DatagramSocket & DatagramSocket::send( ByteBuffer & bb ) {
   size_t       len    = bb.limit() - bb.position();
   const char * buffer = (const char *)( bb.array() + bb.position());
   ssize_t      count  = ::send( _socket, buffer, len, 0 );
   if( count < 0 || ( len != (size_t)count )) {
      throw os::StdApiException( "DatagramSocket.send", __FILE__, __LINE__ );
   }
   bb.position( bb.position() + (size_t)count );
   return *this;
}

DatagramSocket & DatagramSocket::sendTo( ByteBuffer & bb, struct sockaddr_in & target ) {
   size_t       len    = bb.limit() - bb.position();
   const char * buffer = (const char *)( bb.array() + bb.position());
   ssize_t      count  = ::sendto( _socket, buffer, len, 0, (struct sockaddr *)&target, sizeof( struct sockaddr_in ));
   if( count < 0 || ( len != (size_t)count )) {
      throw os::StdApiException( "DatagramSocket.sendTo", __FILE__, __LINE__ );
   }
   bb.position( bb.position() + (size_t)count );
   return *this;
}
