package org.fundaciobit.plugins.paymentweb;

/**
 * 
 * @author anadal
 *
 */
public class StartPaymentResult {

  final String redirectUrl;

  final boolean relativeUrl;

  public StartPaymentResult(String redirectUrl, boolean relativeUrl) {
    super();
    this.redirectUrl = redirectUrl;
    this.relativeUrl = relativeUrl;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public boolean isRelativeUrl() {
    return relativeUrl;
  }

}
