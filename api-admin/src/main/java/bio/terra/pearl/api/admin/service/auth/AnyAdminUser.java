package bio.terra.pearl.api.admin.service.auth;

import java.lang.annotation.*;

/** lowest level of authorization -- just requires any AdminUser operator */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface AnyAdminUser {}
