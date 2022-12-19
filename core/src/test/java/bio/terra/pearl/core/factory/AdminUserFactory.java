package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.admin.AdminUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminUserFactory {

    @Autowired
    private AdminUserService adminUserService;

    public AdminUser.AdminUserBuilder builder(String testName) {
        return AdminUser.builder()
                .username(RandomStringUtils.randomAlphabetic(10) + "@admin.test.com");
    }

    public AdminUser buildPersisted(String testName) {
        return adminUserService.create(builder(testName).build());
    }
}
