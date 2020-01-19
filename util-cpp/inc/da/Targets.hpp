#pragma once

#include <io/sockets.hpp>
#include <types.hpp>
#include "InstanceID.hpp"

namespace da {

   struct Targets {

      sockaddr_in  process;
      unsigned     instancesCount;
      InstanceID * instances;
   };
}
