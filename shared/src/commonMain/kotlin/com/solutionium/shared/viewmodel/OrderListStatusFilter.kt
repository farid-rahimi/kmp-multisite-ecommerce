package com.solutionium.shared.viewmodel

enum class OrderListStatusFilter(val key: String) {
    ALL("all"),
    PROCESSING("processing"),
    AWAITING("awaiting"),
    ON_HOLD("on-hold"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    FAILED("failed"),
    REFUNDED("refunded"),
    PENDING("pending"),
}

