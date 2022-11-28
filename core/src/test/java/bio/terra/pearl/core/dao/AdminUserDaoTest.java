package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.AdminUser;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AdminUserDaoTest extends BaseSpringBootTest {

    @Autowired
    private AdminUserDao adminUserDao;

    @Transactional
    @Test
    public void testCreateAdminUser() {
        AdminUser adminUser = AdminUser.builder()
                .username("test")
                .token("secret")
                .lastLogin(Instant.now())
                .superuser(true)
                .build();

        AdminUser createdUser = adminUserDao.create(adminUser);

        assertNotNull(createdUser.getId());
//        assertThat(createdUser.getUsername(), equalTo(adminUser.getUsername()));
        assertEquals(adminUser.getUsername(), createdUser.getUsername());
    }
}
