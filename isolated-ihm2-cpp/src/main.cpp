#include <isolated/ihm2/ComponentFactory.hpp>

int main( void ) {
   isolated::ihm2::ComponentFactory factory;
   hpms::dab::Distributeur &        component( factory.getIhm2());
   hpms::dab::DistributeurUI        ui( component );
   component.setUI( ui );
   ui.run();
   return 0;
}
