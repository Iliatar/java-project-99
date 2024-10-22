package hexlet.code.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@ToString(includeFieldNames = true, onlyExplicitlyIncluded = true)
public class Task implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private long id;

    @NotBlank
    private String name;

    private long index;

    private String description;

    @NotNull
    @ManyToOne
    private TaskStatus taskStatus;

    @ManyToOne(optional = true)
    @JsonIgnore
    private User assignee;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private Set<Label> labels = new HashSet<>();

    @CreatedDate
    private LocalDate createdAt;

}
