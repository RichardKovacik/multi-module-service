package sk.mvp.user_service.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

public class RegistrationReq {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 5, max = 30, message = "Password must be at least 5 characters long")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@(.+)$",
            message = "Email format is invalid"
    )
    private String email;

    @NotBlank(message = "Gender code is required")
    @Pattern(regexp = "^[FM]$", message = "Gender code must be F or M")
    private String genderCode;// F or M
    // TODO: dorobit validaciu vstupu registracie tieto 4 paramtre povinne spring bean validation

    public RegistrationReq(String username, String password, String email, String genderCode) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.genderCode = genderCode;
    }

    public RegistrationReq() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGenderCode() {
        return genderCode;
    }
    @JsonIgnore
    public char getGenderCodeAsCharacter() {
        return Character.toUpperCase(genderCode.charAt(0));
    }
}
