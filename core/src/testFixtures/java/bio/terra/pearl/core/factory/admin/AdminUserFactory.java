package bio.terra.pearl.core.factory.admin;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
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
                .username(testName + "_" + RandomStringUtils.randomAlphabetic(5) + "@test.com");
    }

    public AdminUser buildPersisted(String testName) {
        return buildPersisted(testName, false);
    }

    public AdminUser buildPersisted(String testName, boolean superuser) {
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess("AdminUserFactory.buildPersisted").build();
        return adminUserService.create(builder(testName).superuser(superuser).build(), auditInfo);
    }

    public AdminUser buildPersisted(AdminUser.AdminUserBuilder builder) {
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess("AdminUserFactory.buildPersisted").build();
        return adminUserService.create(builder.build(), auditInfo);
    }

}
