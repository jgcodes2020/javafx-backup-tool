package io.github.jgcodes.butool.util;

import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class PlatformUtil {
  private static class Container<T> {
    public T obj = null;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Container<?> that = (Container<?>) o;
      return Objects.equals(obj, that.obj);
    }

    @Override
    public int hashCode() {
      return ~obj.hashCode();
    }

    @Override
    public String toString() {
      return "ObjectHolder{" +
        "object=" + obj +
        '}';
    }
  }

  /**
   * Runs the specified Runnable on the JavaFX Application Thread at some unspecified time in the future.
   * This method, which may be called from any thread, will post the Runnable to an event queue and then
   * block until the {@code Runnable} has been executed, or throws an exception.
   *
   * @param r the Runnable whose run method is to be executed on the JavaFX application thread
   * @throws InterruptedException if the thread is interrupted while waiting for the {@code Runnable} to finish.
   * @throws ExecutionException if the {@code Runnable} throws an exception or error while running.
   */
  public static void runAndWait(Runnable r) throws InterruptedException, ExecutionException {
    // run directly if on FX thread
    if (Platform.isFxApplicationThread()) {
      try {
        r.run();
      } catch (Exception e) {
        throw new ExecutionException(e);
      }
    }
    else {
      Container<Throwable> throwable = new Container<>();
      CountDownLatch done = new CountDownLatch(1);
      // catch any and all Throwables, signal thread exit on finish
      Platform.runLater(() -> {
        try {
          r.run();
        } catch (Throwable t) {
          throwable.obj = t;
        } finally {
          done.countDown();
        }
      });

      done.await();
      if (throwable.obj != null) {
        throw new ExecutionException(throwable.obj);
      }
    }
  }
}
