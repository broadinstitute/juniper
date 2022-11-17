package bio.terra.pearl.core.model;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class StudyEnvironment {
    @Id
    @GeneratedValue
    @Getter
    private UUID id;
}
