package kh.edu.istad.platform.customer.persistence.convert;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.UUID;

@ReadingConverter
public class CustomerIdReadConverter implements Converter<Document, CustomerId> {

    @Override
    public CustomerId convert(Document source) {
        return new CustomerId(UUID.fromString(source.getString("_id")));
    }
}
