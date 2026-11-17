package kh.edu.istad.platform.persistence.repository;

/** One row of a "count per role" query: the role's id and how many rows matched it. */
public interface RoleCountView {
    Long getRoleId();

    Long getTotal();
}
