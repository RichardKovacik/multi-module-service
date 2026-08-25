package sk.mvp.user_service.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@ActiveProfiles("test") // použije application-test.properties
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//
//@Sql(scripts = "/data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//@Sql(statements = {
//        "TRUNCATE TABLE user_role RESTART IDENTITY CASCADE",
//        "TRUNCATE TABLE contact RESTART IDENTITY CASCADE",
//        "TRUNCATE TABLE role RESTART IDENTITY CASCADE",
//        "TRUNCATE TABLE users RESTART IDENTITY CASCADE"
//}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class UserServiceE2E {

//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Value("${api.base-url}")
//    private String baseUrl;
//
//    @Order(2)
//    @ParameterizedTest
//    @ValueSource(strings = {"marek.kovac@example.com","lucia.siposova@example.com"})
//    void shouldReturnUserByEmail_parametrized(String email) {
//        ResponseEntity<UserProfile> response = restTemplate.getForEntity(
//                baseUrl + "/api/users/by-email/" + email,
//                UserProfile.class
//        );
//
//        // check status
//        assertEquals(200, response.getStatusCode().value());
//        // Overíme email vo vnútri contact DTO
//        assertEquals(email, Objects.requireNonNull(response.getBody()).getContact().email());
//    }
//
//    @Order(1)
//    @ParameterizedTest
//    @ValueSource(strings = {"marek.kovac@example.com","lucia.siposova@example.com"})
//    void shouldDeleteUserByEmail_parametrized(String email) {
//        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
//                baseUrl + "/api/users/delete/by-email/" + email,
//                HttpMethod.DELETE,
//                null,
//                Void.class
//        );
//        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());
//
//        Optional<User> user = userRepository.findByEmail(email);
//        assertTrue(user.isEmpty());
//    }
//}
