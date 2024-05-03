package com.samourai.wallet.util.urlStatus;

public class UpStatus {
    String id;
    private boolean up;
    private long lastCheck;
    private long expiration;
    private long since;
    private String info;

    public UpStatus(String id, boolean up, long expirationDelay, String info) {
        long now = System.currentTimeMillis();
        this.id = id;
        this.since = now;
        setStatus(up, expirationDelay, info);
    }

    public void setStatus(boolean up, long expirationDelay, String info) {
        long now = System.currentTimeMillis();
        if (up != this.up) {
            this.up = up;
            this.since = now;
        }
        this.lastCheck = now;
        this.expiration = now+expirationDelay;
        this.info = info;
    }

    public String getId() {
        return id;
    }

    public boolean isExpired() {
        return expiration < System.currentTimeMillis();
    }

    public boolean isUp() {
        return up;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public long getSince() {
        return since;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "upStatusId=" + id +
                ", up=" + up +
                ", lastCheck=" + lastCheck +
                ", expiration=" + expiration +
                ", since=" + since +
                ", info=" + info;
    }
}
