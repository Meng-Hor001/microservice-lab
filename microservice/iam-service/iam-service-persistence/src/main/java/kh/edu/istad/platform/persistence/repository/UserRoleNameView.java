package kh.edu.istad.platform.persistence.repository;

/** One row of a "role names per user" query. */
public interface UserRoleNameView {
    Long getUserId();

    String getRoleName();
}
