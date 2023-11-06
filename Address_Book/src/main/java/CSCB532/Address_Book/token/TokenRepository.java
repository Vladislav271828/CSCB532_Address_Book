package CSCB532.Address_Book.token;

import CSCB532.Address_Book.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    //    @Transactional
    @Query("""
            select t from Token t inner join User u on t.user.id = u.id where u.id = :userId and (t.expired = false or t.revoked = false)
            """)
    List<Token> findAllValidTokensByUser(Integer userId);

    //    @Transactional
    Optional<Token> findByToken(String token);


}
