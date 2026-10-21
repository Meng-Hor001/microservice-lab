package kh.edu.istad.platform.customer.persistence.convert;

import kh.edu.istad.platform.customer.domain.valueobject.Email;
import org.bson.BsonString;
import org.springframework.core.convert.converter.Converter;

public class EmailReadConverter implements Converter<String, Email> {
    @Override
    public Email convert(String source) {
        return new Email(source);
    }
}
