package com.brspayment.payment;

import com.brspayment.exception.BRSException;
import com.brspayment.messages.BookingMessage;
import com.brspayment.messages.BusBookingMessage;
import com.brspayment.messages.MessageBroker;
import com.brspayment.messages.MessageDestinationConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {


    private final PaymentRepository paymentRepository;

    private final MessageBroker messageBroker;
    private final TransactionRepository transactionRepository;

    @Autowired
    PaymentService(PaymentRepository paymentRepository,
                   MessageBroker messageBroker,
                   TransactionRepository transactionRepository) {
        this.paymentRepository = paymentRepository;
        this.messageBroker = messageBroker;
        this.transactionRepository = transactionRepository;
    }

    @JmsListener(destination = MessageDestinationConst.DEST_PROCESS_PAYMENT)
    public void receiveMessage(BookingMessage bookingMessage) {
        Payment payment = new Payment();
        payment.setBookingId(bookingMessage.getBookingId());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentAmount(100f);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.UPI);
        paymentRepository.save(payment);
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
}
