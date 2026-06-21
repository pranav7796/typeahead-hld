package com.typeahead.scheduler;

import com.typeahead.service.BatchAggregationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Flushes buffered search submissions to PostgreSQL every 30 seconds. */
@Component
public class BatchWriteScheduler {

    private final BatchAggregationService batchAggregationService;

    public BatchWriteScheduler(BatchAggregationService batchAggregationService) {
        this.batchAggregationService = batchAggregationService;
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void flushBufferedCounts() {
        batchAggregationService.flushBufferedCounts();
    }
}
