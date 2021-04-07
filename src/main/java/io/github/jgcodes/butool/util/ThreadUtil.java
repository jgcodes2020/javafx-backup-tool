package io.github.jgcodes.butool.util;

public class ThreadUtil {
  public static void sleepIgnoreInterrupt(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException ignored) {}
  }
}
