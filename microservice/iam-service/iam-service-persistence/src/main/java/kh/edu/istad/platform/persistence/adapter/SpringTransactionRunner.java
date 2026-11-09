package kh.edu.istad.platform.persistence.adapter;


import kh.edu.istad.platform.iam.application.port.out.TransactionRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SpringTransactionRunner implements TransactionRunner {
    private final TransactionTemplate template;
    @Override
    public <T> T execute(Supplier<T> operations) {

        return template.execute(status -> operations.get());
    }
}
