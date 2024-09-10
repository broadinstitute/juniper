package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class AdminUserDaoTest extends BaseSpringBootTest {

    @Autowired
    private AdminUserDao adminUserDao;

    @Transactional
    @Test
    public void testCreateAdminUser() {
        AdminUser adminUser = AdminUser.builder()
                .username("test")
                .lastLogin(Instant.now())
                .superuser(true)
                .build();

        AdminUser createdUser = adminUserDao.create(adminUser);

        assertThat(createdUser.getId(), notNullValue());
        assertThat(createdUser.getUsername(), equalTo(adminUser.getUsername()));
    }
}
