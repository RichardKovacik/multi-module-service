package sk.mvp.user_service.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sk.mvp.user_service.admin.dto.response.AdminUserListItemResp;
import sk.mvp.user_service.user.entity.Gender;
import sk.mvp.user_service.user.entity.User;
import sk.mvp.user_service.projections.UserSummaryProjection;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findByFirstName(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    @Query("select u from User u JOIN u.contact c where c.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("select u from User u JOIN FETCH u.contact c where c.email = :email or u.username = :username")
    Optional<User> findByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    @Modifying
    @Query("update User u set u.enabled = :flag where u.username = :username")
    void setEnabled(@Param("username") String username, @Param("flag") boolean flag);

    @Modifying
    @Query("update User u set u.enabled = :flag where u.id = :userId")
    void setEnabled(@Param("userId") Long userId, @Param("flag") boolean flag);

    @Query("select u.tokenVersion from User u where u.username = :username")
    Optional<Integer> getTokenVersion(@Param("username") String username);

    @Modifying
    @Query("update User u set u.tokenVersion = u.tokenVersion + 1 where u.username = :username")
    void incrementTokenVersion(@Param("username") String username);

    User save(User user);

    void delete(User user);

    @Modifying
    @Query("delete from User u " +
            "where u.id in " +
            "(select c.user.id" +
            " from Contact c" +
            " where c.email = :email)")
    void deleteUserByEmail(@Param("email") String email);

    @Query(
            value = "SELECT u FROM User u LEFT JOIN FETCH u.contact",
            countQuery = "SELECT count(u) FROM User u")
    Page<User> findAll(Pageable pageable);
    Page<UserSummaryProjection> findAllByGender(Gender gender, Pageable pageable);
}
