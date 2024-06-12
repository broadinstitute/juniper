*** Authorization

This package contains a service (AuthUtilService) with utility methods for authorizing requests.  It also contains annotations
and implementations for annotating methods with authorization requirements.

The expected pattern is:
0. Spring invokes the controller method, and makes the Request available
1. Controller method invokes AuthUtilService.requireAdminUser(request) to confirm the token on the request corresponds to a user in the database
2. Controller method creates an authContext object with the AdminUser object from requireAdminUser call, and any params
3. Controller method invokes an ExtService method, passsing the authContext object as the first argument
4. The ExtService method is Annotatated with @EnforcePortalPermission(permission = "[[permissionName]]")
5. That annotation confirms the user has the given permission for the specified portal (or is a superuser.  If so, the annotation
will populate the authContext object with the Portal, PortalStudy, and/or StudyEnvironment objects retrieved during the authorization process, so
they are available to the service method.
