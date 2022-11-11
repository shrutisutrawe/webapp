package com.neu.assignment.metrics;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.stereotype.Repository;

@Repository
public class WebappApplicationMetrics {
    private static final StatsDClient statsd = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    public void addCount(String metricName) {
        statsd.incrementCounter(metricName);
    }

}
