#include <isolated/ihm1/ComponentFactory.hpp>

#include <util/Log.hpp>
#include <os/sleep.hpp>

int main( void ) {
   UTIL_LOG_HERE();
   isolated::ihm1::ComponentFactory factory;
   hpms::dab::Distributeur &        component( factory.getIhm1());
   hpms::dab::DistributeurUI        ui( component );
   component.setUI( ui );
   ui.run();
   UTIL_LOG_DONE();
   return 0;
}
