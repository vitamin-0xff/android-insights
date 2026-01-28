# Jetpack Compose Android Guidelines

Extends ROOT_GUIDELINES.md. Covers Jetpack Compose-specific patterns and best practices for Android development.

---

## 1. Project Structure

### 1.1 Multi-Module Architecture (Recommended)

```
project-name/
├── app/                        - Main application module
│   ├── src/
│   │   └── main/
│   │       ├── java/com/company/app/
│   │       │   ├── MainActivity.kt
│   │       │   ├── MyApplication.kt
│   │       │   └── di/               - Dependency injection
│   │       │       ├── AppModule.kt
│   │       │       └── NetworkModule.kt
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── core/                       - Core business logic
│   ├── domain/                - Domain models and use cases
│   │   └── src/main/java/com/company/domain/
│   │       ├── model/
│   │       │   └── User.kt
│   │       ├── repository/
│   │       │   └── UserRepository.kt
│   │       └── usecase/
│   │           ├── GetUsersUseCase.kt
│   │           └── CreateUserUseCase.kt
│   └── data/                  - Data layer
│       └── src/main/java/com/company/data/
│           ├── remote/         - API layer
│           │   ├── api/
│           │   │   └── UserApi.kt
│           │   ├── dto/       - API DTOs
│           │   │   ├── UserDto.kt
│           │   │   └── UserRequest.kt
│           │   └── mapper/
│           │       └── UserMapper.kt
│           ├── local/          - Database
│           │   ├── dao/
│           │   │   └── UserDao.kt
│           │   └── entity/
│           │       └── UserEntity.kt
│           └── repository/
│               └── UserRepositoryImpl.kt
├── feature/                    - Feature modules
│   ├── user/
│   │   └── src/main/java/com/company/user/
│   │       ├── ui/
│   │       │   ├── list/
│   │       │   │   ├── UserListScreen.kt
│   │       │   │   ├── UserListViewModel.kt
│   │       │   │   └── UserListState.kt
│   │       │   ├── detail/
│   │       │   │   ├── UserDetailScreen.kt
│   │       │   │   └── UserDetailViewModel.kt
│   │       │   └── components/
│   │       │       ├── UserCard.kt
│   │       │       └── UserForm.kt
│   │       └── navigation/
│   │           └── UserNavigation.kt
│   ├── auth/
│   └── dashboard/
├── ui/                         - Shared UI components
│   └── src/main/java/com/company/ui/
│       ├── components/
│       │   ├── Button.kt
│       │   ├── TextField.kt
│       │   └── LoadingIndicator.kt
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Theme.kt
│       │   └── Type.kt
│       └── util/
│           └── UiState.kt
└── build.gradle.kts
```

### 1.2 Single Module Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/company/app/
│       │   ├── data/
│       │   │   ├── remote/
│       │   │   │   ├── api/
│       │   │   │   ├── dto/
│       │   │   │   └── mapper/
│       │   │   └── repository/
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   ├── repository/
│       │   │   └── usecase/
│       │   ├── ui/
│       │   │   ├── screens/
│       │   │   │   ├── user/
│       │   │   │   ├── auth/
│       │   │   │   └── home/
│       │   │   ├── components/
│       │   │   ├── navigation/
│       │   │   └── theme/
│       │   ├── di/
│       │   ├── util/
│       │   ├── MainActivity.kt
│       │   └── MyApplication.kt
│       ├── res/
│       └── AndroidManifest.xml
└── build.gradle.kts
```

---

## 2. Naming Conventions

### 2.1 Files and Classes

- Screens: `*Screen.kt` (`UserListScreen.kt`, `LoginScreen.kt`)
- ViewModels: `*ViewModel.kt` (`UserListViewModel.kt`)
- State: `*State.kt` or `*UiState.kt`
- Composables: PascalCase (`UserCard.kt`, `CustomButton.kt`)
- Repositories: `*Repository.kt` and `*RepositoryImpl.kt`
- Use Cases: `*UseCase.kt` (`GetUsersUseCase.kt`)
- DTOs: `*Dto.kt` or `*Request.kt`/`*Response.kt`
- Mappers: `*Mapper.kt`
- APIs: `*Api.kt` (`UserApi.kt`)

### 2.2 Functions and Variables

- Composables: PascalCase (`UserCard`, `UserListScreen`)
- Functions: camelCase (`fetchUsers`, `validateForm`)
- Variables: camelCase (`userName`, `isLoading`)
- Constants: UPPER_SNAKE_CASE (`API_BASE_URL`, `MAX_RETRY_COUNT`)
- State variables: `uiState`, `viewState`
- Event handlers: `onUserClick`, `onSubmit`
- Lambda parameters: `onClick`, `onValueChange`

---

## 3. Composable Functions

### 3.1 Basic Composable

```kotlin
// ui/components/Button.kt
package com.company.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(text = text)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text = text)
    }
}
```

Guidelines:
- Use `@Composable` annotation
- Modifier as last parameter with default value
- Boolean parameters have default values
- Event handlers use lambda types
- Use Material3 components

### 3.2 Stateful Composable with State Hoisting

```kotlin
// ui/components/SearchBar.kt
package com.company.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        singleLine = true
    )
}

// Usage with state hoisting
@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    
    Column {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )
        
        // Display results based on searchQuery
    }
}
```

### 3.3 Screen with ViewModel

```kotlin
// feature/user/ui/list/UserListScreen.kt
package com.company.user.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UserListScreen(
    onUserClick: (String) -> Unit,
    viewModel: UserListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    UserListContent(
        uiState = uiState,
        onUserClick = onUserClick,
        onRetry = { viewModel.loadUsers() },
        onSearchQueryChange = { viewModel.updateSearchQuery(it) }
    )
}

@Composable
private fun UserListContent(
    uiState: UserListUiState,
    onUserClick: (String) -> Unit,
    onRetry: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Users",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                ErrorState(
                    message = uiState.error,
                    onRetry = onRetry
                )
            }

            uiState.users.isEmpty() -> {
                EmptyState(message = "No users found")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.users,
                        key = { it.id }
                    ) { user ->
                        UserCard(
                            user = user,
                            onClick = { onUserClick(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryButton(
            text = "Retry",
            onClick = onRetry
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

---

## 4. State Management with ViewModel

### 4.1 UI State

```kotlin
// feature/user/ui/list/UserListState.kt
package com.company.user.ui.list

import com.company.domain.model.User

data class UserListUiState(
    val users: List<User> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface UserListUiEvent {
    data class ShowMessage(val message: String) : UserListUiEvent
    data class NavigateToDetail(val userId: String) : UserListUiEvent
}
```

### 4.2 ViewModel

```kotlin
// feature/user/ui/list/UserListViewModel.kt
package com.company.user.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.domain.usecase.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UserListUiEvent>()
    val uiEvent: SharedFlow<UserListUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getUsersUseCase()
                .onSuccess { users ->
                    _uiState.update {
                        it.copy(
                            users = users,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error"
                        )
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onUserClick(userId: String) {
        viewModelScope.launch {
            _uiEvent.emit(UserListUiEvent.NavigateToDetail(userId))
        }
    }
}
```

Guidelines:
- Use `StateFlow` for UI state
- Use `SharedFlow` for one-time events
- Update state with `update {}` or `value =`
- Handle errors in ViewModel
- Use `viewModelScope` for coroutines

---

## 5. API Integration and Types

### 5.1 API Type Definitions (CRITICAL)

**BEFORE implementing any new feature:**

1. Check if API DTOs exist in `data/remote/dto/` directory
2. If types DO NOT exist, you MUST:
   - Ask backend team for API contract/schema
   - Request Swagger/OpenAPI documentation
   - Get example request/response
   - Clarify data model structure
3. Create DTOs based on actual API contract
4. Never assume or guess API structure

```kotlin
// data/remote/dto/UserDto.kt
package com.company.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("role")
    val role: String,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class UserListResponseDto(
    @SerialName("data")
    val data: List<UserDto>,
    @SerialName("pagination")
    val pagination: PaginationDto
)

@Serializable
data class PaginationDto(
    @SerialName("page")
    val page: Int,
    @SerialName("limit")
    val limit: Int,
    @SerialName("total")
    val total: Int,
    @SerialName("total_pages")
    val totalPages: Int
)

@Serializable
data class UserCreateRequest(
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("role")
    val role: String? = null
)

@Serializable
data class UserUpdateRequest(
    @SerialName("name")
    val name: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("role")
    val role: String? = null
)

@Serializable
data class ApiErrorDto(
    @SerialName("error")
    val error: String,
    @SerialName("message")
    val message: String,
    @SerialName("status_code")
    val statusCode: Int
)
```

### 5.2 Feature Development Workflow

**Step 1: Verify API Contract**
```kotlin
/**
 * CHECKLIST before creating new feature:
 * 
 * [ ] API endpoint exists and documented
 * [ ] Request/response DTOs defined in data/remote/dto/
 * [ ] Error response structure known
 * [ ] Authentication requirements clear
 * [ ] Pagination/filtering strategy understood
 * 
 * IF MISSING:
 * - Contact backend team
 * - Request API documentation
 * - Ask for data model/schema
 * - Get example payloads
 * - Clarify error handling
 */
```

**Step 2: Create API Interface**
```kotlin
// data/remote/api/UserApi.kt
package com.company.data.remote.api

import com.company.data.remote.dto.*
import retrofit2.http.*

/**
 * User API endpoints
 * 
 * Base URL: /api/v1/users
 * Authentication: Bearer token required
 */
interface UserApi {

    /**
     * Get all users
     * 
     * Endpoint: GET /api/v1/users
     * Auth: Required
     * Params: page, limit, role, search
     */
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("role") role: String? = null,
        @Query("search") search: String? = null
    ): UserListResponseDto

    /**
     * Get user by ID
     * 
     * Endpoint: GET /api/v1/users/{id}
     * Auth: Required
     */
    @GET("users/{id}")
    suspend fun getUser(
        @Path("id") id: String
    ): UserDto

    /**
     * Create new user
     * 
     * Endpoint: POST /api/v1/users
     * Auth: Required (Admin only)
     */
    @POST("users")
    suspend fun createUser(
        @Body request: UserCreateRequest
    ): UserDto

    /**
     * Update user
     * 
     * Endpoint: PUT /api/v1/users/{id}
     * Auth: Required (Admin or own user)
     */
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UserUpdateRequest
    ): UserDto

    /**
     * Delete user
     * 
     * Endpoint: DELETE /api/v1/users/{id}
     * Auth: Required (Admin only)
     */
    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") id: String
    )
}
```

**Step 3: Create Domain Model**
```kotlin
// domain/model/User.kt
package com.company.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

enum class UserRole {
    ADMIN,
    USER
}
```

**Step 4: Create Mapper**
```kotlin
// data/remote/mapper/UserMapper.kt
package com.company.data.remote.mapper

import com.company.data.remote.dto.UserDto
import com.company.domain.model.User
import com.company.domain.model.UserRole

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        role = when (role.lowercase()) {
            "admin" -> UserRole.ADMIN
            else -> UserRole.USER
        },
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<UserDto>.toDomain(): List<User> {
    return map { it.toDomain() }
}
```

**Step 5: Create Repository**
```kotlin
// data/repository/UserRepositoryImpl.kt
package com.company.data.repository

import com.company.data.remote.api.UserApi
import com.company.data.remote.dto.UserCreateRequest
import com.company.data.remote.mapper.toDomain
import com.company.domain.model.User
import com.company.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = userApi.getUsers()
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUser(id: String): Result<User> {
        return try {
            val user = userApi.getUser(id)
            Result.success(user.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(
        name: String,
        email: String,
        password: String,
        role: String?
    ): Result<User> {
        return try {
            val request = UserCreateRequest(name, email, password, role)
            val user = userApi.createUser(request)
            Result.success(user.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            userApi.deleteUser(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Step 6: Document Missing API**
```kotlin
// When API DTOs are missing, create this file first:

// data/remote/dto/FEATURE_NAME.PENDING.kt
package com.company.data.remote.dto

/**
 * PENDING API DOCUMENTATION
 * 
 * Feature: User Management
 * Status: Awaiting backend contract
 * 
 * NEEDED INFORMATION:
 * 1. Endpoint URLs and HTTP methods
 * 2. Request body structure
 * 3. Response data format
 * 4. Error response format
 * 5. Authentication requirements
 * 6. Pagination strategy (offset/cursor)
 * 7. Filter/sort parameters
 * 
 * TEMPORARY TYPES (update when confirmed):
 */

// TODO: Confirm with backend team
data class UserDto(
    val id: String,
    val name: String
    // Add more fields as needed
)
```

### 5.3 Network Configuration

```kotlin
// di/NetworkModule.kt
package com.company.app.di

import com.company.data.remote.api.UserApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${getToken()}")
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    private fun getToken(): String {
        // Get token from secure storage
        return ""
    }
}
```

---

## 6. Navigation

### 6.1 Navigation Setup

```kotlin
// ui/navigation/NavGraph.kt
package com.company.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.company.user.ui.detail.UserDetailScreen
import com.company.user.ui.list.UserListScreen

sealed class Screen(val route: String) {
    object UserList : Screen("users")
    object UserDetail : Screen("users/{userId}") {
        fun createRoute(userId: String) = "users/$userId"
    }
    object Login : Screen("login")
    object Home : Screen("home")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.UserList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.UserList.route) {
            UserListScreen(
                onUserClick = { userId ->
                    navController.navigate(Screen.UserDetail.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            requireNotNull(userId)
            
            UserDetailScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

### 6.2 MainActivity

```kotlin
// MainActivity.kt
package com.company.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.company.app.ui.navigation.AppNavGraph
import com.company.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
```

---

## 7. Material Design 3 Theme

### 7.1 Color Scheme

```kotlin
// ui/theme/Color.kt
package com.company.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF6200EE)
val PrimaryVariant = Color(0xFF3700B3)
val Secondary = Color(0xFF03DAC6)
val SecondaryVariant = Color(0xFF018786)
val Background = Color(0xFFFFFFFF)
val Surface = Color(0xFFFFFFFF)
val Error = Color(0xFFB00020)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFF000000)
val OnBackground = Color(0xFF000000)
val OnSurface = Color(0xFF000000)
val OnError = Color(0xFFFFFFFF)

val DarkPrimary = Color(0xFFBB86FC)
val DarkPrimaryVariant = Color(0xFF3700B3)
val DarkSecondary = Color(0xFF03DAC6)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF121212)
val DarkError = Color(0xFFCF6679)
```

### 7.2 Theme

```kotlin
// ui/theme/Theme.kt
package com.company.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    error = Error,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onError = OnError
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    error = DarkError,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnPrimary,
    onSurface = OnPrimary,
    onError = OnError
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### 7.3 Typography

```kotlin
// ui/theme/Type.kt
package com.company.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
```

---

## 8. Testing

### 8.1 Unit Test (ViewModel)

```kotlin
// feature/user/ui/list/UserListViewModelTest.kt
package com.company.user.ui.list

import com.company.domain.model.User
import com.company.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var getUsersUseCase: GetUsersUseCase

    private lateinit var viewModel: UserListViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers success updates state correctly`() = runTest {
        val users = listOf(
            User("1", "John", "john@example.com", UserRole.USER, true, "", "")
        )
        whenever(getUsersUseCase()).thenReturn(Result.success(users))

        viewModel = UserListViewModel(getUsersUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(users, state.users)
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
    }

    @Test
    fun `loadUsers failure updates error state`() = runTest {
        val errorMessage = "Network error"
        whenever(getUsersUseCase()).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = UserListViewModel(getUsersUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.users.isEmpty())
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
    }
}
```

### 8.2 Composable UI Test

```kotlin
// feature/user/ui/components/UserCardTest.kt
package com.company.user.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.company.domain.model.User
import com.company.domain.model.UserRole
import com.company.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class UserCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userCard_displaysUserInformation() {
        val user = User(
            id = "1",
            name = "John Doe",
            email = "john@example.com",
            role = UserRole.USER,
            isActive = true,
            createdAt = "",
            updatedAt = ""
        )

        composeTestRule.setContent {
            AppTheme {
                UserCard(user = user, onClick = {})
            }
        }

        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
    }

    @Test
    fun userCard_clickTriggersCallback() {
        var clicked = false
        val user = User("1", "John", "john@example.com", UserRole.USER, true, "", "")

        composeTestRule.setContent {
            AppTheme {
                UserCard(user = user, onClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithText("John").performClick()
        assert(clicked)
    }
}
```

---

## 9. Build Configuration

### 9.1 build.gradle.kts (App Module)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("kapt")
}

android {
    namespace = "com.company.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.company.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## 10. Pre-Submission Checklist

- [ ] Code follows naming conventions
- [ ] Composables follow single responsibility principle
- [ ] **API DTOs exist in data/remote/dto/ directory**
- [ ] **If API types missing: contacted backend team for contract**
- [ ] **API endpoints documented with auth requirements**
- [ ] Mappers convert DTOs to domain models
- [ ] Repository uses Result type for error handling
- [ ] ViewModels use StateFlow for UI state
- [ ] State hoisting applied correctly
- [ ] Modifiers passed as last parameter
- [ ] LazyColumn items have keys
- [ ] No state modification in Composables (side effects in LaunchedEffect)
- [ ] Material3 components used
- [ ] Theme properly configured
- [ ] Navigation graph properly structured
- [ ] Dependency injection with Hilt configured
- [ ] Tests written for ViewModels and critical Composables
- [ ] Build passes without errors
- [ ] ProGuard rules added if needed
- [ ] Accessibility content descriptions added

---

## 11. New Feature Development Strategy

### When creating a new feature:

1. **Check API DTOs**: Look in `data/remote/dto/FEATURE_NAME*.kt`
2. **If missing**: Create `data/remote/dto/FEATURE_NAME.PENDING.kt` and:
   - Contact backend team
   - Request API documentation
   - Ask for data model/schema
   - Get example request/response
   - Clarify authentication requirements
3. **Document API contract**: Include endpoint, method, auth, request/response
4. **Create domain model**: Define clean domain entities
5. **Create mapper**: DTO to domain model conversion
6. **Create repository**: Implement with Result type
7. **Create use case**: Business logic layer
8. **Create ViewModel**: Handle UI state and events
9. **Build UI**: Composables with state hoisting
10. **Test thoroughly**: Unit and UI tests

---

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-26 | Initial Jetpack Compose guidelines with API type strategy |
