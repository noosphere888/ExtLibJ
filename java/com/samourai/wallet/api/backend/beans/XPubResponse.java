package com.samourai.wallet.api.backend.beans;


public class XPubResponse {
  public Status status;
  public String error;
  public Data data;

  public XPubResponse() {}

  public enum Status {
    ok, error
  }

  public static class Data {
    public long balance;
    public Unused unused;
    public String derivation;
    public long created;
  }

  public static class Unused {
    public int external;
    public int internal;
  }
}
