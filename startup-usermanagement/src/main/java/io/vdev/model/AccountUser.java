package io.vdev.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
public class AccountUser extends PanacheEntityBase {
    @Id
    @SequenceGenerator(name = "accountUserSeq", sequenceName = "account_user_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "accountUserSeq")
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    @Column(unique = true)
    private String email;
    @Column(updatable = false, name = "created_at")
    private Date createdAt;
    @Column(updatable = true, name = "updated_at")
    private Date updatedAt;
    @Column(name = "account_user_type")
    private AccountUserType accountUserType;

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }
}
