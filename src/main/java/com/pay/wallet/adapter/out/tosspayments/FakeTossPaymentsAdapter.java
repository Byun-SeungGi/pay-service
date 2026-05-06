package com.pay.wallet.adapter.out.tosspayments;

import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.application.port.out.TossPaymentStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("loadtest")
public class FakeTossPaymentsAdapter implements TossPaymentPort {

    @Override
    public String confirm(String paymentKey, String orderId, BigDecimal amount) {
        return """
                  {
                    "paymentKey": "%s",
                    "orderId": "%s",
                    "totalAmount": %s,
                    "status": "DONE",
                    "method": "카드"
                  }
                  """.formatted(paymentKey, orderId, amount);
    }

    @Override
    public String cancel(String paymentKey, String cancelReason, BigDecimal cancelAmount, String idempotencyKey) {
        return """
                  {
                    "paymentKey": "%s",
                    "status": "CANCELED"
                  }
                  """.formatted(paymentKey);
    }

    @Override
    public TossPaymentStatus getPayment(String paymentKey) {
        return new TossPaymentStatus(TossPaymentStatus.Action.APPROVE, "{}");
    }
}
