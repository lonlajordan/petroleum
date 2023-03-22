package com.petroleum.models;

import com.petroleum.enums.Role;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String firstName;
    private String lastName;
    @Column(nullable = false, unique = true)
    private String email = "";
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false, name = "roles")
    private Role role = Role.ROLE_DISPATCHER;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLogin = LocalDateTime.now();
    @Column(name = "actif")
    private boolean enabled = true;

    public User() { }

    public String getName(){
        return firstName + " " + lastName;
    }

    public void normalize(){
        if(this.firstName != null) this.firstName = this.firstName.trim().toUpperCase();
        if(this.lastName != null) this.lastName = StringUtils.capitalize(this.lastName.trim());
    }
}
