package com.agriflow.app.features.auth

import app.cash.turbine.test
import com.agriflow.app.MainDispatcherRule
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.otp.OtpType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val staffAuthRepository: com.agriflow.app.features.staff.auth.StaffAuthRepository = mockk()
    private lateinit var viewModel: AuthViewModel

    private val dummyUser = User(
        id = "user123",
        username = "agriflow_user",
        email = "test@example.com",
        phoneNumber = "1234567890",
        role = UserRole.BUYER,
        firstName = "John",
        surName = "Doe"
    )

    private val dummyTokens = AuthTokens(
        accessToken = "accessToken",
        refreshToken = "refreshToken"
    )

    private val dummySession = AuthSession(
        user = dummyUser,
        tokens = dummyTokens
    )

    @Before
    fun setUp() {
        viewModel = AuthViewModel(authRepository, staffAuthRepository)
    }

    // --- Login Validation Tests ---

    @Test
    fun `login validation - when email is empty, show error`() = runTest {
        viewModel.onAction(AuthAction.LoginEmailChanged(""))
        viewModel.onAction(AuthAction.LoginPasswordChanged("password123"))

        viewModel.events.test {
            viewModel.onAction(AuthAction.LoginSubmitted)
            assertEquals("Email or Username is required", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("Email or Username is required"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `staff login - when mustChangePassword is true, emit NavigateToChangePassword`() = runTest {
        val staffSession = dummySession.copy(mustChangePassword = true)
        coEvery { staffAuthRepository.login("agent01", "password123") } returns Result.Success(staffSession)

        viewModel.onAction(AuthAction.LoginEmailChanged("agent01"))
        viewModel.onAction(AuthAction.LoginPasswordChanged("password123"))

        viewModel.events.test {
            viewModel.onAction(AuthAction.LoginSubmitted)
            assertEquals(AuthEvent.NavigateToChangePassword("password123"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login validation - when password is empty, show error`() = runTest {
        viewModel.onAction(AuthAction.LoginEmailChanged("test@example.com"))
        viewModel.onAction(AuthAction.LoginPasswordChanged(""))

        viewModel.events.test {
            viewModel.onAction(AuthAction.LoginSubmitted)
            assertEquals("Password is required", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("Password is required"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Registration Validation Tests ---

    @Test
    fun `register validation - when username is empty, show error`() = runTest {
        viewModel.onAction(AuthAction.RegisterusernameChanged(""))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged("John"))
        viewModel.onAction(AuthAction.RegistersurNameChanged("Doe"))
        viewModel.onAction(AuthAction.RegisterEmailChanged("test@example.com"))
        viewModel.onAction(AuthAction.RegisterPasswordChanged("password123"))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.onAction(AuthAction.RegisterSubmitted)
            assertEquals("Username is required", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("Username is required"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register validation - when firstName is empty, show error`() = runTest {
        viewModel.onAction(AuthAction.RegisterusernameChanged("user123"))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged(""))
        viewModel.onAction(AuthAction.RegistersurNameChanged("Doe"))
        viewModel.onAction(AuthAction.RegisterEmailChanged("test@example.com"))
        viewModel.onAction(AuthAction.RegisterPasswordChanged("password123"))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.onAction(AuthAction.RegisterSubmitted)
            assertEquals("First name is required", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("First name is required"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register validation - when surName is empty, show error`() = runTest {
        viewModel.onAction(AuthAction.RegisterusernameChanged("user123"))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged("John"))
        viewModel.onAction(AuthAction.RegistersurNameChanged(""))
        viewModel.onAction(AuthAction.RegisterEmailChanged("test@example.com"))
        viewModel.onAction(AuthAction.RegisterPasswordChanged("password123"))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.onAction(AuthAction.RegisterSubmitted)
            assertEquals("Surname is required", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("Surname is required"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register validation - when email is empty, show error`() = runTest {
        viewModel.onAction(AuthAction.RegisterusernameChanged("user123"))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged("John"))
        viewModel.onAction(AuthAction.RegistersurNameChanged("Doe"))
        viewModel.onAction(AuthAction.RegisterEmailChanged(""))
        viewModel.onAction(AuthAction.RegisterPasswordChanged("password123"))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.onAction(AuthAction.RegisterSubmitted)
            assertEquals("Email is required", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("Email is required"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register validation - when email is invalid, show error`() = runTest {
        viewModel.onAction(AuthAction.RegisterusernameChanged("user123"))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged("John"))
        viewModel.onAction(AuthAction.RegistersurNameChanged("Doe"))
        viewModel.onAction(AuthAction.RegisterEmailChanged("invalidemail"))
        viewModel.onAction(AuthAction.RegisterPasswordChanged("password123"))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.onAction(AuthAction.RegisterSubmitted)
            assertEquals("Enter a valid email address", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("Enter a valid email address"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register validation - when password is too short, show error`() = runTest {
        viewModel.onAction(AuthAction.RegisterusernameChanged("user123"))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged("John"))
        viewModel.onAction(AuthAction.RegistersurNameChanged("Doe"))
        viewModel.onAction(AuthAction.RegisterEmailChanged("test@example.com"))
        viewModel.onAction(AuthAction.RegisterPasswordChanged("short"))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.onAction(AuthAction.RegisterSubmitted)
            assertEquals("Password must be at least 8 characters", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("Password must be at least 8 characters"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register validation - when terms are not accepted, show error`() = runTest {
        viewModel.onAction(AuthAction.RegisterusernameChanged("user123"))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged("John"))
        viewModel.onAction(AuthAction.RegistersurNameChanged("Doe"))
        viewModel.onAction(AuthAction.RegisterEmailChanged("test@example.com"))
        viewModel.onAction(AuthAction.RegisterPasswordChanged("password123"))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(false))

        viewModel.events.test {
            viewModel.onAction(AuthAction.RegisterSubmitted)
            assertEquals("Accept the terms to create an account", viewModel.state.value.errorMessage)
            assertEquals(AuthEvent.ShowMessage("Accept the terms to create an account"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Login API Integration Tests ---

    @Test
    fun `login success - updates loading and triggers NavigateToMain`() = runTest {
        val email = "test@example.com"
        val password = "password123"

        coEvery { authRepository.login(email, password) } coAnswers {
            kotlinx.coroutines.delay(10)
            Result.Success(dummySession)
        }

        viewModel.onAction(AuthAction.LoginEmailChanged(email))
        viewModel.onAction(AuthAction.LoginPasswordChanged(password))

        viewModel.events.test {
            viewModel.state.test {
                val initialState = awaitItem()
                assertNull(initialState.errorMessage)
                assertEquals(false, initialState.isLoading)
                
                viewModel.onAction(AuthAction.LoginSubmitted)
                
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                assertNull(loadingState.errorMessage)

                val successState = awaitItem()
                assertEquals(false, successState.isLoading)
                assertNull(successState.errorMessage)
            }

            assertEquals(AuthEvent.NavigateToMain, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login error - updates loading, sets error message, and triggers ShowMessage`() = runTest {
        val email = "test@example.com"
        val password = "password123"

        coEvery { authRepository.login(email, password) } coAnswers {
            kotlinx.coroutines.delay(10)
            Result.Error(DataError.Network.UNAUTHORIZED)
        }

        viewModel.onAction(AuthAction.LoginEmailChanged(email))
        viewModel.onAction(AuthAction.LoginPasswordChanged(password))

        viewModel.events.test {
            viewModel.state.test {
                val initialState = awaitItem()
                assertNull(initialState.errorMessage)
                assertEquals(false, initialState.isLoading)

                viewModel.onAction(AuthAction.LoginSubmitted)

                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                val errorState = awaitItem()
                assertEquals(false, errorState.isLoading)
                assertEquals("Incorrect email or password.", errorState.errorMessage)
            }

            assertEquals(AuthEvent.ShowMessage("Incorrect email or password."), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Register API Integration Tests ---

    @Test
    fun `register and sendOtp success - updates loading and triggers NavigateToOtp`() = runTest {
        val username = "user123"
        val firstName = "John"
        val surName = "Doe"
        val email = "test@example.com"
        val password = "password123"
        val phoneNumber = "1234567890"

        coEvery {
            authRepository.register(username, email, phoneNumber, password, firstName, surName)
        } coAnswers {
            kotlinx.coroutines.delay(10)
            Result.Success(dummySession)
        }

        coEvery {
            authRepository.sendOtp(email, OtpType.REGISTRATION)
        } coAnswers {
            kotlinx.coroutines.delay(10)
            Result.Success(Unit)
        }

        viewModel.onAction(AuthAction.RegisterusernameChanged(username))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged(firstName))
        viewModel.onAction(AuthAction.RegistersurNameChanged(surName))
        viewModel.onAction(AuthAction.RegisterEmailChanged(email))
        viewModel.onAction(AuthAction.RegisterPhoneNumberChanged(phoneNumber))
        viewModel.onAction(AuthAction.RegisterPasswordChanged(password))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.state.test {
                val initialState = awaitItem()
                assertNull(initialState.errorMessage)
                assertEquals(false, initialState.isLoading)

                viewModel.onAction(AuthAction.RegisterSubmitted)

                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                val successState = awaitItem()
                assertEquals(false, successState.isLoading)
                assertNull(successState.errorMessage)
            }

            assertEquals(AuthEvent.NavigateToOtp(email, OtpType.REGISTRATION.name), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register success but sendOtp error - updates loading, sets error message, and triggers ShowMessage`() = runTest {
        val username = "user123"
        val firstName = "John"
        val surName = "Doe"
        val email = "test@example.com"
        val password = "password123"
        val phoneNumber = "1234567890"

        coEvery {
            authRepository.register(username, email, phoneNumber, password, firstName, surName)
        } coAnswers {
            kotlinx.coroutines.delay(10)
            Result.Success(dummySession)
        }

        coEvery {
            authRepository.sendOtp(email, OtpType.REGISTRATION)
        } coAnswers {
            kotlinx.coroutines.delay(10)
            Result.Error(DataError.Network.SERVER_ERROR)
        }

        viewModel.onAction(AuthAction.RegisterusernameChanged(username))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged(firstName))
        viewModel.onAction(AuthAction.RegistersurNameChanged(surName))
        viewModel.onAction(AuthAction.RegisterEmailChanged(email))
        viewModel.onAction(AuthAction.RegisterPhoneNumberChanged(phoneNumber))
        viewModel.onAction(AuthAction.RegisterPasswordChanged(password))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.state.test {
                val initialState = awaitItem()
                assertNull(initialState.errorMessage)
                assertEquals(false, initialState.isLoading)

                viewModel.onAction(AuthAction.RegisterSubmitted)

                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                val errorState = awaitItem()
                assertEquals(false, errorState.isLoading)
                assertEquals("The server is unavailable. Try again later.", errorState.errorMessage)
            }

            assertEquals(AuthEvent.ShowMessage("The server is unavailable. Try again later."), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register error - updates loading, sets error message, and triggers ShowMessage`() = runTest {
        val username = "user123"
        val firstName = "John"
        val surName = "Doe"
        val email = "test@example.com"
        val password = "password123"
        val phoneNumber = "1234567890"

        coEvery {
            authRepository.register(username, email, phoneNumber, password, firstName, surName)
        } coAnswers {
            kotlinx.coroutines.delay(10)
            Result.Error(DataError.Network.CONFLICT)
        }

        viewModel.onAction(AuthAction.RegisterusernameChanged(username))
        viewModel.onAction(AuthAction.RegisterfirstNameChanged(firstName))
        viewModel.onAction(AuthAction.RegistersurNameChanged(surName))
        viewModel.onAction(AuthAction.RegisterEmailChanged(email))
        viewModel.onAction(AuthAction.RegisterPhoneNumberChanged(phoneNumber))
        viewModel.onAction(AuthAction.RegisterPasswordChanged(password))
        viewModel.onAction(AuthAction.RegisterTermsAcceptedChanged(true))

        viewModel.events.test {
            viewModel.state.test {
                val initialState = awaitItem()
                assertNull(initialState.errorMessage)
                assertEquals(false, initialState.isLoading)

                viewModel.onAction(AuthAction.RegisterSubmitted)

                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                val errorState = awaitItem()
                assertEquals(false, errorState.isLoading)
                assertEquals("An account with these details already exists.", errorState.errorMessage)
            }

            assertEquals(AuthEvent.ShowMessage("An account with these details already exists."), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `input changes - when error is showing, typing email or password clears the error`() = runTest {
        // 1. Trigger login validation error
        viewModel.onAction(AuthAction.LoginEmailChanged(""))
        viewModel.onAction(AuthAction.LoginPasswordChanged("password123"))
        viewModel.onAction(AuthAction.LoginSubmitted)
        assertEquals("Email or Username is required", viewModel.state.value.errorMessage)

        // 2. Clear login error by changing email
        viewModel.onAction(AuthAction.LoginEmailChanged("user@example.com"))
        assertNull(viewModel.state.value.errorMessage)
        assertEquals("user@example.com", viewModel.state.value.loginEmail)

        // 3. Trigger register validation error
        viewModel.onAction(AuthAction.RegisterusernameChanged(""))
        viewModel.onAction(AuthAction.RegisterSubmitted)
        assertEquals("Username is required", viewModel.state.value.errorMessage)

        // 4. Clear register error by changing username
        viewModel.onAction(AuthAction.RegisterusernameChanged("agriflow_new_username"))
        assertNull(viewModel.state.value.errorMessage)
        assertEquals("agriflow_new_username", viewModel.state.value.registerusername)
    }
}

