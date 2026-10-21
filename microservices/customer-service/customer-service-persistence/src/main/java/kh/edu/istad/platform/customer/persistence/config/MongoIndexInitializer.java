package kh.edu.istad.platform.customer.persistence.config;

import kh.edu.istad.platform.customer.persistence.Document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.Document.CustomerSegmentDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoIndexInitializer implements ApplicationRunner {

    private final ReactiveMongoTemplate template;

    @Override
        public void run(ApplicationArguments args) {
            var customerIndexes = template.indexOps(CustomerDocument.class);
            var segmentIndexes = template.indexOps(CustomerSegmentDocument.class);

            customerIndexes.createIndex(
                            new Index()
                                    .on("username", Sort.Direction.ASC)
                                    .unique()
                    )
                    .then(customerIndexes.createIndex(
                            new Index()
                                    .on("email", Sort.Direction.ASC)
                                    .unique()
                    ))
                    .then(segmentIndexes.createIndex(
                            new Index()
                                    .on("name", Sort.Direction.ASC)
                                    .unique()
                    ))
                    .block();
    }
}
