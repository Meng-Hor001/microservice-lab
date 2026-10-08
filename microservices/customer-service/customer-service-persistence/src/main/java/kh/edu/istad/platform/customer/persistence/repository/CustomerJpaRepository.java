package kh.edu.istad.platform.customer.persistence.repository;

import kh.edu.istad.platform.customer.persistence.entity.CustomerEntity;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity,UUID> {
}
