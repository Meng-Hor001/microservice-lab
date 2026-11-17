package kh.edu.istad.platform.persistence.repository;

/** One row of a "count per permission" query: the permission's id and how many rows matched it. */
public interface PermissionCountView {
    Long getPermissionId();

    Long getTotal();
}
