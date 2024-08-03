package bio.terra.pearl.api.admin.service.auth;

import java.lang.annotation.*;

/**
 * annotation to mark a method as only available in the sandbox environment. The method's first
 * argument must implement the EnvironmentAwareAuthContext interface
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SandboxOnly {}
