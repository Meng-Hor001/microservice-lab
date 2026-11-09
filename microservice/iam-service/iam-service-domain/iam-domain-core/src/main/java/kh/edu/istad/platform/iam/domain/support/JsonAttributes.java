package kh.edu.istad.platform.iam.domain.support;

import kh.edu.istad.platform.iam.domain.exception.DomainRuleException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static kh.edu.istad.platform.iam.domain.support.DomainRules.check;

public final class JsonAttributes {
    private JsonAttributes() {

    }

    public static Map<String, Object> copy(
            Map<String, Object> input
    ) {
        return input == null ? Map.of() : copyMap(input);
    }

    private static Map<String, Object> copyMap(Map<?, ?> input) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : input.entrySet()) {
            check(
                    entry.getKey() instanceof String,
                    "JSON object keys must be strings"
            );

            result.put(
                    (String) entry.getKey(),
                    copyValue(entry.getValue())
            );
        }

        return Collections.unmodifiableMap(result);
    }

    private static Object copyValue(Object value) {
        if (value == null
                || value instanceof String
                || value instanceof Boolean
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof BigInteger
                || value instanceof BigDecimal) {
            return value;
        }

        if (value instanceof Double number) {
            check(
                    Double.isFinite(number),
                    "JSON numbers must be finite"
            );

            return number;
        }

        if (value instanceof Float number) {
            check(
                    Float.isFinite(number),
                    "JSON numbers must be finite"
            );

            return number;
        }

        if (value instanceof Map<?, ?> map) {
            return copyMap(map);
        }

        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();

            for (Object item : list) {
                result.add(copyValue(item));
            }

            return Collections.unmodifiableList(result);
        }

        throw new DomainRuleException(
                "Unsupported JSON attribute type: "
                        + value.getClass().getSimpleName()
        );
    }
}
