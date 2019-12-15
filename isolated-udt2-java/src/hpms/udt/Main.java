package hpms.udt;

import java.io.IOException;

import isolated.udt2.ComponentFactory;

public final class Main {

   public static void main( String[] args ) throws IOException, InterruptedException {
      final Thread factory = new ComponentFactory();
      factory.join();
   }
}
