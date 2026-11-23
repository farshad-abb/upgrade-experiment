package com.doodle.experiment.web;

import java.util.regex.Pattern;

/** Visitor ids are UUIDs created by the frontend and kept in a cookie. Anything else is rejected. */
public final class VisitorIds {

    /** Name of the cookie the frontend sets; keep in sync with frontend/src/lib/visitorCookie.ts. */
    public static final String COOKIE = "visitor_id";

    private static final Pattern UUID = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private VisitorIds() {
    }

    public static String require(String visitorId) {
        if (visitorId == null || !UUID.matcher(visitorId).matches()) {
            throw new BadRequestException("visitorId must be a UUID");
        }
        return visitorId.toLowerCase();
    }
}
