package com.crediya.api;

import com.crediya.api.dto.ApplicationRequest;
import com.crediya.api.dto.ApplicationResponse;
import com.crediya.api.mapper.ApplicationMapper;
import com.crediya.model.application.Application;
import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import com.crediya.usecase.application.ApplicationUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@ContextConfiguration(classes = {RouterRest.class, ApplicationHandlerV1.class, TestSecurityConfig.class})
@WebFluxTest
class RouterRestTest {
    private static final String CUSTOMER_ROLE = "ROLE_CLIENTE";
    private static final String ADMIN_ROLE = "ROLE_ADMINISTRADOR";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ApplicationUseCase applicationUseCase;

    @MockitoBean
    private ApplicationMapper mapper;

    @Test
    void shouldCreateApplication() {
        Application app = new Application();
        app.setIdentificationNumber(123);
        app.setLoanTypeId(1L);

        ApplicationRequest request = new ApplicationRequest(
                123,
                BigDecimal.valueOf(1_000_000),
                12,
                1
        );

        ApplicationResponse response = new ApplicationResponse(
                123,
                "cust@test.com",
                "Diego R",
                BigDecimal.valueOf(5_500_000),
                BigDecimal.valueOf(1_000_000),
                12,
                8.5F,
                "In Revision",
                "Personal"
        );

        Mockito.when(applicationUseCase.newApplication(Mockito.any(),  Mockito.any())).thenReturn(Mono.just(app));

        Mockito.when(mapper.toResponse(Mockito.any()))
                .thenReturn(response);

        webTestClient.mutateWith(
                        mockJwt()
                                .authorities(new SimpleGrantedAuthority(CUSTOMER_ROLE))
                )
                .post()
                .uri("/api/v1/solicitud")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.identificationNumber").isEqualTo(app.getIdentificationNumber())
                .jsonPath("$.loanType").isEqualTo("Personal");
    }

    @Test
    void tesNewApplicationNoIdentificationNumber() {

        ApplicationRequest request = new ApplicationRequest(
                null,
                BigDecimal.valueOf(1_000_000),
                12,
                1
        );

        webTestClient.mutateWith(
                        mockJwt()
                                .authorities(new SimpleGrantedAuthority(CUSTOMER_ROLE))
                )
                .post()
                .uri("/api/v1/solicitud")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Identification number is required"));
    }

    @Test
    void tesNewApplicationNoAmount() {

        ApplicationRequest request = new ApplicationRequest(
               123,
                null,
                12,
                1
        );

        webTestClient.mutateWith(
                mockJwt()
                        .authorities(new SimpleGrantedAuthority(CUSTOMER_ROLE))
                )
                .post()
                .uri("/api/v1/solicitud")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("amount is required"));
    }

    @Test
    void tesNewApplicationNoTerm() {

        ApplicationRequest request = new ApplicationRequest(
                123,
                BigDecimal.valueOf(1_000_000),
                null,
                1
        );

        webTestClient.mutateWith(
                        mockJwt()
                                .authorities(new SimpleGrantedAuthority(CUSTOMER_ROLE))
                )
                .post()
                .uri("/api/v1/solicitud")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("term is required"));
    }

    @Test
    void tesNewApplicationNoLoanType() {

        ApplicationRequest request = new ApplicationRequest(
                123,
                BigDecimal.valueOf(1_000_000),
                12,
                null
        );

        webTestClient.mutateWith(
                        mockJwt()
                                .authorities(new SimpleGrantedAuthority(CUSTOMER_ROLE))
                )
                .post()
                .uri("/api/v1/solicitud")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Loan Type is required"));
    }

    @Test
    void testListApplications() {
        Application app = new Application();
        app.setIdentificationNumber(123);
        app.setLoanTypeId(1L);

        ApplicationResponse response = new ApplicationResponse(
                123,
                "cust@test.com",
                "Digo R",
                BigDecimal.valueOf(5_000_000),
                BigDecimal.valueOf(1_000_000),
                12,
                8.5F,
                "In Revision",
                "Personal"
        );

        // Mock Page<Application>
        Page<Application> page = new Page<>(
                List.of(app), // content
                0,            // page number
                10,           // page size
                1             // total count
        );

        // Mock use case to return a Page wrapped in Mono
        Mockito.when(applicationUseCase.listApplications(Mockito.anyList(), Mockito.any(PageRequest.class)))
                .thenReturn(Mono.just(page));

        // Mock mapper
        Mockito.when(mapper.toResponse(Mockito.any())).thenReturn(response);

        webTestClient.mutateWith(
                        mockJwt()
                                .authorities(new SimpleGrantedAuthority(ADMIN_ROLE))
                )
                .get()
                .uri("/api/v1/solicitud?page=0&size=10&statusIds=1,2,3")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].identificationNumber").isEqualTo(123)
                .jsonPath("$.content[0].loanType").isEqualTo("Personal");
    }

}

