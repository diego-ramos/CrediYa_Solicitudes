package com.crediya.config;

import com.crediya.model.application.gateways.ApplicationRepository;
import com.crediya.model.applicationstatus.gateways.ApplicationStatusRepository;
import com.crediya.model.loantype.gateways.LoanTypeRepository;
import com.crediya.model.sqsmessage.SqsApplicationUpdateMessage;
import com.crediya.model.sqsmessage.SqsCheckDebtCapacityMessage;
import com.crediya.model.sqsmessage.SqsTotalsMessage;
import com.crediya.model.sqsmessage.gateway.SqsMessagePublisher;
import com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class    TestConfig {
        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public ApplicationRepository applicationRepository() {
            return Mockito.mock(ApplicationRepository.class);
        }

        @Bean
        public LoanTypeRepository loanTypeRepository() {
            return Mockito.mock(LoanTypeRepository.class);
        }

        @Bean
        public ApplicationStatusRepository applicationStatusRepository() {
            return Mockito.mock(ApplicationStatusRepository.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        public SqsMessagePublisher<SqsApplicationUpdateMessage> sqsApplicationUpdateMessagePublisher() {
            return Mockito.mock(SqsMessagePublisher.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        public SqsMessagePublisher<SqsCheckDebtCapacityMessage> sqsCheckDebtCapacityMessagePublisher() {
            return Mockito.mock(SqsMessagePublisher.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        public SqsMessagePublisher<SqsTotalsMessage> sqsTotalsMessagePublisher() {
            return Mockito.mock(SqsMessagePublisher.class);
        }
    }

}