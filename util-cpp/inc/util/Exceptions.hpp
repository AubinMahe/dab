#pragma once

#include <exception>

namespace util {

   class Exception : public std::exception {
   protected:

      Exception( const char * file, int line, const char * func, const char * fmt ... );

   private:

      virtual const char * getName() const noexcept = 0;

   public:

      virtual const char * what() const noexcept;

   protected:

      char _prefix [1000];
      char _message[1000];
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

#  define UTIL_CTXT __FILE__, __LINE__, __PRETTY_FUNCTION__
}
