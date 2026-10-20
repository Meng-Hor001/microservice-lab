package kh.edu.istad.platform.customer.persistence.query;

import kh.edu.istad.common.domain.exception.CustomerDomainException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DynamicMongoQuery {
    private final List<Criteria> conditions = new ArrayList<>();

    public DynamicMongoQuery equal(String field, Object value) {
        if (value != null) {
            conditions.add(Criteria.where(field).is(value));
        }
        return this;
    }

    public DynamicMongoQuery keyword(
            String keyword,
            String... fields
    ) {
        if (keyword == null || keyword.isBlank()) {
            return this;
        }
        if (fields.length == 0){
            throw new IllegalArgumentException(
                    "At least one search field is required"
            );
        }

        String escapedKeyword = Pattern.quote(keyword.trim());

        List<Criteria> alternatives = new ArrayList<>();

        for (String field : fields) {
            alternatives.add(
                    Criteria.where(field)
                            .regex(escapedKeyword, "i")
            );
        }
        conditions.add(new Criteria().orOperator(alternatives.toArray(new Criteria[0])));

        return this;
    }

    public Query page(int page, int size, Sort sort){
        if (page< 0 || size < 1 || size > 100){
            throw new CustomerDomainException(
                    "page must be >= 0 and size between 1 and 100"
            );
        }

        Query query = new Query();

        if (!conditions.isEmpty()){
            query.addCriteria(
                    new Criteria().andOperator(
                            conditions.toArray(new Criteria[0])
                    )
            );
        }
        return query.with(PageRequest.of(page, size, sort));
    }
}
