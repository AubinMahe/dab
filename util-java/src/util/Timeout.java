package util;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Timeout {

   public static final int TIMEOUT_MAX_COUNT = 20;

   private static final ScheduledExecutorService _Executor = Executors.newScheduledThreadPool( TIMEOUT_MAX_COUNT );

   private final long               _duration;
   private final Runnable           _actionWhenElapsed;
   private /* */ ScheduledFuture<?> _future;

   public Timeout( Duration duration, Runnable actionWhenElapsed ) {
      _duration          = duration.toNanos();
      _actionWhenElapsed = actionWhenElapsed;
   }

   public void start() {
      cancel();
      _future = _Executor.schedule( _actionWhenElapsed, _duration, TimeUnit.NANOSECONDS );
   }

   public void cancel() {
      if( _future != null ) {
         _future.cancel( true );
      }
   }

   public boolean hasBeenScheduled() {
      return _future != null;
   }

   public boolean isCancelled() {
      return ( _future != null )&& _future.isCancelled();
   }

   public boolean isDone() {
      return ( _future != null )&& _future.isCancelled();
   }
}
