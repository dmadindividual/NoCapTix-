package topg.Event_Platform.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import topg.Event_Platform.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    private String id;

    private String name;

    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "nin_number", unique = true, nullable = false)
    private String ninNumber;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}
