package com.sme.erp.auth.repository;

import com.sme.erp.auth.entity.User;
import com.sme.erp.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u join fetch u.role where u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
            select u from User u
            join fetch u.role r
            where (:keyword is null or lower(u.name) like lower(concat('%', :keyword, '%'))
                or lower(u.username) like lower(concat('%', :keyword, '%'))
                or lower(u.email) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(u.phone, '')) like lower(concat('%', :keyword, '%')))
              and (:status is null or u.status = :status)
            order by u.id desc
            """)
    List<User> search(@Param("keyword") String keyword, @Param("status") Status status);
}
