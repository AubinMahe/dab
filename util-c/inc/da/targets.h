#pragma once

#include <io/sockets.h>
#include <util/error_codes.h>
#include <types.h>

typedef struct da_targets_tag {

   struct sockaddr_in process;
   size_t             instances_count;
   byte *             instances;

} da_targets;
