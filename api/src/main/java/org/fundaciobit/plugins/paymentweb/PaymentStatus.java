package org.fundaciobit.plugins.paymentweb;

/**
 * 
 * @author anadal
 *
 */
public class PaymentStatus {

  public static final int STATUS_INPROGRESS = 0;

  /**
   * Payment authorisation was successfully completed.
   */
  public static final int STATUS_AUTHORISED = 1;

  /**
   * Payment was: - refused - fail authorisation - error
   */
  public static final int STATUS_ERROR = -1;

  /**
   * Payment cancelled
   */
  public static final int STATUS_CANCELLED = -2;

  int status = STATUS_INPROGRESS;

  String paymentReference;

  String errorMsg;

  String paymentMethod;

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getPaymentReference() {
    return paymentReference;
  }

  public void setPaymentReference(String paymentReference) {
    this.paymentReference = paymentReference;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

}
