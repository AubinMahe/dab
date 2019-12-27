#pragma once

#include <io/sockets.hpp>
#include <types.hpp>

namespace da {

   struct Targets {

      sockaddr_in process;
      unsigned    instancesCount;
      byte *      instances;
   };
}
