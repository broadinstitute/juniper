package bio.terra.pearl.core.service;

import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * we mostly manage transactions declaratively, but there are some cases (e.g. we need to call another @Transactional
 * method from within the same service class) where we need explicit management.  Those cases can use this service.
 *
 * Adapted from
 * https://stackoverflow.com/questions/3423972/spring-transaction-method-call-by-the-method-within-the-same-class-does-not-wo
 */
@Service
public class TransactionHandler {
    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T runInTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T runInNewTransaction(Supplier<T> supplier) {
        return supplier.get();
    }
}
