/**
 * Application navigation routing, graphs, and destination definitions.
 */
package com.agriflow.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.features.profile.roleUpgrade.RoleUpgradeRoute
import com.agriflow.app.features.profile.Farmer.FarmerUpgradeRoute
import com.agriflow.app.features.profile.Enterprise.EnterpriseUpgradeRoute
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import com.agriflow.app.core.ui.SplashRoute
import com.agriflow.app.features.auth.login.LoginRoute
import com.agriflow.app.features.auth.register.RegisterRoute
import com.agriflow.app.features.marketplace.MarketplaceRoute
import com.agriflow.app.homescreen.HomeRoute
import com.agriflow.app.features.products.productdetails.ProductDetailsRoute
import com.agriflow.app.features.suppliernetwork.SupplierNetworkRoute
import com.agriflow.app.features.profile.ProfileRoute
import com.agriflow.app.features.profile.EditProfileRoute
import com.agriflow.app.features.MyStore.sellerdashboard.MyStoreRoute
import com.agriflow.app.features.products.addproduct.AddProductRoute
import com.agriflow.app.features.products.editproduct.EditProductRoute
import com.agriflow.app.features.auth.password.ForgotPasswordRoute
import com.agriflow.app.features.auth.otp.OtpVerificationRoute
import com.agriflow.app.features.auth.password.CreateNewPasswordRoute
import com.agriflow.app.features.products.myproducts.MyProductsRoute
import com.agriflow.app.features.cart.presentation.CartRoute
import com.agriflow.app.features.payment.PaymentRoute
import com.agriflow.app.features.payment.PaymentMethodsRoute
import com.agriflow.app.features.orders.OrdersRoute
import com.agriflow.app.features.wallet.WalletRoute
import com.agriflow.app.features.businessdetails.BusinessDetailsRoute
import com.agriflow.app.features.notifications.NotificationScreen
import com.agriflow.app.features.staff.dashboard.StaffDashboardRoute
import com.agriflow.app.features.staff.auth.StaffChangePasswordRoute





/**
 * Data class representing an item in the bottom navigation bar.
 * Tightly coupled to our type-safe Route interface.
 */
data class BottomNavItem(
    val route: Route,
    val title: String,
    val icon: ImageVector
)

/**
 * AgriflowNavHost is the central navigation component.
 * It wraps our main NavHost inside a Scaffold to manage the bottom navigation bar.
 *
 * @param navController The stateful controller that manages back navigation.
 * @param startDestination The very first screen or graph the app should load.
 */
@Composable
fun AgriflowNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val navigationState by viewModel.navigationState.collectAsState()

    if (navigationState.isLoading) {
        SplashRoute(onSplashFinished = {})
    } else {
        AgriflowNavContent(
            navController = navController,
            startDestination = navigationState.startDestination,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@Composable
private fun AgriflowNavContent(
    navController: NavHostController,
    startDestination: Route,
    viewModel: NavigationViewModel,
    modifier: Modifier = Modifier
) {
    // Observe the current back stack entry to dynamically determine which screen is active
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val userRole by viewModel.userRole.collectAsState()

    val navItems = remember(userRole) {
        buildList {
            add(
                BottomNavItem(
                    route = Route.Home,
                    title = "Home",
                    icon = Icons.Default.Home
                )
            )
            add(
                BottomNavItem(
                    route = Route.Marketplace(),
                    title = "Market",
                    icon = Icons.Default.Storefront
                )
            )
            add(
                BottomNavItem(
                    route = Route.Orders(),
                    title = "Orders",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            )
            if (userRole == UserRole.FARMER || userRole == UserRole.SUPPLIER) {
                // Dynamically add the "My Store" tab for Farmer/Supplier roles
                add(
                    BottomNavItem(
                        route = Route.MyStore,
                        title = "My Store",
                        icon = Icons.Default.Store
                    )
                )
            }
            add(
                BottomNavItem(
                    route = Route.Profile,
                    title = "Account",
                    icon = Icons.Default.Person
                )
            )
        }
    }

    // Show the bottom navigation bar ONLY if the active destination is one of the top-level bottom nav items.
    // This automatically hides it on Splash, Auth screens, and nested detail screens.
    val showBottomBar = currentDestination?.let { dest ->
        navItems.any { item -> dest.hasRoute(item.route::class) }
    } == true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    navItems.forEach { item ->
                        // Check if the current destination has this item's route
                        val isSelected = currentDestination?.hasRoute(item.route::class) == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // as users tap between tabs.
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item (e.g. clicking "Home" repeatedly).
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item.
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(text = item.title)
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // The NavHost links the controller to our specific routes
        // We apply bottom paddingValues to the NavHost so screens aren't drawn underneath the bottom bar,
        // but allow the screens to handle top insets edge-to-edge natively.
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
            composable<Route.Splash> {
                SplashRoute(
                    onSplashFinished = {
                        val startDestination = if (viewModel.isUserLoggedIn()){
                            if (viewModel.isUserStaff()) {
                                Route.StaffGraph
                            } else {
                                Route.MainGraph
                            }
                        }else{
                            Route.AuthGraph
                        }

                        navController.navigate(startDestination) {
                            popUpTo<Route.Splash> {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // --- AUTH GRAPH ---

            navigation<Route.AuthGraph>(startDestination = Route.Login) {
                composable<Route.Login> {
                    LoginRoute (
                        onLoginSuccess = {
                            navController.navigate(Route.MainGraph) {
                                popUpTo<Route.AuthGraph> {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onStaffLoginSuccess = {
                            navController.navigate(Route.StaffGraph) {
                                popUpTo<Route.AuthGraph> {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToChangePassword = { currentPassword ->
                            navController.navigate(Route.StaffChangePassword(currentPassword)) {
                                launchSingleTop = true
                            }
                        },
                        onRegisterClick = {
                            navController.navigate(Route.Register) {
                                launchSingleTop = true
                            }
                        },
                        onForgotClick = {
                            navController.navigate(Route.Forget) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.Register> {
                    RegisterRoute(
                        onNavigateToOtp = { email, type ->
                            navController.navigate(Route.OtpVerificationRoute(email, type)) {
                                launchSingleTop = true
                            }
                        },
                        onRegisterSuccess = {
                            navController.navigate(Route.MainGraph) {
                                popUpTo<Route.AuthGraph> {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onLoginClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Route.Forget> {
                    ForgotPasswordRoute(
                        onNavigateToOtp = { email, type ->
                            navController.navigate(Route.OtpVerificationRoute(email, type)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Route.OtpVerificationRoute> {
                    OtpVerificationRoute(
                        onNavigateToResetPassword = { email, token ->
                            navController.navigate(Route.CreateNewPasswordRoute(email, token)) {
                                popUpTo(Route.OtpVerificationRoute::class) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Route.Login) {
                                popUpTo(Route.AuthGraph) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Route.CreateNewPasswordRoute> {
                    CreateNewPasswordRoute(
                        onResetSuccess = {
                            navController.navigate(Route.Login) {
                                popUpTo(Route.AuthGraph) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Route.StaffChangePassword> {
                    StaffChangePasswordRoute(
                        onPasswordChanged = {
                            navController.navigate(Route.StaffGraph) {
                                popUpTo<Route.AuthGraph> {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }

            }

            // --- STAFF GRAPH ---
            navigation<Route.StaffGraph>(startDestination = Route.StaffDashboard) {
                composable<Route.StaffDashboard> {
                    StaffDashboardRoute(
                        onLogoutSuccess = {
                            navController.navigate(Route.AuthGraph) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // --- MAIN GRAPH ---
            // The main destination graph containing bottom bar screens and related detail flows
            navigation<Route.MainGraph>(startDestination = Route.Home) {
                composable<Route.Home> {
                    HomeRoute(
                        onNavigateToMarketplace = { filter ->
                            navController.navigate(Route.Marketplace(searchFilter = filter)) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToSupplierNetwork = {
                            navController.navigate(Route.SupplierNetwork) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCart = {
                            navController.navigate(Route.Cart) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToNotification = {
                            navController.navigate(Route.Notification) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToWallet =  {
                            navController.navigate(Route.Wallet) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToProductDetails = { productId ->
                            navController.navigate(Route.ProductDetails(productId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToBusinessDetails = { businessId ->
                            navController.navigate(Route.BusinessDetails(businessId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.Marketplace> {
                    MarketplaceRoute(
                        onNavigateToProduct = { productId ->
                            navController.navigate(Route.ProductDetails(productId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCart = {
                            navController.navigate(Route.Cart) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.SupplierNetwork> {
                    SupplierNetworkRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToBusinessDetails = { businessId ->
                            navController.navigate(Route.BusinessDetails(businessId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.MyStore> {
                    MyStoreRoute(
                        onNavigateToMyStore = {
                            navController.navigate(Route.MyStore) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToAddProduct = {
                            navController.navigate(Route.MyProducts) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToOrders = { orderId ->
                            navController.navigate(Route.Orders(orderId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.MyProducts> {
                    MyProductsRoute(
                        onNavigateToAddProduct = { productId ->
                            val dest = if (productId != null) {
                                Route.EditProduct(productId)
                            } else {
                                Route.AddProduct
                            }
                            navController.navigate(dest) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToProductDetails = { productId ->
                            navController.navigate(Route.ProductDetails(productId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Route.AddProduct> {
                    AddProductRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Route.EditProduct> {
                    EditProductRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Route.Orders> {
                    OrdersRoute()
                }

                composable<Route.Profile> {
                    ProfileRoute(
                        onLogoutSuccess = {
                            navController.navigate(Route.AuthGraph) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToUpgrade = { role ->
                            navController.navigate(viewModel.getUpgradeDestination()) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToEditProfile = {
                            navController.navigate(Route.EditProfile) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToPaymentMethods = {
                            navController.navigate(Route.PaymentMethods) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToOrders = {
                            navController.navigate(Route.Orders()) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToWallet = {
                            navController.navigate(Route.Wallet) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.RoleUpgrade> {
                    RoleUpgradeRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToFarmer = {
                            navController.navigate(Route.FarmerUpgrade) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToEnterprise = {
                            navController.navigate(Route.EnterpriseUpgrade) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.FarmerUpgrade> {
                    FarmerUpgradeRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onUpgradeSuccess = {
                            navController.popBackStack(Route.Profile, inclusive = false)
                        }
                    )
                }

                composable<Route.EnterpriseUpgrade> {
                    EnterpriseUpgradeRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onUpgradeSuccess = {
                            navController.popBackStack(Route.Profile, inclusive = false)
                        }
                    )
                }

                composable<Route.Cart> {
                    CartRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCheckout = { total ->
                            navController.navigate(Route.Payment(total)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.Payment> {
                    PaymentRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToOrders = {
                            navController.navigate(Route.Orders()) {
                                popUpTo(Route.Cart) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Route.ProductDetails> {
                    ProductDetailsRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCart = {
                            navController.navigate(Route.Cart) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToBusinessDetails = { businessId ->
                            navController.navigate(Route.BusinessDetails(businessId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.BusinessDetails> {
                    BusinessDetailsRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToProduct = { productId ->
                            navController.navigate(Route.ProductDetails(productId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.PaymentMethods>{
                    PaymentMethodsRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                    )
                }

                composable<Route.EditProfile>{
                    EditProfileRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Route.Notification>{
                    NotificationScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
                composable<Route.Chat>{
                    DummyScreen("Chat")
                }

                composable<Route.HelpCenter>{
                    DummyScreen("Help Center")
                }
                composable<Route.Wallet> {
                    WalletRoute(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }


            }
        }
    }
}

// A temporary placeholder screen so we can run the app and see something
@Composable
private fun DummyScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}
