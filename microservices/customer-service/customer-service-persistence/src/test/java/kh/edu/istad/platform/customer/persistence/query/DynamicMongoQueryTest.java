package kh.edu.istad.platform.customer.persistence.query;

import kh.edu.istad.common.domain.exception.CustomerDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.*;

class DynamicMongoQueryTest {
    @Test
    void combinesExactFiltersWithLiteralCaseInsensitiveKeywordAndPaging() {
        var query = new DynamicMongoQuery().equal("status", "ACTIVE")
                .equal("segmentId", "gold").keyword(" .* ", "username", "email")
                .page(2, 10, Sort.by("username", "id"));
        String json = query.getQueryObject().toJson();
        assertThat(json).contains("$and", "$or", "ACTIVE", "gold", "username", "email", "\\\\Q.*\\\\E");
        assertThat(query.getSkip()).isEqualTo(20);
        assertThat(query.getLimit()).isEqualTo(10);
        assertThat(query.getSortObject().keySet()).containsExactly("username", "id");
    }

    @Test
    void optionalFiltersCanBeOmitted() {
        var query = new DynamicMongoQuery().equal("status", null).keyword("  ", "name")
                .page(0, 10, Sort.unsorted());
        assertThat(query.getQueryObject()).isEmpty();
    }

    @Test
    void rejectsInvalidPaginationAndSupportsLargeOffsets() {
        for (int[] pair : new int[][]{{-1, 10}, {0, 0}, {0, 101}}) {
            assertThatThrownBy(() -> new DynamicMongoQuery().page(pair[0], pair[1], Sort.unsorted()))
                    .isInstanceOf(CustomerDomainException.class);
        }
        assertThat(new DynamicMongoQuery().page(Integer.MAX_VALUE, 100, Sort.unsorted()).getSkip())
                .isEqualTo(214748364700L);
    }
}
