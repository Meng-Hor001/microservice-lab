package kh.edu.istad.platform.iam.application.port.out;

import java.util.function.Supplier;

public interface TransactionRunner {
    <T> T execute(Supplier<T> operations);
}
