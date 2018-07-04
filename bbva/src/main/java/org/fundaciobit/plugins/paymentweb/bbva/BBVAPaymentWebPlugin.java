package org.fundaciobit.plugins.paymentweb.bbva;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fundaciobit.plugins.paymentweb.AbstractPaymentWeb;
import org.fundaciobit.plugins.paymentweb.PaymentInfo;
import org.fundaciobit.plugins.paymentweb.PaymentStatus;
import org.fundaciobit.plugins.paymentweb.StartPaymentResult;

import sis.redsys.api.ApiMacSha256;

/**
 * 
 * @author anadal
 *
 */
public class BBVAPaymentWebPlugin extends AbstractPaymentWeb {

  public static final String BBVA_PAYMENTWEB_BASE_PROPERTY = PAYMENTWEB_BASE_PROPERTY
      + "bbva.";


  // https://sis-t.redsys.es:25443/sis/realizarPago  ==>>  Pruebas
  // https://sis.redsys.es/sis/realizarPago  ==>>   Real
  public static final String URL = BBVA_PAYMENTWEB_BASE_PROPERTY + "url";

  public static final String TERMINAL = BBVA_PAYMENTWEB_BASE_PROPERTY + "terminal";
  
  public static final String MERCHANTCODE = BBVA_PAYMENTWEB_BASE_PROPERTY + "merchantcode";
  
  public static final String CLAVE = BBVA_PAYMENTWEB_BASE_PROPERTY + "clave";
  
  
  
  
  
  
  public BBVAPaymentWebPlugin() {
    super();
    // TODO Auto-generated constructor stub
  }

  public BBVAPaymentWebPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
    // TODO Auto-generated constructor stub
  }

  public BBVAPaymentWebPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
    // TODO Auto-generated constructor stub
  }

  @Override
  public String getName(Locale locale) throws Exception {
    return "BBVA";
  }

  @Override
  public StartPaymentResult startPayment(HttpServletRequest request,
      String absoluteAddress, String relativeAddress, PaymentInfo paymentInfo)
      throws Exception {

    final long paymentID = paymentInfo.getPaymentID();

    registryPayment(paymentID, paymentInfo);

    String redirectUrl = relativeAddress  + paymentID + "/" + START;

    boolean relativeUrl = true;

    return new StartPaymentResult(redirectUrl, relativeUrl);

  }

  @Override
  public void controllerGET(HttpServletRequest request,
      HttpServletResponse response, String absoluteAddress,
      String relativeAddress, long paymentID, String query) throws Exception {

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
    } else if (query.startsWith(NOTIFICACIO)) {
      notificacioPost(request, response, absoluteAddress, relativeAddress, paymentID);
    } else {
      final String type = "POST";
      sendNotFound(response, absoluteAddress, relativeAddress, paymentID,
          query, type);
    }
  }
  
  
  
  
  
//--------------------------------------------------------------------------
 // --------------------------------------------------------------------------
 // ---------------------------------- FINAL ---------------------------------
 // --------------------------------------------------------------------------
 // --------------------------------------------------------------------------

 protected static final String NOTIFICACIO = "notificacio";

 protected void notificacioPost(HttpServletRequest request,
     HttpServletResponse response, String absoluteAddress,
     String relativeAddress, Long paymentID) throws Exception {

   PaymentInfo paymentInfo = getPaymentInfo(paymentID);
   
   log.info(" XYZ ===============  BBVA NOTIFICACIO " + paymentID + " ==================");
   
   commonFinalNotificacio(request, response, paymentID);
   
   
   log.info(" XYZ ---------------(End of NOTIFICACIO " + paymentID + ")------------------");

   
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

    log.info(" XYZ ===============  BBVA FINAL " + paymentID + " ==================");
    
    commonFinalNotificacio(request, response, paymentID);
    
    
    log.info(" XYZ ---------------(End of FINAL " + paymentID + ")-------------------");

    response.sendRedirect(paymentInfo.getReturnUrl());

  }

  protected void commonFinalNotificacio(HttpServletRequest request,
      HttpServletResponse response,
      Long paymentID) throws UnsupportedEncodingException, Exception {
    
    
    PaymentStatus status = getPaymentStatus(paymentID);
    
    if (status == null) {
      log.error("Status with paymentID = " + paymentID + " is nULL" , new Exception());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    
    if (status.getStatus() != PaymentStatus.STATUS_INPROGRESS) {
      
      log.info(" XYZ IIINNNFFFOOO  Ja s'ha assignat valor de Status pel paymentID = "
        + paymentID + ": " + status.getStatus());
      
      return;
    }
    
    
    //  XYZ TODO Eliminar
    Map<String, String[]> parameters = request.getParameterMap();

    if (parameters.size() == 0) {
      log.info("XYZ PagamentID[" + paymentID + "] = >> SENSE PARAMETRES");
    } else {

      log.info("XYZ LLISTA PARAMETRES PAYMENTID=" + paymentID);

      for (String parameter : parameters.keySet()) {
        String[] values = parameters.get(parameter);
        log.info("       - " + parameter + " ==>> |" + values[0] + "|");
      }
    }

    // XYZ Imprimir firma
    String version = request.getParameter("Ds_SignatureVersion");

    String params = request.getParameter("Ds_MerchantParameters");
    // XYZ Validar firma !!!!
    String signatureRecibida = request.getParameter("Ds_Signature");
    
    ApiMacSha256 apiMacSha256= new ApiMacSha256();

    String decodec = apiMacSha256.decodeMerchantParameters(params);
    System.out.println("XYZ DECODED =>  " + decodec);
    
    
    
    String[] all = new String[] { "Ds_Date","Ds_Hour","Ds_Amount","Ds_Currency",
        "Ds_Order","Ds_MerchantCode","Ds_Terminal","Ds_Response","Ds_MerchantData",
        "Ds_SecurePayment","Ds_TransactionType","Ds_Card_Country","Ds_AuthorisationCode",
        "Ds_ConsumerLanguage", "Ds_Card_Type"
    };
    StringBuffer allInfo = new StringBuffer();
    allInfo.append("Ds_SignatureVersion: " + version + "\n");
    for (int i = 0; i < all.length; i++) {
      try {
         String value = apiMacSha256.getParameter(all[i]);
         if (value != null) {
           allInfo.append(all[i] + ": " + value + "\n");
           System.out.println("XYZ " + all[i] + ":  " + value);
         }
      } catch(Exception e) {
        log.warn(e.getMessage());
      }
    }
    allInfo.append("Ds_Signature: " + signatureRecibida + "\n");
    
    String codigoRespuestaStr = apiMacSha256.getParameter("Ds_Response");
    

   
    
    if (codigoRespuestaStr == null || codigoRespuestaStr.trim().length() == 0) {
      status.setStatus(PaymentStatus.STATUS_ERROR);
      status.setErrorMsg("No s'ha pogut obtenir el parameter Ds_response de la resposta BBVA");
    } else {
      int codigoResp = Integer.parseInt(codigoRespuestaStr);
      
      if (codigoResp < 100 || codigoResp == 900 || codigoResp == 400) {
        status.setStatus(PaymentStatus.STATUS_AUTHORISED);
        
        String cardType = "unknown";
        try {
          cardType = apiMacSha256.getParameter("Ds_Card_Type");
          } catch(Exception e) {  }
        //String auth ="unknown";
//        try {
//          auth =  apiMacSha256.getParameter("Ds_AuthorisationCode");
//          
//          } catch(Exception e) {  }
        
        status.setPaymentReference(allInfo.toString());
            
            /*"Order: " + apiMacSha256.getParameter("Ds_Order") + "\n"
            + "AuthorisationCode: " + auth */
        status.setPaymentMethod(cardType);
      } else if (codigoResp == 9915) {
        status.setStatus(PaymentStatus.STATUS_CANCELLED);
      } else {
         // XYZ  TAULA D'ERRORS !!!!
        status.setStatus(PaymentStatus.STATUS_ERROR);
        status.setErrorMsg("S'ha produit un error en la passarel·la BBVA amb codi " + codigoResp);
      }
      
    }
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

    
    // XYZ ?????
    //final String shopperEmail = paymentInfo.getShopperEmail();

    

    
    final String returnURL = absoluteAddress  + paymentID + "/" + FINAL ;
    final String notificacioURL = absoluteAddress  + paymentID + "/" + NOTIFICACIO ;

    
    String paymentAmount = String.valueOf((int) (paymentInfo.getAmount() * 100));
    
    // XYZ Converter EUR to 978
    // String currencyCode = paymentInfo.getCurrencyCodeISO4217(); // "EUR";
    String currencyCode = "978";
    
    ApiMacSha256 apiMacSha256= new ApiMacSha256();
    
    String order = String.valueOf(Math.abs(Long.toString(paymentID, Character.MAX_RADIX).hashCode()));
    
    String merchantCode = getPropertyRequired(MERCHANTCODE);
    String terminal = getPropertyRequired(TERMINAL);

    apiMacSha256.setParameter("DS_MERCHANT_AMOUNT",paymentAmount);
    apiMacSha256.setParameter("DS_MERCHANT_ORDER", order);
    apiMacSha256.setParameter("DS_MERCHANT_MERCHANTCODE", merchantCode); //   FUC
    apiMacSha256.setParameter("DS_MERCHANT_CURRENCY", currencyCode);
    apiMacSha256.setParameter("DS_MERCHANT_TRANSACTIONTYPE", "0");
    apiMacSha256.setParameter("DS_MERCHANT_TERMINAL", terminal);
    apiMacSha256.setParameter("DS_MERCHANT_MERCHANTURL", notificacioURL ); // paymentInfo.getSellerUrl()
    apiMacSha256.setParameter("DS_MERCHANT_MERCHANTNAME", paymentInfo.getSellerName());
    apiMacSha256.setParameter("DS_MERCHANT_PRODUCTDESCRIPTION", paymentInfo.getDescriptionProduct());
    apiMacSha256.setParameter("DS_MERCHANT_TITULAR", paymentInfo.getShopperName());
    apiMacSha256.setParameter("DS_MERCHANT_URLOK",returnURL);
    apiMacSha256.setParameter("DS_MERCHANT_URLKO",returnURL);
    apiMacSha256.setParameter("DS_MERCHANT_MERCHANTDATA","" + paymentID);
    
    

    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_AMOUNT\"," + paymentAmount+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_ORDER\"," +  order+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_MERCHANTCODE\"," +  merchantCode+");"); //   FUC
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_CURRENCY\"," +  currencyCode+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_TRANSACTIONTYPE\"," +  "0"+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_TERMINAL\"," + terminal+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_MERCHANTURL\"," + notificacioURL); //  paymentInfo.getSellerUrl()+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_MERCHANTNAME\"," +  paymentInfo.getSellerName()+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_PRODUCTDESCRIPTION\"," +  paymentInfo.getDescriptionProduct()+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_TITULAR\"," +  paymentInfo.getShopperName()+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_URLOK\"," + returnURL+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_URLKO\"," + returnURL+");");
    System.out.println("apiMacSha256.setParameter(\"DS_MERCHANT_MERCHANTDATA\"," +  paymentID+");");

    
    /** XYZ
     * Opcional: el Valor 0, indicará que no se ha determinado el idioma del cliente (opcional).
     *  Otros valores posibles son:
     * Castellano-001, Inglés-002, Catalán-003, Francés-004, Alemán-005,
     * Holandés-006, Italiano-007, Sueco-008, Portugués-009, Valenciano-010,
     * Polaco-011, Gallego-012 y Euskera-013.
     */

    Locale locale = paymentInfo.getShopperLocale();
    String langBBVA;
    if (locale == null) {
      langBBVA = "003";
    } else {
      String lang = locale.getLanguage();
      
      if ("ca".equals(lang)) {
        langBBVA = "003";
      } else if ("es".equals(lang)) {
        langBBVA = "001";
      }  else if ("eu".equals(lang)) {
        langBBVA = "013";
      } else {
        langBBVA = "003";
      }

    }
    
    
    apiMacSha256.setParameter("Ds_Merchant_ConsumerLanguage", langBBVA);

    System.out.println("apiMacSha256.setParameter(\"Ds_Merchant_ConsumerLanguage\"," +  langBBVA+");");

    
    String params = apiMacSha256.createMerchantParameters();
    String signature = apiMacSha256.createMerchantSignature(getProperty(CLAVE));



    // Set correct character encoding
    response.setCharacterEncoding("UTF-8");
    
    String type = "text";

    String html = "<!DOCTYPE html>\n" + "<html>\n" + "<body>\n"
        + " <form id=\"formPagament\"  action=\"" + getPropertyRequired(URL) + "\" method=\"POST\" >\n"
        // ------------
        + "     <input type=\"" + type + "\" name=\"Ds_SignatureVersion\" value=\"HMAC_SHA256_V1\" /> <br/>\n"
        // --------------
        + "     <input type=\"" + type + "\" name=\"Ds_MerchantParameters\" value=\"" + params + "\" /> <br/>\n"
        // ------------------
        + "     <input type=\"" + type + "\" name=\"Ds_Signature\" value=\"" + signature + "\" /> <br/>\n"


        // TODO ELIMINAR Substituir per javascript amb submit
        + "<input type =\"submit\" name=\"submitButton\" value=\"Submit Button\" />\n"
        + "</form>\n"
        + "<script type=\"text/javascript\">\n"
        
        + "window.onload = function() {\n"
        //+ "   alert('hola');\n"
        + "   document.getElementById(\"formPagament\").submit();\n"
        + "};\n"

        + "</script>\n"

        + " </body>\n" 
        + " </html>\n";

    response.getWriter().print(html);

  }



}
