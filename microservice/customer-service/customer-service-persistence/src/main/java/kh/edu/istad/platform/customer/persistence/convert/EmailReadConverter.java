package kh.edu.istad.platform.customer.persistence.convert;

import kh.edu.istad.platform.customer.domain.valueobject.Email;
import org.bson.BsonString;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class EmailReadConverter implements Converter<BsonString, Email> {

    @Override
    public Email convert(BsonString source) {
        return new Email(source.getValue());
    }
}
