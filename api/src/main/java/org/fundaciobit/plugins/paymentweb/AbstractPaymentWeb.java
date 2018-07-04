package org.fundaciobit.plugins.paymentweb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fundaciobit.plugins.utils.AbstractPluginProperties;

/**
 * 
 * @author xmas
 *
 */
public abstract class AbstractPaymentWeb extends AbstractPluginProperties
  implements IPaymentWebPlugin {
  
   public Logger log = Logger.getLogger(this.getClass());
  
   
   
  
  public AbstractPaymentWeb() {
    super();
    // TODO Auto-generated constructor stub
  }


  public AbstractPaymentWeb(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
    // TODO Auto-generated constructor stub
  }


  public AbstractPaymentWeb(String propertyKeyBase) {
    super(propertyKeyBase);
    // TODO Auto-generated constructor stub
  }


  @Override
  public PaymentStatus getPaymentStatus(long paymentID) throws Exception {
    
    PaymentWebItem pwi = getPaymentWebItem(paymentID);
    if (pwi == null) {
      return null;
    } else {
      return pwi.paymentStatus;
    }
    
  }
  
  
  @Override
  public PaymentInfo getPaymentInfo(long paymentID) throws Exception {
    
    PaymentWebItem pwi = getPaymentWebItem(paymentID);
    if (pwi == null) {
      return null;
    } else {
      return pwi.paymentInfo;
    }
    
  }
  
  
  @Override
  public void closePayment(long paymentID) {
    synchronized (allPayments) {
      checkExpiredPayments();
      allPayments.remove(paymentID);
    }
  }
  
  
  protected void registryPayment(long paymentID, PaymentInfo paymentInfo) {
    synchronized (allPayments) {
      checkExpiredPayments();
      allPayments.put(paymentID, new PaymentWebItem(paymentInfo));
    }
  }
  
 

  

  
  
  // --------------------------------------------------------------
  // --------------------------------------------------------------
  // ----------------------------  MAP  ---------------------------
  // --------------------------------------------------------------
  // --------------------------------------------------------------
  
 
 public class PaymentWebItem {
    
    private PaymentInfo paymentInfo;
    
    private PaymentStatus paymentStatus;

    public PaymentWebItem(PaymentInfo paymentInfo) {
      super();
      this.paymentInfo = paymentInfo;
      this.paymentStatus = new PaymentStatus();
    }

    public PaymentInfo getPaymentInfo() {
      return paymentInfo;
    }

    public PaymentStatus getPaymentStatus() {
      return paymentStatus;
    }

  }
 
 protected static final Map<Long, PaymentWebItem> allPayments = new HashMap<Long, PaymentWebItem>();

 
 private PaymentWebItem getPaymentWebItem(long paymentID) {
   PaymentWebItem pwi;
   synchronized (allPayments) {
     checkExpiredPayments();
     pwi = allPayments.get(paymentID);
   }
   return pwi;
 }
 
 public static long lastCheckFirmesCaducades = 0;
 
 private void checkExpiredPayments() {
     
     Long now = System.currentTimeMillis();
    
     final long un_minut_en_ms =  60 * 60 * 1000;
    
     if (now + un_minut_en_ms > lastCheckFirmesCaducades) {
       lastCheckFirmesCaducades = now;
       List<Long> keysToDelete = new ArrayList<Long>();
      
       Set<Long> ids = allPayments.keySet();
       for (Long id : ids) {
         PaymentWebItem ssf = allPayments.get(id);
         if (ssf == null) {
           continue;
         }
        

         if (now >  ssf.getPaymentInfo().getExpireDate().getTime()) {
           keysToDelete.add(id);
           SimpleDateFormat sdf = new SimpleDateFormat();
           log.info("Passarel·la De Firma: Tancant SignatureSET amb ID = "
               + id + " a causa de que està caducat "
               + "( ARA: " + sdf.format(new Date(now)) + " | CADUCITAT: " 
               + sdf.format( ssf.getPaymentInfo().getExpireDate()) + ")");
         }
       }
      
       if (keysToDelete.size() != 0) {
         synchronized (allPayments) {

           for (Long id : keysToDelete) {
             closePayment(id);
           }
         }
       }
     }
   }
 
 
 
  
}
