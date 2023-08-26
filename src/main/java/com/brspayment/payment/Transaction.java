package com.brspayment.payment;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "transaction", schema = "bus_reservation_db")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "transaction_date")
    private Instant transactionDate;

    @Column(name = "transaction_type", length = 50)
    private String transactionType;

    @Column(name = "transaction_amount")
    private Float transactionAmount;

    @Column(name = "transaction_status", length = 20)
    private String transactionStatus;

    @Column(name = "transaction_details", length = 100)
    private String transactionDetails;

}