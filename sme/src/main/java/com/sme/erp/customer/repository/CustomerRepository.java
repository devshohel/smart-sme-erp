package com.sme.erp.customer.repository;

import com.sme.erp.customer.entity.Customer;
import com.sme.erp.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findTopByOrderByIdDesc();

    boolean existsByCustomerCode(String customerCode);

    boolean existsByCustomerCodeAndIdNot(String customerCode, Long id);

    @Query(value = "SELECT COALESCE(MAX(id), 0) FROM customers", nativeQuery = true)
    Long findMaxIdIncludingDeleted();

    @Query(value = "SELECT COUNT(*) FROM customers WHERE customer_code = :customerCode", nativeQuery = true)
    Long countByCustomerCodeIncludingDeleted(@Param("customerCode") String customerCode);

    @Query(value = "SELECT COUNT(*) FROM customers WHERE customer_code = :customerCode AND id <> :id", nativeQuery = true)
    Long countByCustomerCodeAndIdNotIncludingDeleted(@Param("customerCode") String customerCode, @Param("id") Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    List<Customer> findByStatus(Status status);

    @Query("""
            SELECT c
            FROM Customer c
            WHERE (:status IS NULL OR c.status = :status)
              AND (
                    :keyword IS NULL
                    OR LOWER(COALESCE(c.customerCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.companyName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.contactPerson, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.city, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    List<Customer> search(@Param("keyword") String keyword, @Param("status") Status status);

    @Query("""
            SELECT c
            FROM Customer c
            WHERE (:status IS NULL OR c.status = :status)
              AND (
                    :keyword IS NULL
                    OR LOWER(COALESCE(c.customerCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Customer> searchPage(@Param("keyword") String keyword, @Param("status") Status status, Pageable pageable);

    @Query("""
            SELECT c
            FROM Customer c
            WHERE :keyword IS NULL
               OR LOWER(COALESCE(c.customerCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(c.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY c.name ASC
            """)
    List<Customer> autocomplete(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE customers SET is_deleted = false, updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);

    @Query(value = "SELECT * FROM customers WHERE is_deleted = true ORDER BY id DESC", nativeQuery = true)
    List<Customer> findDeletedCustomers();
}
