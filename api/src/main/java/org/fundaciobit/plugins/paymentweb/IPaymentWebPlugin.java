package org.fundaciobit.plugins.paymentweb;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fundaciobit.plugins.IPlugin;

/**
 * 
 * @author anadal
 * 
 */
public interface IPaymentWebPlugin extends IPlugin {

  public static final String PAYMENTWEB_BASE_PROPERTY = IPLUGIN_BASE_PROPERTIES  + "paymentweb.";

  public String getName(Locale locale) throws Exception;
  
  /**
   * 
   * @param request
   * @param paymentInfo
   * @return Url de redirecció al sistema de pagament
   * @throws Exception
   */
  public StartPaymentResult startPayment(HttpServletRequest request,
      String absoluteAddress, String relativeAddress,
      PaymentInfo paymentInfo) throws Exception;

  public void controllerGET(HttpServletRequest request, HttpServletResponse response,
      String absoluteAddress, String relativeAddress,
      long paymentID, String query) throws Exception;
  
  public void controllerPOST(HttpServletRequest request, HttpServletResponse response,
      String absoluteAddress, String relativeAddress,
      long paymentID, String query) throws Exception;

  public PaymentStatus getPaymentStatus(long paymentID) throws Exception;
  
  public PaymentInfo getPaymentInfo(long paymentID) throws Exception;
  
  
  public void closePayment(long paymentID);
  
}