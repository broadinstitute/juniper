package bio.terra.pearl.core.model.study;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZoneId;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentConfig extends BaseEntity {
    @Builder.Default
    private boolean passwordProtected = true;

    // This is a very low-security password, intended as a soft barrier if a study needs to be live but not public (e.g.
    // needs to be publicly accessible for IRB review, or friends-and-family beta, but not exposed to the public.
    // it only keeps the frontend from displaying, and therefore is ok to send in the clear.
    @Builder.Default
    private String password = "broad_institute";

    @Builder.Default
    private boolean acceptingEnrollment = true;

    @Builder.Default
    private boolean acceptingProxyEnrollment = false;

    @Builder.Default
    private boolean enableFamilyLinkage = false;

    @Builder.Default
    private boolean initialized = false;
    /**
     * if true, all kit requests will be directed to the stub DSM instead of the live client
     */
    @Builder.Default
    private boolean useStubDsm = true;
    /**
     * if true, kit requests will all be routed to a centralized development realm in DSM, ensuring that
     * the kit requests are not picked up by GP
     */
    @Builder.Default
    private boolean useDevDsmRealm = true;

    /**
     * if true, the study staff will be able to scan in-person kits into the system,
     * and participants will see information about in-person kits on their dashboard
     */
    @Builder.Default
    private boolean enableInPersonKits = false;

    /**
     * enrollee search expression used to study eligibility.
     * if not specified, allow anyone with a portal account to join
     */
    @Builder.Default
    private String studyEligibilityRule = null;

    /**
     * enrollee search expression used to determine kit eligibility.
     * if not specified, enrollees are eligible if they have completed all required surveys.
     * note: this eligibility rule is not verified by the backend; it is only used as a frontend filter.
     */
    @Builder.Default
    private String kitEligibilityRule = null;

    /**
     * the "home" timezone of the study (e.g. where the study staff is located).  For now, this only impacts export behavior
     * This is a ZoneId stored as a String
     */
    @Builder.Default
    private String timeZone = ZoneId.of("America/New_York").toString();

}
