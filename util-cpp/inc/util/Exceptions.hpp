#pragma once

#include <types.hpp>
#include <exception>

namespace util {

   class Exception : public std::exception {
   protected:

      Exception( const char * file, int line, const char * func, const char * fmt ... );

   private:

      virtual const char * getName() const noexcept = 0;

   public:

      void push_backtrace(  const char * file, int line, const char * func, const char * fmt ... );

      virtual const char * what() const noexcept;

   protected:

      typedef char message_t[1000];

      static message_t _message;

   protected:

      typedef char stackItem_t[500 + sizeof( message_t )];

      stackItem_t _stack[12];
      size_t      _stackIndex;
   };

#  define EXCEPTION_DEF( NAME )\
   class NAME : public Exception {\
   public:\
      template<class ... A>\
      NAME( const char * file, int line, const char * func, const char * fmt, A ... args ) :\
         Exception( file, line, func, fmt, args... )\
      {}\
   private:\
      virtual const char * getName() const noexcept { return #NAME; }\
   }

   EXCEPTION_DEF( NullArg );

   inline void nullCheck( const char * file, int line, const char * func, const void * arg, const char * argName ) {
      if( arg == 0 ) {
         throw NullArg( file, line, func, "%s is null", argName );
      }
   }

   EXCEPTION_DEF( NotFound );
   EXCEPTION_DEF( Parse );
   EXCEPTION_DEF( Overflow );
   EXCEPTION_DEF( Underflow );
   EXCEPTION_DEF( NotApplicable );
   EXCEPTION_DEF( Unexpected );
   EXCEPTION_DEF( OutOfRange );

   class Runtime : public Exception {
   public:
      Runtime( const char * file, int line, const char * func, const char * fmt... );
   private:
      virtual const char * getName() const noexcept { return "Runtime"; }
   };

#  define UTIL_CTXT __FILE__, __LINE__, HPMS_FUNCNAME
}
