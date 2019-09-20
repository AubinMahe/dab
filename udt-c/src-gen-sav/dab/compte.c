#include <dab/compte.h>
#include <stdio.h>

util_error dab_compte_put( dab_compte * This, io_byte_buffer * target ) {
   UTIL_ERROR_CHECK( io_byte_buffer_put_string( target, This->id       ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_double( target, This->solde    ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_put_bool  ( target, This->autorise ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error dab_compte_get( dab_compte * This, io_byte_buffer * source ) {
   UTIL_ERROR_CHECK( io_byte_buffer_get_string( source, This->id, sizeof( This->id )), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_get_double( source, &This->solde                ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( io_byte_buffer_get_bool  ( source, &This->autorise             ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}
