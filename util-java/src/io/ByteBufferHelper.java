package io;

import java.nio.ByteBuffer;

public final class ByteBufferHelper {

   public static void putString( ByteBuffer target, String s ) {
      final byte[] chars = s.getBytes();
      target.putInt( chars.length );
      target.put( chars );
   }

   public static String getString( ByteBuffer source ) {
      final int    len   = source.getInt();
      final byte[] chars = new byte[len];
      source.get( chars );
      return new String( chars );
   }

   public static void putBoolean( ByteBuffer target, boolean value ) {
      target.put((byte)( value ? 1 : 0 ));
   }

   public static boolean getBoolean( ByteBuffer source ) {
      return source.get() != 0;
   }
}
