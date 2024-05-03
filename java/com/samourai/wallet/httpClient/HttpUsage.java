package com.samourai.wallet.httpClient;

import java.util.Objects;

public class HttpUsage {
  public static final HttpUsage BACKEND = new HttpUsage("BACKEND");
  public static final HttpUsage SOROBAN = new HttpUsage("SOROBAN");

  private String name;
  public HttpUsage(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HttpUsage httpUsage = (HttpUsage) o;
    return Objects.equals(name, httpUsage.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
