package sk.mvp.user_service.user.dto;

import jakarta.validation.constraints.Size;
import sk.mvp.user_service.user.entity.User;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String username;

    @Size(min = 3, max = 50)
    private String firstName;
    @Size(min = 3, max = 60)
    private String lastName;

    private String genderCode;

    private ContactResp contact;

    public UserProfile() {
    }

    public UserProfile(User user) {
        if (user != null) {
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.genderCode = user.getGender().getCodeAsString();
            this.username = user.getUsername();
            if (user.getContact() != null) {
               this.contact = new ContactResp(user.getContact().getEmail(), user.getContact().getPhoneNumber());
            }
        }
    }


    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGenderCode() {
        return genderCode;
    }

    public ContactResp getContact() {
        return contact;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstName(@Size(min = 3, max = 50) String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(@Size(min = 3, max = 60) String lastName) {
        this.lastName = lastName;
    }

    public void setGenderCode(String genderCode) {
        this.genderCode = genderCode;
    }

    public void setContact(ContactResp contact) {
        this.contact = contact;
    }
}
