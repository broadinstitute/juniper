package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.DaoTestUtils;
import bio.terra.pearl.core.model.participant.Profile;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProfileDaoTests extends BaseSpringBootTest {
    @Autowired
    ProfileDao profileDao;
    @Test
    public void testLocalDateSaves() {
       Profile profile = Profile.builder()
               .familyName("smith")
               .birthDate(LocalDate.of(1987,4,7)).build();
       Profile savedProfile = profileDao.create(profile);
       DaoTestUtils.assertGeneratedProperties(savedProfile);
       assertThat(savedProfile, samePropertyValuesAs(profile, "id", "createdAt", "lastUpdated"));
    }
}
