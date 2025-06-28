package com.example.uclub_backend.repository;

import com.example.uclub_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByRole(User.UserRole role);

    List<User> findByStatus(User.UserStatus status);

    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:keyword% OR u.email LIKE %:keyword% OR u.account LIKE %:keyword%")
    List<User> findByKeyword(@Param("keyword") String keyword);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = '社团管理员' OR u.role = '系统管理员'")
    List<User> findAdmins();

    Optional<User> findByAccount(String account);
    boolean existsByAccount(String account);

    @Query("SELECT u FROM User u " +
            "WHERE (:email IS NULL OR u.email LIKE %:email%) " +
            "AND (:nickname IS NULL OR u.nickname LIKE %:nickname%) " +
            "AND (:role IS NULL OR u.role LIKE %:role%) " +
            "AND (:status IS NULL OR u.status = :status)")
    List<User> queryUsers(@Param("email") String email,
                          @Param("nickname") String nickname,
                          @Param("role") String role,
                          @Param("status") String status);

    Optional<User> findByEmail(String email);
}

