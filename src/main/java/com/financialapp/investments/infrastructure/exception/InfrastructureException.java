package com.financialapp.investments.infrastructure.exception;

/**
 * Marker for infrastructure-adapter failures (Feign, Kafka, IOL HTTP, DB, etc.).
 * Application use cases catch this and translate it into a domain-meaningful
 * {@code DomainException} subtype (e.g. {@code BanksServiceException},
 * {@code IolServiceException}). It is intentionally NOT part of the
 * {@code DomainException} sealed hierarchy.
 */
public class InfrastructureException extends RuntimeException {

    public InfrastructureException(String message) {
        super(message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
