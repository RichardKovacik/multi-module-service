package sk.mvp.user_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "contact")
public class Contact {
    @Id
    private int id;

    @Column(unique=true, nullable=false, length=40)
    private String email;
    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public Contact() {
    }

    public Contact(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }


    public Contact(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getId() {
        return id;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
