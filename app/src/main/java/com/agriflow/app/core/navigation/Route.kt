/**
 * Application navigation routing, graphs, and destination definitions.
 */
package com.agriflow.app.core.navigation

import kotlinx.serialization.Serializable

/**
 * We use sealed interfaces to represent our navigation routes.
 * A sealed interface means all possible implementations must be known at compile time.
 * The @Serializable annotation tells the compiler to automatically convert these objects
 * into a format that the Navigation component can safely pass between screens.
 */
sealed interface Route {

    @Serializable
    data object Splash : Route

    // --- GRAPHS ---
    // Graphs represent a collection of screens.
    @Serializable
    data object AuthGraph : Route

    @Serializable
    data object MainGraph : Route

    // --- AUTH SCREENS ---
    // We use 'data object' for screens that don't take any arguments.
    @Serializable
    data object Login : Route

    @Serializable
    data object Register : Route

    @Serializable
    data object Forget : Route {
    }
        // --- MAIN SCREENS ---
    @Serializable
    data object Home : Route

    @Serializable
    data class Marketplace(val searchFilter: String? = null) : Route

    @Serializable
    data class Orders(val orderId: String? = null) : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object RoleUpgrade : Route

    @Serializable
    data object FarmerUpgrade : Route

    @Serializable
    data object EnterpriseUpgrade : Route

    // Notice this is a 'data class', not a 'data object'.
    // This is the magic of type-safe navigation. We declare exactly what data this screen needs.
    @Serializable
    data class ProductDetails(val productId: String) : Route

    @Serializable
    data class BusinessDetails(val businessId: String) : Route

    @Serializable
    data object Cart : Route

    @Serializable
    data class Payment(val amount: Double) : Route

    @Serializable
    data object Chat : Route
    @Serializable
    data object Notification : Route

    @Serializable
    data object Wallet : Route
    @Serializable
    data object MyStore : Route

    @Serializable
    data object SupplierNetwork : Route
    @Serializable
    data object AddProduct : Route

    @Serializable
    data class EditProduct(val productId: String) : Route

    @Serializable
    data object EditProfile : Route

    @Serializable
    data object PaymentMethods : Route

    @Serializable
    data object HelpCenter : Route
    @Serializable
    data object MyProducts : Route

    @Serializable
    data class OtpVerificationRoute(val email: String, val type: String) : Route

    @Serializable
    data class CreateNewPasswordRoute(val email: String, val resetToken: String) : Route
}