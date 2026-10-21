package kh.edu.istad.platform.customer.persistence.convert;

import kh.edu.istad.platform.customer.domain.valueobject.Email;
import org.bson.BsonString;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class EmailWriteConverter implements Converter<Email, String> {

    @Override
    public String convert(Email source) {
        return source.value();
    }
}
