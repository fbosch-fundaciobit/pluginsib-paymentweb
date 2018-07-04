package org.fundaciobit.plugins.paymentweb.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
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
public class TestPaymentWebPlugin extends AbstractPaymentWeb {

  public static final String TEST_PAYMENTWEB_BASE_PROPERTY = PAYMENTWEB_BASE_PROPERTY
      + "test.";
  
  
  public TestPaymentWebPlugin() {
    super();
    // TODO Auto-generated constructor stub
  }

  public TestPaymentWebPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
    // TODO Auto-generated constructor stub
  }

  public TestPaymentWebPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
    // TODO Auto-generated constructor stub
  }

  @Override
  public String getName(Locale locale) throws Exception {
    return "Test";
  }

  @Override
  public StartPaymentResult startPayment(HttpServletRequest request,
      String absoluteAddress, String relativeAddress, PaymentInfo paymentInfo)
      throws Exception {

    final long paymentID = paymentInfo.getPaymentID();

    registryPayment(paymentID, paymentInfo);

    String redirectUrl = relativeAddress + paymentID + "/"  +  START;

    boolean relativeUrl = true;

    return new StartPaymentResult(redirectUrl, relativeUrl);

  }

  @Override
  public void controllerGET(HttpServletRequest request,
      HttpServletResponse response, String absoluteAddress,
      String relativeAddress, long paymentID, String query) throws Exception {

    log.error(" XYZ ZZZZZZZZZZZZZZ   query ==> |" + query + "|");
    log.error(" XYZ ZZZZZZZZZZZZZZ   query.startsWith(START) ==> |" + query.startsWith(START) + "|");
    log.error(" XYZ ZZZZZZZZZZZZZZ    query.equals(START) ==> |" + query.equals(START) + "|");
    if (query.startsWith(START)) {
      startGet(request, response, absoluteAddress, relativeAddress, paymentID);
    } else if (query.startsWith(FINAL)) {
      finalPost(request, response, absoluteAddress, relativeAddress, paymentID);
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


    

    String statusStr = request.getParameter("status");
    long statusID = Long.parseLong(statusStr);

    PaymentStatus status = getPaymentStatus(paymentID);

    if (PaymentStatus.STATUS_AUTHORISED == statusID) {
      status.setStatus(PaymentStatus.STATUS_AUTHORISED);
      status.setPaymentReference(request.getParameter("pspReference"));
      status.setPaymentMethod(request.getParameter("paymentMethod"));
      
      
    } else if (PaymentStatus.STATUS_ERROR == statusID) {
      status.setStatus(PaymentStatus.STATUS_ERROR);
      
      String error = request.getParameter("error");
      // TODO Traduir !!!!!
      status
          .setErrorMsg("S'ha produit el següent error durant el pagament:" + error );
    } else if (PaymentStatus.STATUS_CANCELLED == statusID) {

      status.setStatus(PaymentStatus.STATUS_CANCELLED);
      // TODO Traduir !!!!!
      status.setErrorMsg("L'usuari ha cancel·lat el pagament");
    }  else {
      status.setStatus(PaymentStatus.STATUS_ERROR);
      
      String msg = "Final de pagament amb estat desconegut (|"
          + statusStr + "|)."; // TODO Traduir !!!!!
      log.error(msg, new Exception(msg));
      status.setErrorMsg(msg);
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

    // Set correct character encoding
    response.setCharacterEncoding("UTF-8");

    String html = "<html>\n" 
                 + "<body>\n"
                 + "<h2>Dades de Pagament</h2>\n"
       // AQUI DADES DE PAGAMENT
       
        +  "getAmount: " + paymentInfo.getAmount() + "<br/>\n"
        +  "getCurrencyCodeISO4217: " + paymentInfo.getCurrencyCodeISO4217() + "<br/>\n"
        +  "getDescriptionProduct: " + paymentInfo.getDescriptionProduct() + "<br/>\n"
        +  "getExpireDate: " + paymentInfo.getExpireDate() + "<br/>\n"
        +  "getPaymentID: " + paymentInfo.getPaymentID() + "<br/>\n"
        +  "getReturnUrl: " + paymentInfo.getReturnUrl() + "<br/>\n"
        +  "getSellerName: " + paymentInfo.getSellerName() + "<br/>\n"
        +  "getShopperEmail: " + paymentInfo.getShopperEmail() + "<br/>\n"
        +  "getShopperLocale: " + paymentInfo.getShopperLocale() + "<br/>\n"
        +  "getShopperName: " + paymentInfo.getShopperName() + "<br/><br/>\n";


    
    
    final int[] estats = { PaymentStatus.STATUS_AUTHORISED, PaymentStatus.STATUS_CANCELLED,
        PaymentStatus.STATUS_ERROR, PaymentStatus.STATUS_INPROGRESS};
    final String[] estatsStr = { "STATUS_AUTHORISED", "STATUS_CANCELLED",
        "STATUS_ERROR", "STATUS_INPROGRESS" };
    for (int i = 0; i < estats.length; i++) {
      
    
      html = html + "<table border=1px><tr><td> <form action=\""
        + absoluteAddress  + paymentID + "/" + FINAL
        + "\" method=\"POST\" >\n"
        + "     <input type=\"hidden\" name=\"status\" value=\""
        + estats[i]
        + "\" />\n";

        if (estats[i] == PaymentStatus.STATUS_ERROR) {
         html = html + " Error:   <input type=\"text\" name=\"error\" value=\""
            + "El numero de la targeta es incorrecte"
            + "\" /> <br/>\n";
        } else if(estats[i] == PaymentStatus.STATUS_AUTHORISED) {
          html = html + " paymentMethod:   <input type=\"text\" name=\"paymentMethod\" value=\""
              + "mc" + "\" /> <br/>\n"
              + " pspReference:   <input type=\"text\" name=\"pspReference\" value=\""
              + System.currentTimeMillis()
              + "\" /> <br/>\n";
          
          
        }
      

        html = html    + "<input type =\"submit\" name=\"" + estatsStr[i] + "\" value=\"" + estatsStr[i] + "\" />\n"
        + "</form></td></tr></table><br/>\n";
    }
    
    html = html + " </body>\n" + " </html>\n";

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
