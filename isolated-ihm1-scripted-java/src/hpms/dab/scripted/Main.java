package hpms.dab.scripted;

import java.io.File;
import java.io.IOException;

import isolated.scripted.ihm1.ComponentFactory;

public class Main {

   public static void main( String[] args ) throws InterruptedException, IOException {
      final ComponentFactory factory = new ComponentFactory();
      factory.getIhm1().run( Integer.parseInt( args[0] ), new File( args[1] ), new File( args[2] ));
      factory.join();
   }
}
