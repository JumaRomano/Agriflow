/**
 * Sealed interface representing user actions and UI events for the Home flow.
 */
package com.agriflow.app.features.homescreen

import com.agriflow.app.features.marketplace.productdetails.Product

/**
 * UI Actions (User Interactions) originating from the Home Dashboard Screen.
 */
sealed interface HomeAction {
    data object StartSourcingClicked : HomeAction
    data object ViewSupplierNetworkClicked : HomeAction
    data object CartClicked : HomeAction
    data object NotificationsClicked : HomeAction
    data object WalletClicked : HomeAction



    data class CategorySelected(val category: String) : HomeAction

    data class ProductClicked(val product: Product) : HomeAction

    data class SearchQueryChanged(val query: String) : HomeAction
    data class DistributorClicked(val businessId: String) : HomeAction
}
