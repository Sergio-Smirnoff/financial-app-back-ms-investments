package com.financialapp.investments.web.dto.response;

import java.time.LocalDate;

public record EnsuredSnapshotResponse(boolean created, LocalDate date) {}
