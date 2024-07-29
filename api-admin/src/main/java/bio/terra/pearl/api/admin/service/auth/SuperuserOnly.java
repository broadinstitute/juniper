package bio.terra.pearl.api.admin.service.auth;

import java.lang.annotation.*;

/**
 * annotation to mark a method as only accessible by a Juniper superuser. The method's first
 * argument must implement the OperatorAuthContext interface
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SuperuserOnly {}
