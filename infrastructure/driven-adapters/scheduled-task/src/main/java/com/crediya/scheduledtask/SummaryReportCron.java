package com.crediya.scheduledtask;

import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.usecase.application.ApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryReportCron {
    private final ApplicationUseCase applicationUseCase;

    @Scheduled(cron = "${adapter.summarycron.expression}")
    public void sendSummaryReportCron() {
        applicationUseCase.sendSummaryReport()
                .onErrorMap(e -> new TechnicalException(e, TechnicalErrorMessage.ERROR_SENDING_SUMMARY_REPORT))
                .doOnError(e -> log.error("Error sending summary report", e))
                .subscribe();
    }
}
