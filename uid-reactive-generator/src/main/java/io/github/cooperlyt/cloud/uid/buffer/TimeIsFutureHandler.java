package io.github.cooperlyt.cloud.uid.buffer;

@FunctionalInterface
public interface TimeIsFutureHandler {
  void timeIsFuture(long idTime, long currentTime);

}
