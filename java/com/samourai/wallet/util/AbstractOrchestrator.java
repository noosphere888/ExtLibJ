package com.samourai.wallet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public abstract class AbstractOrchestrator {
  private Logger log;
  private final int LOOP_DELAY;
  private final int START_DELAY;
  private final Integer LAST_RUN_DELAY;

  private boolean started;
  protected Thread myThread;
  private boolean dontDisturb;
  private long lastRun;
  private boolean lastRunSetInLoop;

  public AbstractOrchestrator(int loopDelay) {
    this(loopDelay, 0, null);
  }

  public AbstractOrchestrator(int loopDelayMs, int startDelayMs, Integer lastRunDelaySeconds) {
    this.log = LoggerFactory.getLogger(getClass().getName());
    this.LOOP_DELAY = loopDelayMs;
    this.START_DELAY = startDelayMs;
    this.LAST_RUN_DELAY = lastRunDelaySeconds;
    resetOrchestrator();
  }

  protected void resetOrchestrator() {
    this.dontDisturb = false;
    this.lastRun = 0;
  }

  protected String getThreadName() {
    return getClass().getSimpleName();
  }

  public synchronized void start(boolean daemon) {
    if (isStarted()) {
      log.error("Cannot start: already started");
      return;
    }
    if (log.isTraceEnabled()) {
      log.trace(
          "Starting... loopDelay="
              + LOOP_DELAY
              + ", lastRunDelay="
              + (LAST_RUN_DELAY != null ? LAST_RUN_DELAY : "null"));
    }
    this.started = true;
    String mdc = LogbackUtils.mdcAppend("orchestrator="+getThreadName());
    this.myThread =
        new Thread(
                () -> {
                  MDC.put("mdc",mdc);
                  if (START_DELAY > 0) {
                    doSleep(START_DELAY);
                  }
                  while (started) {
                    lastRunSetInLoop = false;
                    runOrchestrator();

                    // orchestrator may have been stopped in the meantime, as function is not
                    // synchronized
                    if (lastRunSetInLoop && LAST_RUN_DELAY != null) {
                      // wait for lastRunDelay if we did run in this loop
                      waitForLastRunDelay(LAST_RUN_DELAY);
                    } else {
                      doSleep(LOOP_DELAY);
                    }

                    lastRunSetInLoop = false;
                  }

                  // thread exiting
                  myThread = null;
                  if (log.isTraceEnabled()) {
                    log.trace("Ended.");
                  }
                  resetOrchestrator();
                },
            getThreadName());
    this.myThread.setDaemon(daemon);
    this.myThread.start();
  }

  protected abstract void runOrchestrator();

  public synchronized void stop() {
    if (!isStarted()) {
      log.error("Cannot stop: not started");
      return;
    }
    if (log.isTraceEnabled()) {
      log.trace("Ending...");
    }
    this.started = false;
    synchronized (myThread) {
      myThread.notify();
    }
  }

  protected synchronized void notifyOrchestrator() {
    if (isStarted() && !isDontDisturb()) {
      if (log.isTraceEnabled()) {
        log.trace("notifying");
      }
      synchronized (myThread) {
        myThread.notify();
      }
    } else {
      if (log.isTraceEnabled()) {
        log.trace("NOT notifying (dontDisturb)");
      }
    }
  }

  protected void sleepOrchestrator(long timeToWait, boolean withDontDisturb) {
    if (withDontDisturb) {
      dontDisturb = true;
    }
    doSleep(timeToWait);
    if (withDontDisturb) {
      dontDisturb = false;
    }
  }

  private void doSleep(long timeToWait) {
    try {
      synchronized (myThread) {
        myThread.wait(timeToWait);
      }
    } catch (InterruptedException e) {
    }
  }

  private long computeWaitForLastRunDelay(int delay) {
    long elapsedTimeSinceLastRun = System.currentTimeMillis() - lastRun;
    long timeToWait = (delay * 1000) - elapsedTimeSinceLastRun;
    return timeToWait;
  }

  private boolean waitForLastRunDelay(int delay) {
    long timeToWait = computeWaitForLastRunDelay(delay);
    if (timeToWait > 0) {
      if (log.isTraceEnabled()) {
        log.trace("Sleeping for lastRunDelay (" + (timeToWait / 1000) + "s to wait)");
      }
      sleepOrchestrator(timeToWait, true);
      return true;
    }
    return false;
  }

  protected void setLastRun() {
    this.lastRun = System.currentTimeMillis();
    this.lastRunSetInLoop = true;
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isDontDisturb() {
    return dontDisturb;
  }

  protected int getLOOP_DELAY() {
    return LOOP_DELAY;
  }
}
