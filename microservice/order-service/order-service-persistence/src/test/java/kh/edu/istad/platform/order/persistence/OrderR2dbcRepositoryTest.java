package kh.edu.istad.platform.order.persistence;


import kh.edu.istad.platform.order.persistence.repository.BusinessReactiveRepository;
import kh.edu.istad.platform.order.persistence.repository.CustomerReactiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;

@DataR2dbcTest
// @ContextConfiguration(classes = PersistenceTestConfig.class)
public class OrderR2dbcRepositoryTest {

    @Autowired
    private BusinessReactiveRepository businessReactiveRepository;

    @Test
    void findAll() {
        StepVerifier.create(
                        businessReactiveRepository.findAll().collectList()
                )
                .assertNext(businessEntity -> IO.println("Data: " + businessEntity))
                .verifyComplete();
    }
}
