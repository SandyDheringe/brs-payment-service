package com.brspayment.payment;

import com.brspayment.exception.BRSException;
import com.brspayment.util.MessageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {


    private final PaymentRepository paymentRepository;

    private final MessageBroker messageBroker;

    @Autowired
    PaymentService(PaymentRepository paymentRepository,
                   MessageBroker messageBroker) {
        this.paymentRepository = paymentRepository;
        this.messageBroker = messageBroker;
    }

    @JmsListener(destination = "brsqueue")
    public void receiveMessage(String message) {
        Payment payment = new Payment();
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentAmount(100f);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.UPI);
        paymentRepository.save(payment);
    }


    public void processPayment(PaymentRequest paymentRequest) {
        Payment payment = paymentRepository.findByBookingId(paymentRequest.getBookingId()).orElse(null);
        if (payment == null) {
            throw new BRSException(String.format("Payment for booking %d not found", paymentRequest.getBookingId()));
        }

        payment.setPaymentAmount(paymentRequest.getPaymentAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.saveAndFlush(payment);
        messageBroker.sendMessage("brsqueue","booking id="+paymentRequest.getBookingId());

    }
}
