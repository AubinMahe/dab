#pragma once

#include <da/InstanceID.hpp>
#include <io/DatagramSocket.hpp>
#include <io/sockets.hpp>
#include <os/Thread.hpp>

namespace da {

   class AsynchronousRequestResponse {
   protected:

      AsynchronousRequestResponse(
         io::DatagramSocket   & socket,
         const da::InstanceID & from,
         const da::InstanceID & to,
         const sockaddr_in    & target )
       :
         _socket( socket  ),
         _from  ( from    ),
         _to    ( to      ),
         _target( target  ),
         _thread( nullptr )
      {}

      virtual ~ AsynchronousRequestResponse( void ) {
         delete _thread;
      }

      void start() {
         _thread = new os::Thread((os::thread_entry_t)_run, this );
      }

      virtual void run( void ) = 0;

   private:

      static void * _run( AsynchronousRequestResponse * This ) {
         This->run();
         delete This;
         return nullptr;
      }

   protected:

      io::DatagramSocket & _socket;
      da::InstanceID       _from;
      da::InstanceID       _to;
      sockaddr_in          _target;
      os::Thread *         _thread;
   };
}
