package com.brspayment.payment;

import com.brspayment.exception.BRSException;
import com.brspayment.messages.BookingMessage;
import com.brspayment.messages.BusBookingMessage;
import com.brspayment.messages.MessageBroker;
import com.brspayment.messages.MessageDestinationConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PaymentService {


    private final PaymentRepository paymentRepository;

    private final MessageBroker messageBroker;
    private final TransactionRepository transactionRepository;
private final ObjectMapper objectMapper;
    @Autowired
    PaymentService(PaymentRepository paymentRepository,
                   MessageBroker messageBroker,
                   TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.messageBroker = messageBroker;
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = MessageDestinationConst.DEST_PROCESS_PAYMENT)
    public void initiatePayment(Map<String, Object> object) {

        final BookingMessage bookingMessage = objectMapper.convertValue(object, BookingMessage.class);
        System.out.println("Received message: " + bookingMessage);

        Payment payment = new Payment();
        payment.setBookingId(bookingMessage.getBookingId());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentAmount(bookingMessage.getBookingAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.UPI);
        paymentRepository.saveAndFlush(payment);
    }

    public void processPayment(PaymentRequest paymentRequest) {
        Payment pendingPayment = paymentRepository.findByBookingId(paymentRequest.getBookingId()).orElse(null);
        if (pendingPayment == null) {
            throw new BRSException(String.format("Payment for booking %d not found", paymentRequest.getBookingId()));
        }

        pendingPayment.setPaymentAmount(paymentRequest.getPaymentAmount());
        pendingPayment.setPaymentMethod(paymentRequest.getPaymentMethod());
        pendingPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        pendingPayment.setPaymentDate(LocalDateTime.now());
        paymentRepository.saveAndFlush(pendingPayment);

        Transaction transaction = new Transaction();
        transaction.setBookingId(paymentRequest.getBookingId());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionEvent(TransactionEvent.BOOKING_CONFIRMED);
        transactionRepository.saveAndFlush(transaction);

        messageBroker.sendInventoryUpdateMessage(MessageDestinationConst.DEST_UPDATE_INVENTORY,
                new BusBookingMessage(paymentRequest.getBookingId(), paymentRequest.getBusId()));

    }

    @JmsListener(destination = MessageDestinationConst.DEST_INITIATE_PAYMENT_REFUND)
    public void initiateRefund(Map<String, Object> object) {
        final BookingMessage bookingMessage = objectMapper.convertValue(object, BookingMessage.class);
        System.out.println("Received message: " + bookingMessage);
        Payment payment = paymentRepository.findByBookingId(bookingMessage.getBookingId()).orElse(null);
        if(payment !=null) {
            payment.setPaymentStatus(PaymentStatus.REFUND_INITIATED);
            paymentRepository.saveAndFlush(payment);
        }
    }

}
