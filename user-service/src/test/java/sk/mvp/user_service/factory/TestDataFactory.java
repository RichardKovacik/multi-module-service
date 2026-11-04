package sk.mvp.user_service.factory;

import sk.mvp.user_service.auth.dto.request.RegistrationReq;
import sk.mvp.user_service.auth.entity.VerificationToken;
import sk.mvp.user_service.auth.entity.VerificationTokenType;
import sk.mvp.user_service.user.entity.Contact;
import sk.mvp.user_service.user.entity.Gender;
import sk.mvp.user_service.user.entity.User;

import java.time.Instant;

public final class TestDataFactory {

    public static RegistrationReq createRegistrationReq() {
        return new RegistrationReq("janko", "pp", "jank@gmail.com", "M");

    }
    public static User createValidUserInstance() {
        User mockUser;
        Contact mockContact = new Contact("xemaple@dd.com","");
        mockUser = new User("janko", "pp", mockContact, Gender.MALE);
        mockUser.setUsername("username");
        mockUser.setContact(mockContact);
        mockUser.setId(1);
        mockUser.create();
        return mockUser;
    }

//    public static User createMockUser() {
//        mockContact = new Contact(mockReq.getEmail(),"");
//        mockUser = new User(mockReq.getUsername(), mockReq.getPassword(), mockContact, Gender.MALE);
//        mockUser.setUsername(mockReq.getUsername());
//        mockUser.setContact(mockContact);
//        mockUser.setId(1);
//    }

    public static VerificationToken createMockToken(VerificationTokenType tokenType,
                                                    Instant expiresAt) {
        VerificationToken token = new VerificationToken();
        token.setUser(createValidUserInstance());
        token.setUsed(false);
        token.setExpiresAt(expiresAt);
        token.setToken("tokenUUID");
        token.setVerificationTokenType(tokenType);
        return token;
    }
}
