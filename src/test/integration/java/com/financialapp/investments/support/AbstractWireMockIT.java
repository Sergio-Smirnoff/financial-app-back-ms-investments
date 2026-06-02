package com.financialapp.investments.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Base for integration tests that exercise the outbound HTTP boundary (IOL market data
 * and the ms-banks Feign client) against a real WireMock server — never {@code @MockBean}.
 *
 * <p>The server is a single JVM-wide singleton on one dynamically-allocated port, started once
 * and never restarted. This matters because the Spring context (with its downstream base-URL
 * properties) is cached and shared across every {@code AbstractWireMockIT} subclass: a per-class
 * extension with {@code dynamicPort()} would restart on a new port between classes and leave the
 * cached context pointing at a dead port. Happy-path stubs live under
 * {@code src/test/resources/wiremock/{mappings,__files}}; error/edge paths are added per test.
 */
public abstract class AbstractWireMockIT extends AbstractIntegrationIT {

    protected static final WireMockServer wireMock;

    static {
        wireMock = new WireMockServer(options().dynamicPort().usingFilesUnderClasspath("wiremock"));
        wireMock.start();
    }

    @DynamicPropertySource
    static void downstreamUrls(DynamicPropertyRegistry registry) {
        registry.add("iol.base-url", wireMock::baseUrl);
        registry.add("banks.service.url", wireMock::baseUrl);
    }

    /** Clear only the request journal between tests; file-based stub mappings stay in place. */
    @BeforeEach
    void resetWireMockRequests() {
        wireMock.resetRequests();
    }
}
