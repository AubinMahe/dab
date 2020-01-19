#pragma once

#include <types.hpp>
#include <da/FacetMessage.hpp>
#include <da/InstanceID.hpp>
#include <util/Log.hpp>

#include <type_traits>
#include <algorithm>

#include <string.h>

namespace da {

   class IFacetMessagesQueue {
   public:

      virtual ~ IFacetMessagesQueue( void ) {}

   public:

      virtual byte capacity() const = 0;
      virtual bool is_empty() const = 0;
   };

   template<class I, class P, byte S>
   class FacetMessagesQueue : public IFacetMessagesQueue {

      static_assert( std::is_enum<I>::value,
         "I type parameter of this class must be an enum class : byte");
      static_assert( S > 0,
         "S parameter must be strictly positive (>0)" );

   public:

      FacetMessagesQueue( I iface, const char * name, bool discardOldWhenFull ) :
         _iface             ( iface              ),
         _name              ( name               ),
         _discardOldWhenFull( discardOldWhenFull ),
         _count             ( 0U                 )
      {
         ::memset( _queue, 0, sizeof( _queue ));
      }

      virtual ~ FacetMessagesQueue( void ) {}

      FacetMessagesQueue( const FacetMessagesQueue & ) = delete;
      FacetMessagesQueue & operator = ( const FacetMessagesQueue & ) = delete;

   public:

      virtual byte capacity() const {
         return S;
      }

      byte size() const {
         return _count;
      }

      bool is_empty() const {
         return _count == 0;
      }

      bool pop_front( FacetMessage<I, P> & message ) {
         if( _count > 0 ) {
            message = _queue[0];
            const size_t bytes = --_count * sizeof( FacetMessage<I, P> );
            ::memmove( _queue, _queue + 1, bytes );
            UTIL_LOG_ARGS( "Message removed from '%s': %s, from: %s, to %s",
               _name, ::toString( _iface, message._event ), message._fromInstance.toString(), message._instance.toString());
            return true;
         }
         UTIL_LOG_ARGS( "Queue '%s' is empty (%d)", _name, _count );
         return false;
      }

      bool push_front( const FacetMessage<I, P> & message ) {
         if( _count < S ) {
            const size_t bytes = std::max( _count, (byte)( S - 1 )) * sizeof( FacetMessage<I, P> );
            ::memmove( _queue + 1, _queue, bytes );
            _queue[0] = message;
            ++_count;
            UTIL_LOG_ARGS( "Message added to '%s': %s, from: %s, to %s",
               _name, toString( _iface, message._event ), message._fromInstance.toString(), message._instance.toString());
            return true;
         }
         if( _discardOldWhenFull ) {
            _queue[0] = message;
            ++_count;
            UTIL_LOG_ARGS( "Message added to '%s': %s, from: %s, to %s, an older message has been discarted",
               _name, ::toString( _iface, message._event ), message._fromInstance.toString(), message._instance.toString());
            return true;
         }
         UTIL_LOG_ARGS( "Queue '%s' is full (%d)", _name, _count );
         return false;
      }

      bool push_back( const FacetMessage<I, P> & message ) {
         if( _count < S ) {
            _queue[_count++] = message;
            UTIL_LOG_ARGS( "Message added to '%s': %s, from: %s, to %s",
               _name, ::toString( _iface, message._event ), message._fromInstance.toString(), message._instance.toString());
            return true;
         }
         if( _discardOldWhenFull ) {
            const size_t bytes = ( _count - 1 ) * sizeof( FacetMessage<I, P> );
            ::memmove( _queue, _queue + 1, bytes );
            _queue[_count-1] = message;
            UTIL_LOG_ARGS( "Message added to '%s': %s, from: %s, to %s, an old message has been discarted",
               _name,
               ::toString( _iface, message._event ),
               message._fromInstance.toString(),
               message._instance.toString());
            return true;
         }
         UTIL_LOG_ARGS( "Queue '%s' is full (%d)", _name, _count );
         return false;
      }

      private:

         I                  _iface;
         const char *       _name;
         const bool         _discardOldWhenFull;
         byte               _count;
         FacetMessage<I, P> _queue[S];
   };
}
