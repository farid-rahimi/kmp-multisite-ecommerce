package com.solutionium.shared.viewmodel


import com.solutionium.shared.data.model.ProductAttribute
import com.solutionium.shared.data.model.ProductVariation
import com.solutionium.shared.data.model.VariationAttribute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Encapsulates the complex business logic for handling WooCommerce product variation selections.
 * This class is designed to be owned by a ViewModel and requires a CoroutineScope.
 *
 * @param scope The CoroutineScope of the owning ViewModel (`viewModelScope`).
 */
class VariationSelectionStateHolder(
    private val scope: CoroutineScope
) {
    // Inputs from the ViewModel
    private val _productAttributes = MutableStateFlow<List<ProductAttribute>>(emptyList())
    private val _productVariations = MutableStateFlow<List<ProductVariation>>(emptyList())
    val variations : StateFlow<List<ProductVariation>> = _productVariations.asStateFlow()

    val variationAttributes: StateFlow<List<ProductAttribute>> = _productAttributes.asStateFlow()

    // Internal State: The currently selected options by the user, e.g., {"Color" to "Red", "Size" to "42"}
    private val _selectedOptions = MutableStateFlow<Map<Int, String>>(emptyMap())
    val selectedOptions: StateFlow<Map<Int, String>> = _selectedOptions.asStateFlow()

    private val _selectedImageUrl = MutableStateFlow<String?>(null)
    val selectedImageUrl: StateFlow<String?> = _selectedImageUrl.asStateFlow()

    /**
     * The final, resolved purchasable variation based on the user's current selections.
     * Emits the matching `ProductVariation` or `null` if the combination is incomplete or invalid.
     */
    val selectedVariation: StateFlow<ProductVariation?> =
        combine(_selectedOptions, _productVariations) { selections, variations ->
            if (selections.isEmpty()) return@combine null

            variations.find { variation ->
                // A variation is a match if ALL of its attributes (e.g., Color and Size)
                // match the user's current selections.
                variation.attributes.all { attr ->
                    selections[attr.id] == attr.option
                }
            }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Populates the state holder with the necessary product data from the API.
     * This should be called by the ViewModel once the data is fetched.
     *
     * It also sets the default selections based on the WooCommerce `default_attributes`.
     */
    fun setProductData(
        attributes: List<ProductAttribute>,
        variations: List<ProductVariation>,
        defaultAttributes: List<VariationAttribute>
    ) {
        _productAttributes.value = attributes
        _productVariations.value = variations

        // Pre-select default attributes if they are valid
        val defaultSelections = defaultAttributes
            .filter { default -> isOptionAvailable(default.id, default.option) }
            .associate { it.id to it.option }

        if (defaultSelections.isNotEmpty()) {
            _selectedOptions.value = defaultSelections
        }
    }

    /**
     * Called by the ViewModel when the user selects a new variation option (e.g., clicks on "Red").
     */
    fun onOptionSelected(attributeId: Int, optionValue: String) {
        val currentSelections = _selectedOptions.value.toMutableMap()
        currentSelections[attributeId] = optionValue

        _productVariations.value.find {
            it.attributes.any { attr -> attr.slug == "pa_color" && attr.id == attributeId && attr.option == optionValue}
        } ?.image?.let { imageUrl ->
            _selectedImageUrl.value = imageUrl
        }

        // --- Advanced Logic: Validate selections ---
        // After selecting a color, we must check if the currently selected size is still valid.
        // If not, we remove the invalid size selection to force the user to re-select.
        val invalidSelections = currentSelections.filterNot { (key, value) ->
            isOptionStillValid(key, value, currentSelections)
        }
        invalidSelections.keys.forEach { key ->
            currentSelections.remove(key)
        }

        _selectedOptions.value = currentSelections
    }

    /**
     * Checks if a specific option is available for purchase in *any* combination.
     * Used to gray out options that are completely out of stock.
     */
    fun isOptionAvailable(attributeId: Int, optionValue: String): Boolean {
        return _productVariations.value.any { variation ->
            variation.stockStatus == "instock" && variation.attributes.any { attr ->
                attr.id == attributeId && attr.option == optionValue
            }
        }
    }

    /**
     * A more advanced check. Determines if an option (`optionValue`) is valid given the *other*
     * currently selected options. For example, if "Red" is selected, is "Size 45" available in Red?
     */
    private fun isOptionStillValid(
        attributeId: Int,
        optionValue: String,
        currentSelections: Map<Int, String>
    ): Boolean {
        return _productVariations.value.any { variation ->
            // The option must exist in a variation that is in stock.
            variation.stockStatus == "instock" &&
                    // And that variation's attributes must be a superset of the user's current selections.
                    currentSelections.all { (selectedAttr, selectedOption) ->
                        // If we are checking the attribute itself, we use the new optionValue.
                        // Otherwise, we use the already selected option.
                        val valueToCheck =
                            if (selectedAttr == attributeId) optionValue else selectedOption
                        variation.attributes.any { it.id == selectedAttr && it.option == valueToCheck }
                    }
        }
    }
}
