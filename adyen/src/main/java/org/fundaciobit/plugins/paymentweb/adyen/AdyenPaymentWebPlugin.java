package org.fundaciobit.plugins.paymentweb.adyen;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.fundaciobit.plugins.paymentweb.AbstractPaymentWeb;
import org.fundaciobit.plugins.paymentweb.PaymentInfo;
import org.fundaciobit.plugins.paymentweb.PaymentStatus;
import org.fundaciobit.plugins.paymentweb.StartPaymentResult;

/**
 * 
 * @author anadal
 *
 */
public class AdyenPaymentWebPlugin extends AbstractPaymentWeb {

  public static final String ADYEN_PAYMENTWEB_BASE_PROPERTY = PAYMENTWEB_BASE_PROPERTY
      + "adyen.";

  public static final String HMACKEY_SHA1 = ADYEN_PAYMENTWEB_BASE_PROPERTY
      + "hmackey";

  public static final String SKINCODE = ADYEN_PAYMENTWEB_BASE_PROPERTY
      + "skincode";

  public static final String MERCHANTACCOUNT = ADYEN_PAYMENTWEB_BASE_PROPERTY
      + "merchantaccount";

  // https://test.adyen.com/hpp/pay.shtml
  public static final String URL = ADYEN_PAYMENTWEB_BASE_PROPERTY + "url";

  
  
  
  public AdyenPaymentWebPlugin() {
    super();
    // TODO Auto-generated constructor stub
  }

  public AdyenPaymentWebPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
    // TODO Auto-generated constructor stub
  }

  public AdyenPaymentWebPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
    // TODO Auto-generated constructor stub
  }

  @Override
  public String getName(Locale locale) throws Exception {
    return "Adyen";
  }

  @Override
  public StartPaymentResult startPayment(HttpServletRequest request,
      String absoluteAddress, String relativeAddress, PaymentInfo paymentInfo)
      throws Exception {

    final long paymentID = paymentInfo.getPaymentID();

    registryPayment(paymentID, paymentInfo);

    String redirectUrl = relativeAddress + paymentID + "/" + START;

    boolean relativeUrl = true;

    return new StartPaymentResult(redirectUrl, relativeUrl);

  }

  @Override
  public void controllerGET(HttpServletRequest request,
      HttpServletResponse response, String absoluteAddress,
      String relativeAddress, long paymentID, String query) throws Exception {

    if (query.startsWith(START)) {
      startGet(request, response, absoluteAddress, relativeAddress, paymentID);
    } else {
      final String type = "GET";
      sendNotFound(response, absoluteAddress, relativeAddress, paymentID,
          query, type);
    }

  }

  private void sendNotFound(HttpServletResponse response,
      String absoluteAddress, String relativeAddress, long paymentID,
      String query, final String type) throws IOException {
    log.error(" ===  PAYMENT " + type + " ===");
    log.error(" absoluteAddress ==> |" + absoluteAddress + "|");
    log.error(" relativeAddress ==> |" + relativeAddress + "|");
    log.error(" paymentID ==> |" + paymentID + "|");
    log.error(" query ==> |" + query + "|");

    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  @Override
  public void controllerPOST(HttpServletRequest request,
      HttpServletResponse response, String absoluteAddress,
      String relativeAddress, long paymentID, String query) throws Exception {

    if (query.startsWith(FINAL)) {
      finalPost(request, response, absoluteAddress, relativeAddress, paymentID);
    } else {
      final String type = "POST";
      sendNotFound(response, absoluteAddress, relativeAddress, paymentID,
          query, type);
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // ---------------------------------- FINAL ---------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  protected static final String FINAL = "final";

  protected void finalPost(HttpServletRequest request,
      HttpServletResponse response, String absoluteAddress,
      String relativeAddress, Long paymentID) throws Exception {

    PaymentInfo paymentInfo = getPaymentInfo(paymentID);

    
    //  XYZ TODO Eliminar
    Map<String, String[]> parameters = request.getParameterMap();

    if (parameters.size() == 0) {
      log.info("PagamentID[" + paymentID + "] = >> SENSE PARAMETRES");
    } else {

      log.info("LLISTA PARAMETRES PAYMENTID=" + paymentID);

      for (String parameter : parameters.keySet()) {
        String[] values = parameters.get(parameter);
        log.info("       - " + parameter + " ==>> |" + values[0] + "|");
      }
    }


    /*
     * authResult The result of the payment. One of: ◦AUTHORISED Payment
     * authorisation was successfully completed. ◦REFUSED Payment was refused /
     * payment authorisation was unsuccessful. ◦CANCELLED Payment attempt was
     * cancelled by the shopper or the shopper requested to return to the
     * merchant by pressing the back button on the initial page. ◦PENDING Final
     * status of the payment attempt could not be established immediately. This
     * can happen if the systems providing final payment status are unavailable
     * or the shopper needs to take further action to complete the payment.
     * ◦ERROR An error occurred during the payment processing.
     */

    String authResult = request.getParameter("authResult");

    PaymentStatus status = getPaymentStatus(paymentID);

    if ("AUTHORISED".equals(authResult)) {
      status.setStatus(PaymentStatus.STATUS_AUTHORISED);
      status.setPaymentReference(request.getParameter("pspReference"));
      status.setPaymentMethod(request.getParameter("paymentMethod"));
    } else if ("REFUSED".equals(authResult)) {
      status.setStatus(PaymentStatus.STATUS_ERROR);
      // TODO Traduir !!!!!
      status
          .setErrorMsg("La Passarel·la de Pagament no ha acceptat el sistema de pagament introduït pel comprador.");
    } else if ("CANCELLED".equals(authResult)) {

      status.setStatus(PaymentStatus.STATUS_CANCELLED);
      // TODO Traduir !!!!!
      status.setErrorMsg("L'usuari ha cancel·lat el pagament");
    } else if ("PENDING".equals(authResult)) {
      status.setStatus(PaymentStatus.STATUS_ERROR);
      // TODO Traduir !!!!!
      status
          .setErrorMsg("No s'accepta el tipus de pagament que ha realitzat. L'hauria d'anul·lar.");
    } else {
      status.setStatus(PaymentStatus.STATUS_ERROR);
      // TODO Traduir !!!!!
      status.setErrorMsg("Final de pagament amb estat desconegut ("
          + authResult + ").");
    }

    response.sendRedirect(paymentInfo.getReturnUrl());

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // ---------------------------------- START ---------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  protected static final String START = "start";

  protected void startGet(HttpServletRequest request,
      HttpServletResponse response, String absoluteAddress,
      String relativeAddress, Long paymentID) throws Exception {

    PaymentInfo paymentInfo = getPaymentInfo(paymentID);

    // Generate dates
    Calendar calendar = Calendar.getInstance();
    // Date currentDate = calendar.getTime(); // current date
    calendar.add(Calendar.DATE, 1);
    Date sessionDate = calendar.getTime(); // current date + 1 day
    calendar.add(Calendar.DATE, 2);
    Date shippingDate = calendar.getTime(); // current date + 3 days

    final String hmacKey = getPropertyRequired(HMACKEY_SHA1);
    final String skinCode = getPropertyRequired(SKINCODE);
    final String merchantAccount = getPropertyRequired(MERCHANTACCOUNT);

    final String shopperEmail = paymentInfo.getShopperEmail();
    final String returnURL = absoluteAddress + FINAL + "/" + paymentID;

    // Define variables

    String paymentAmount = String
        .valueOf((int) (paymentInfo.getAmount() * 100));
    String currencyCode = paymentInfo.getCurrencyCodeISO4217(); // "EUR";
    String shopperLocale = paymentInfo.getShopperLocale().toString(); // "es_ES";

    final String allowedMethods = "card"; // card

    String merchantReturnData = "paymentID_" + paymentID;

    // Define variables
    String merchantReference = paymentInfo.getDescriptionProduct();

    String shipBeforeDate = new SimpleDateFormat("yyyy-MM-dd")
        .format(shippingDate);

    // TODO XYZ "2016-03-25T20:28:28+00:00";

    String z = new SimpleDateFormat("Z").format(sessionDate);
    String XXX = z.substring(0, 3) + ":" + z.substring(3);

    String sessionValidity = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" + XXX)
        .format(sessionDate);

    String shopperReference = paymentInfo.getShopperName(); // Nom del comprador

    System.out.println(" paymentAmount: |" + paymentAmount + "|");
    System.out.println(" currencyCode: |" + currencyCode + "|");
    System.out.println(" shipBeforeDate: |" + shipBeforeDate + "|");
    System.out.println(" merchantReference: |" + merchantReference + "|");
    System.out.println(" skinCode: |" + skinCode + "|");
    System.out.println(" merchantAccount: |" + merchantAccount + "|");
    System.out.println(" sessionValidity: |" + sessionValidity + "|");
    System.out.println(" shopperEmail: |" + shopperEmail + "|");
    System.out.println(" shopperReference: |" + shopperReference + "|");
    System.out.println(" merchantReturnData: |" + merchantReturnData + "|");

    // Signing the open invoice data
    // merchantSig
    String signingString = paymentAmount + currencyCode + shipBeforeDate
        + merchantReference + skinCode + merchantAccount + sessionValidity
        + shopperEmail + shopperReference + allowedMethods + merchantReturnData;

    String merchantSig;

    merchantSig = calculateHMAC(hmacKey, signingString);

    System.out.println(" signingString: |" + signingString + "|");
    System.out.println(" merchantSig: |" + merchantSig + "|");

    // Set correct character encoding
    response.setCharacterEncoding("UTF-8");

    String html = "<html>\n" + "<body>\n" + " <form action=\""
        + getPropertyRequired(URL)
        + "\" method=\"POST\" >\n"
        // paymentAmount +
        + "     <input type=\"text\" name=\"paymentAmount\" value=\""
        + paymentAmount
        + "\" /> <br/>\n"
        // currencyCode +
        + "    <input type=\"text\" name=\"currencyCode\" value=\""
        + currencyCode
        + "\" /> <br/>\n"
        // shipBeforeDate +
        + "     <input type=\"text\" name=\"shipBeforeDate\" value=\""
        + shipBeforeDate
        + "\" /> <br/>\n"
        // merchantReference +
        + "     <input type=\"text\" name=\"merchantReference\" value=\""
        + merchantReference
        + "\" /> <br/>\n"
        // skinCode +
        + "    <input type=\"text\" name=\"skinCode\" value=\""
        + skinCode
        + "\" /> <br/>\n"
        // merchantAccount +
        + "     <input type=\"text\" name=\"merchantAccount\" value=\""
        + merchantAccount
        + "\" /> <br/>\n"
        // sessionValidity +
        + "     <input type=\"text\" name=\"sessionValidity\" value=\""
        + sessionValidity
        + "\" /> <br/>\n"
        // shopperEmail +
        + "     <input type=\"text\" name=\"shopperEmail\" value=\""
        + shopperEmail
        + "\" /> <br/>\n"
        // + allowedMethods
        + "     <input type=\"text\" name=\"allowedMethods\" value=\""
        + allowedMethods
        + "\" /> <br/>\n"
        // shopperLocale
        + "     <input type=\"text\" name=\"shopperLocale\" value=\""
        + shopperLocale
        + "\" /> <br/>\n"
        // shopperReference
        + "     <input type=\"text\" name=\"shopperReference\" value=\""
        + shopperReference
        + "\" /> <br/>\n"
        // merchantReturnData;
        + "    <input type=\"text\" name=\"merchantReturnData\" value=\""
        + merchantReturnData
        + "\" /> <br/>\n"

        + "     <input type=\"text\" name=\"merchantSig\" value=\""
        + merchantSig
        + "\" /> <br/>\n"
        + "     <input type=\"text\" name=\"resURL\" value=\""
        + returnURL
        + "\" /> <br/>\n"

        // TODO ELIMINAR Substituir per javascript amb submit
        + "<input type =\"submit\" name=\"submit\" value=\"Submit Button\" />\n"
        + "</form>\n" + " </body>\n" + " </html>\n";

    response.getWriter().print(html);

  }

  /**
   * Computes the Base64 encoded signature using the HMAC algorithm with the
   * SHA-1 hashing function.
   */
  protected static String calculateHMAC(String hmacKey, String signingString)
      throws GeneralSecurityException, UnsupportedEncodingException {
    SecretKeySpec keySpec = new SecretKeySpec(hmacKey.getBytes(), "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(keySpec);

    byte[] result = mac.doFinal(signingString.getBytes("UTF-8"));
    return Base64.encodeBase64String(result);
  }

}
