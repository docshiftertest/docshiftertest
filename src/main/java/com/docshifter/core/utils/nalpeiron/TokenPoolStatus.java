package com.docshifter.core.utils.nalpeiron;

/**
 * TokenPoolStatus is an Enum representing the pool status codes of Consumption-Tokens.
 * Each status code represents a distinct state in the token processing flow.
 * It also encapsulates the error code and its associated message.
 *
 * @author Juan Marques
 * @created 13/07/2023
 */
public enum TokenPoolStatus {

    REQUEST_AUTHORIZED(1, "Request Authorized"),
    UNSET(0, "Unset"),
    ERROR(-1, "Error"),
    UNKNOWN_CONSUMPTION_TOKEN(-2, "Unknown consumption token"),
    REQUEST_DENIED(-3, "Request Denied"),
    RESOURCE_NOT_AUTHORIZED_FOR_USE(-4, "Resource Not Authorized for Use"),
    LICENSE_EXPIRED(-5, "License Expired"),
    /**
     * The license being checked is not a consumption-based license.
     */
    NOT_CONSUMPTION_BASED_LICENSE(-1096, "Not a Consumption based license"),
    /**
     * The server denied the token transaction request, typically because there are insufficient tokens on the server to check out.
     */
    SERVER_DENIED_TRANSACTION(-1136, "Server denied transaction - Insufficient tokens to checkout");

    private final int code;
    private final String message;

    /**
     * Constructs a new TokenPoolStatus with the given error code and message.
     *
     * @param code    the error code
     * @param message the error message
     */
    TokenPoolStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Returns the error code of this status.
     *
     * @return the error code
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Returns the error message of this status.
     *
     * @return the error message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns the TokenPoolStatus for the given error code.
     *
     * @param errorCode the error code
     *
     * @return the associated TokenPoolStatus
     *
     * @throws IllegalArgumentException if the error code does not match any TokenPoolStatus
     */
    public static TokenPoolStatus fromErrorCode(int errorCode) {
        for (TokenPoolStatus status : values()) {
            if (status.getCode() == errorCode) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid error code: " + errorCode);
    }
}