package com.agriflow.app.features.suppliernetwork

import app.cash.turbine.test
import com.agriflow.app.MainDispatcherRule
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.marketplace.MarketplaceRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SupplierNetworkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val marketplaceRepository: MarketplaceRepository = mockk()
    private val suppliersFlow = MutableStateFlow<List<SupplierEntity>>(emptyList())
    private lateinit var viewModel: SupplierNetworkViewModel

    @Before
    fun setUp() {
        every { marketplaceRepository.observeSuppliers() } returns suppliersFlow
        coEvery { marketplaceRepository.getVerifiedBusinesses() } returns Result.Success(emptyList())
    }

    @Test
    fun `test initial state`() = runTest {
        viewModel = SupplierNetworkViewModel(marketplaceRepository)
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals("", initialState.searchQuery)
            assertEquals("All", initialState.selectedFilter)
            assertEquals(emptyList<SupplierNetworkItem>(), initialState.suppliers)
            assertFalse(initialState.isLoading)
        }
    }

    @Test
    fun `test mapping and filtering of supplier and farm types`() = runTest {
        val dummyEntities = listOf(
            SupplierEntity(
                supplierId = "1",
                name = "Farm 1",
                farmLocation = "Location 1",
                rating = 4.5,
                contactInfo = "",
                type = "FARM",
                logoUrl = null,
                reviewCount = 10,
                isVerified = true
            ),
            SupplierEntity(
                supplierId = "2",
                name = "Farmer 2",
                farmLocation = "Location 2",
                rating = 4.0,
                contactInfo = "",
                type = "FARMER",
                logoUrl = null,
                reviewCount = 5,
                isVerified = true
            ),
            SupplierEntity(
                supplierId = "3",
                name = "Supplier 3",
                farmLocation = "Location 3",
                rating = 4.8,
                contactInfo = "",
                type = "ENTERPRISE",
                logoUrl = null,
                reviewCount = 20,
                isVerified = true
            ),
            SupplierEntity(
                supplierId = "4",
                name = "Supplier 4",
                farmLocation = "Location 4",
                rating = 3.9,
                contactInfo = "",
                type = "SUPPLIER",
                logoUrl = null,
                reviewCount = 2,
                isVerified = true
            )
        )

        suppliersFlow.value = dummyEntities
        viewModel = SupplierNetworkViewModel(marketplaceRepository)

        viewModel.state.test {
            // Initial state check - filter All
            val stateAll = awaitItem()
            assertEquals(4, stateAll.suppliers.size)
            assertEquals(SupplierType.FARMER, stateAll.suppliers[0].type)
            assertEquals("🌾", stateAll.suppliers[0].emoji)
            assertEquals(SupplierType.FARMER, stateAll.suppliers[1].type)
            assertEquals("🌾", stateAll.suppliers[1].emoji)
            assertEquals(SupplierType.SUPPLIER, stateAll.suppliers[2].type)
            assertEquals("🏢", stateAll.suppliers[2].emoji)
            assertEquals(SupplierType.SUPPLIER, stateAll.suppliers[3].type)
            assertEquals("🏢", stateAll.suppliers[3].emoji)

            // Select Farms filter
            viewModel.onFilterSelected("Farms")
            var stateFarms = awaitItem()
            while (stateFarms.selectedFilter != "Farms" || stateFarms.suppliers.size != 2) {
                stateFarms = awaitItem()
            }
            assertEquals(2, stateFarms.suppliers.size)
            assertEquals("Farm 1", stateFarms.suppliers[0].name)
            assertEquals("Farmer 2", stateFarms.suppliers[1].name)

            // Select Suppliers filter
            viewModel.onFilterSelected("Suppliers")
            var stateSuppliers = awaitItem()
            while (stateSuppliers.selectedFilter != "Suppliers" || stateSuppliers.suppliers.size != 2) {
                stateSuppliers = awaitItem()
            }
            assertEquals(2, stateSuppliers.suppliers.size)
            assertEquals("Supplier 3", stateSuppliers.suppliers[0].name)
            assertEquals("Supplier 4", stateSuppliers.suppliers[1].name)

            // Search query filtering
            viewModel.onSearchQueryChanged("Farm 1")
            var stateSearch = awaitItem()
            while (stateSearch.searchQuery != "Farm 1" || stateSearch.suppliers.isNotEmpty()) {
                stateSearch = awaitItem()
            }
            assertEquals(0, stateSearch.suppliers.size)
            
            // Go back to "All" filter to check search query mapping
            viewModel.onFilterSelected("All")
            var stateAllSearch = awaitItem()
            while (stateAllSearch.selectedFilter != "All" || stateAllSearch.suppliers.size != 1) {
                stateAllSearch = awaitItem()
            }
            assertEquals(1, stateAllSearch.suppliers.size)
            assertEquals("Farm 1", stateAllSearch.suppliers[0].name)
        }
    }
}
