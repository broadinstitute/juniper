package bio.terra.pearl.core.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
public class Study {
    @Id
    @GeneratedValue
    @Getter
    private UUID id;

    @Getter @Setter
    @NotNull
    private String name;

    @Column(unique = true)
    @Getter @Setter
    @NotNull
    private String shortname;

    @Getter @Setter
    private String camelCase;
}
