package com.crediya.api;

public final class Constants {
    public static final String INVALID_REQUEST = "Invalid request.";
    public static final String APPLICATION_REGISTER_SUCCESS = "Application registered successfully: {}";
    public static final String ERROR_REGISTERING_APPLICATION = "Error while registering application";
    public static final String ERROR_GETTING_APPLICATIONS = "Error while getting applications";
    public static final String RETURNING_APPLICATION = "Returning application: {}";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String MUST_PROVIDE_VALID_CREDENTIALS = "You must provide valid credentials";
    public static final String FORBIDDEN = "Forbidden";
    public static final String YOU_DONT_HAVE_PERMISSION_TO_ACCESS = "You do not have permission to access this resource";
    public static final String GRANTED_AUTHORITY = "Granted authority:";
    public static final String AUTHORIZATION_CHECK = "Authorization check: user={}, path={}, method={}, allowed={}";
    public static final String NO_PERMISSIONS_CLAIM = "No permissions claim in token for user={}";
    public static final String RETURNING_APPLICATIONS_PAGE = "Returning application: page={}, size={}, total={}";
    public static final String APPLICATION_STATUS_UPDATE_REQUEST = "Application status update request: {}";
    public static final String ERROR_UPDATING_APPLICATION_STATUS = "Error while updating application status";

    private Constants() {}
}
