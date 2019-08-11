#pragma once

#include <stdexcept>

namespace os {

   class StdApiException : public std::runtime_error {
   public:

      StdApiException( const char * classMethod, const char * file, unsigned line );
   };
}
