#pragma once

#include <dab/IUniteDeTraitement.hpp>

namespace dab::udt {

   ::dab::IUniteDeTraitement * newUniteDeTraitement(
      const char *   intrfc,
      unsigned short udtPort,
      const char *   scAddress,
      unsigned short scPort,
      const char *   uiAddress,
      unsigned short uiPort     );
}
