package com.samourai.wallet.util.urlStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UpStatusPool {
  private static final Logger log = LoggerFactory.getLogger(UpStatusPool.class);

  private long retryDelayMs;
  private Map<String, UpStatus> upStatusById;

  public UpStatusPool(long retryDelayMs) {
    this.retryDelayMs = retryDelayMs;
    this.upStatusById = new LinkedHashMap<>();
  }

  public UpStatus getUrlStatus(String id) {
    UpStatus upStatus = upStatusById.get(id);
    if (upStatus == null || upStatus.isExpired()) {
      upStatus = null;
    }
    return upStatus;
  }

  public boolean isDown(String id) {
    UpStatus upStatus = getUrlStatus(id);
    if (upStatus != null && !upStatus.isUp()) {
        return true; // down
    }
    return false; // up
  }

  public Collection<String> filterNotDown(Collection<String> urls) {
    return urls.stream().filter(url -> !isDown(url)).collect(Collectors.toList());
  }

  protected void setStatus(String id, boolean up, String info) {
    UpStatus upStatus = upStatusById.get(id);
    boolean statusChanged = (upStatus == null && !up) || (upStatus != null && up != upStatus.isUp());
    if (upStatus != null) {
      // update existing status
      upStatus.setStatus(up, retryDelayMs, info);
    } else {
      // create new status
      upStatus = new UpStatus(id, up, retryDelayMs, info);
      upStatusById.put(id, upStatus);
    }
    if (statusChanged) {
      onChange(upStatus);
    }
  }

  // overridable
  protected void onChange(UpStatus upStatus) {
    if (log.isDebugEnabled()) {
      log.debug("upStatus changed: "+upStatus);
    }
  }

  public void setStatusUp(String url, String info) {
    setStatus(url, true, info);
  }

  public void setStatusDown(String url, String info) {
    setStatus(url, false, info);
  }

  public void expireAll() {
    long now = System.currentTimeMillis();
    for (UpStatus upStatus : upStatusById.values()) {
      upStatus.setExpiration(now);
    }
  }

  public void clear() {
    upStatusById.clear();
  }

  public void clear(String url) {
    upStatusById.remove(url);
  }

  public void clear(Collection<String> urls) {
    urls.stream().forEach(url -> clear(url));
  }

  public Collection<UpStatus> getListNotExpired() {
    return upStatusById.values().stream().filter(upStatus -> !upStatus.isExpired()).collect(Collectors.toList());
  }

  public Collection<UpStatus> getListWithExpired() {
    return upStatusById.values();
  }
}
