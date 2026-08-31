package com.siraj.app.features.cost.domain

import com.siraj.app.domain.models.cost.UsageMetrics
import com.siraj.app.domain.models.cost.WorkspaceLimits

class CostEngine {
    
    fun isLimitExceeded(usage: Double, limit: Double): Boolean {
        return usage >= limit
    }

    fun calculateAlertTriggers(
        currentUsage: Double,
        limit: Double
    ): List<Int> {
        if (limit <= 0) return emptyList()
        val percentage = (currentUsage / limit) * 100
        val triggers = mutableListOf<Int>()
        if (percentage >= 50.0) triggers.add(50)
        if (percentage >= 80.0) triggers.add(80)
        if (percentage >= 100.0) triggers.add(100)
        return triggers
    }

    fun canPerformOperation(
        limits: WorkspaceLimits,
        metrics: UsageMetrics,
        userId: String,
        estimatedCost: Double,
        promptHash: String? = null
    ): Boolean {
        if (isLimitExceeded(metrics.currentDailyUsage + estimatedCost, limits.dailyLimitUsd)) return false
        if (isLimitExceeded(metrics.currentMonthlyUsage + estimatedCost, limits.monthlyLimitUsd)) return false
        
        val userUsage = metrics.userUsageMap[userId] ?: 0.0
        if (isLimitExceeded(userUsage + estimatedCost, limits.perUserLimitUsd)) return false
        
        if (estimatedCost > limits.perOperationLimitUsd) return false
        
        if (promptHash != null) {
            val regens = metrics.regenerationsCountMap[promptHash] ?: 0
            if (regens >= limits.maxRegenerationsPerPrompt) return false
        }
        
        return true
    }
}
