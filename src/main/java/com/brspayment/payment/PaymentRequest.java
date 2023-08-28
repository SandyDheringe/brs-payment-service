package com.brspayment.payment;

import lombok.Data;

@Data
public class PaymentRequest {
    private Integer bookingId;
    private Integer busId;
    private Float paymentAmount;
    private PaymentMethod paymentMethod;
}
