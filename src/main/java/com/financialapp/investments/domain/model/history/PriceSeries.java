package com.financialapp.investments.domain.model.history;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record PriceSeries(List<HistoricalPricePoint> points) {

    public PriceSeries {
        Objects.requireNonNull(points, "points must not be null");
        points = dailyCloses(points);
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    private static List<HistoricalPricePoint> dailyCloses(List<HistoricalPricePoint> raw) {
        Map<LocalDate, HistoricalPricePoint> latestPerDay = raw.stream()
                .collect(Collectors.toMap(
                        point -> point.pricedAt().toLocalDate(),
                        point -> point,
                        (a, b) -> a.pricedAt().isAfter(b.pricedAt()) ? a : b));
        return latestPerDay.values().stream()
                .sorted(Comparator.comparing(HistoricalPricePoint::pricedAt))
                .toList();
    }
}
