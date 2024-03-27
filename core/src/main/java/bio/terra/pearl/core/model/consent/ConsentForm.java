package bio.terra.pearl.core.model.consent;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.PortalAttached;
import bio.terra.pearl.core.model.Versioned;
import java.util.UUID;

import bio.terra.pearl.core.model.form.VersionedForm;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** See https://broadworkbench.atlassian.net/wiki/spaces/PEARL/pages/2669281289/Consent+forms */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ConsentForm extends VersionedForm { }
