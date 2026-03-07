package com.solutionium.shared.data.model

data class ShippingMethod(
    val id: Int,
    val title: String,
    val methodId: String,
    val description: String,
    val cost: String,
    val settings: Map<String, MethodSetting>? = null,
) {
    fun calculateShippingCost(cartTotal: Double): Double {
        if (cost.isBlank()) return 0.0

        val feeRegex = Regex("""\[fee\s+percent="([^"]+)"\s+min_fee="([^"]+)"\s+max_fee="([^"]+)"]""")
        val feeMatch = feeRegex.find(cost)

        val baseCostString = if (feeMatch != null) cost.substringBefore(feeMatch.value).trim() else cost.trim()
        val cleanedBaseCostString = baseCostString.replace(Regex("""\s*\+\s*$"""), "")
        val baseCost = cleanedBaseCostString.toDoubleOrNull() ?: 0.0

        val percentageFee = try {
            if (feeMatch == null) {
                0.0
            } else {
                val percent = feeMatch.groupValues.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                val minFee = feeMatch.groupValues.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                val maxFee = feeMatch.groupValues.getOrNull(3)?.toDoubleOrNull() ?: 0.0

                var calculatedFee = cartTotal * percent / 100.0
                if (maxFee > 0.0 && calculatedFee > maxFee) calculatedFee = maxFee
                if (calculatedFee < minFee) calculatedFee = minFee
                calculatedFee
            }
        } catch (_: Exception) {
            0.0
        }

        return baseCost + percentageFee
    }

    fun isFreeShippingByCoupon(): Boolean {
        return methodId == "free_shipping" && settings?.get("requires")?.value == "coupon"
    }

    fun isFreeShippingByMinOrder():Boolean {
        return methodId == "free_shipping" && settings?.get("requires")?.value == "min_amount"
    }

    fun isEligibleForMinOrderAmount(subTotal: Double): Boolean {
        return subTotal >= (settings?.get("min_amount")?.value?.toDouble() ?: 0.0)
    }
}


data class MethodSetting (
    val id: String? = null,
    val label: String? = null,
    val description: String? = null,
    val type: String? = null,
    val value: String? = null,
    val default: String? = null,
    val tip: String? = null,
    val placeholder: String? = null,
)
