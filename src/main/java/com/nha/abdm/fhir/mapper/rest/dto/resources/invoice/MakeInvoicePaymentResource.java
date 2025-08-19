/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoicePaymentResource {
  public PaymentReconciliation buildInvoicePayment(InvoiceBundleRequest invoiceBundleRequest)
      throws ParseException {

    if (invoiceBundleRequest == null || invoiceBundleRequest.getPayment() == null) {
      return null;
    }

    var paymentReq = invoiceBundleRequest.getPayment();
    var invoiceReq = invoiceBundleRequest.getInvoice();

    PaymentReconciliation paymentReconciliation = new PaymentReconciliation();
    paymentReconciliation.setId(UUID.randomUUID().toString());

    if (paymentReq.getStatus() != null) {
      paymentReconciliation.setStatus(
          PaymentReconciliation.PaymentReconciliationStatus.fromCode(
              paymentReq.getStatus().getValue()));
    }

    if (paymentReq.getPaymentDate() != null) {
      paymentReconciliation.setPaymentDate(Utils.getFormattedDate(paymentReq.getPaymentDate()));
    }

    if (paymentReq.getPaidAmount() != null && paymentReq.getPaidAmount().doubleValue() > 0) {
      paymentReconciliation.setPaymentAmount(
          new Money()
              .setValue(paymentReq.getPaidAmount())
              .setCurrency(invoiceReq != null ? invoiceReq.getCurrency() : "INR"));
    }

    if (paymentReq.getTransactionId() != null && !paymentReq.getTransactionId().isBlank()) {
      paymentReconciliation.setPaymentIdentifier(
          new Identifier().setValue(paymentReq.getTransactionId()));
    }
    if (paymentReq.getMethod() != null && !paymentReq.getMethod().isBlank()) {
      PaymentReconciliation.DetailsComponent detail = new PaymentReconciliation.DetailsComponent();
      detail.setType(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(ResourceProfileIdentifier.PROFILE_INVOICE_PAYMENT_TYPE)
                      .setCode(paymentReq.getMethod())
                      .setDisplay(paymentReq.getMethod())));

      paymentReconciliation.addDetail(detail);
    }

    return paymentReconciliation;
  }
}
