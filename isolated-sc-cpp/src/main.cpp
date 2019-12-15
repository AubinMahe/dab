#include <isolated/sc/ComponentFactory.hpp>

int main( void ) {
   isolated::sc::ComponentFactory factory;
   hpms::sc::Banque &             component( factory.getSc());
   hpms::sc::BanqueUI             ui( component );
   component.setUI( ui );
   ui.run();
   return 0;
}
