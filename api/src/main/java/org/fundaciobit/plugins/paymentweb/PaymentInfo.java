package org.fundaciobit.plugins.paymentweb;

import java.util.Date;
import java.util.Locale;

/**
 * 
 * @author anadal
 *
 */
public class PaymentInfo {
  
  long paymentID;

  String descriptionProduct;
  
  float amount;
  
  /**
   * http://www.currency-iso.org/en/home/tables/table-a1.html
   */
  String currencyCodeISO4217;
  
  // Comprador
  Locale shopperLocale;
  
  // Comprador
  String shopperEmail;
  
  // Comprador
  String shopperName;
  
  

  String returnUrl;
  
  // Nom Venedor
  String sellerName;
  
  String sellerUrl;
  
  Date expireDate;

  public String getDescriptionProduct() {
    return descriptionProduct;
  }

  public void setDescriptionProduct(String descriptionProduct) {
    this.descriptionProduct = descriptionProduct;
  }

  public float getAmount() {
    return amount;
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public String getCurrencyCodeISO4217() {
    return currencyCodeISO4217;
  }

  public void setCurrencyCodeISO4217(String currencyCodeISO4217) {
    this.currencyCodeISO4217 = currencyCodeISO4217;
  }

  public Locale getShopperLocale() {
    return shopperLocale;
  }

  public void setShopperLocale(Locale shopperLocale) {
    this.shopperLocale = shopperLocale;
  }

  public String getShopperEmail() {
    return shopperEmail;
  }

  public void setShopperEmail(String shopperEmail) {
    this.shopperEmail = shopperEmail;
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  public void setReturnUrl(String returnUrl) {
    this.returnUrl = returnUrl;
  }

  public Date getExpireDate() {
    return expireDate;
  }

  public void setExpireDate(Date expireDate) {
    this.expireDate = expireDate;
  }

  public String getSellerName() {
    return sellerName;
  }

  public void setSellerName(String sellerName) {
    this.sellerName = sellerName;
  }
  
  

  public String getSellerUrl() {
    return sellerUrl;
  }

  public void setSellerUrl(String sellerUrl) {
    this.sellerUrl = sellerUrl;
  }

  public String getShopperName() {
    return shopperName;
  }

  public void setShopperName(String shopperName) {
    this.shopperName = shopperName;
  }
  
  public long getPaymentID() {
    return paymentID;
  }

  public void setPaymentID(long paymentID) {
    this.paymentID = paymentID;
  }
  
  

  /**
   * 
   * @return
   */
  public static synchronized long generateUniqueSignaturesSetID() {
    long id;
    
    id = (System.currentTimeMillis() * 1000000L) + System.nanoTime() % 1000000L;
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
    }
    
    return id;
  }

}
