#include <dab/carte.h>
#include <stdio.h>

util_error dab_carte_put( dab_carte * This, io_byte_buffer * target ) {
   UTIL_ERROR_CHECK( io_byte_buffer_put_string( target, This->id        ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_string( target, This->code      ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte  ( target, This->month     ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_ushort( target, This->year      ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_byte  ( target, This->nb_essais ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_carte_get( dab_carte * This, io_byte_buffer * source ) {
   UTIL_ERROR_CHECK( io_byte_buffer_get_string( source, This->id  , sizeof( This->id )  ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_get_string( source, This->code, sizeof( This->code )), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_get_byte  ( source, &This->month                    ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_get_ushort( source, &This->year                     ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_get_byte  ( source, &This->nb_essais                ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}
