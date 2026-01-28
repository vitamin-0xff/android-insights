# Android Device Insights (Phone Checker)

## Project Overview

Android Device Insights is a comprehensive native Android application designed to provide users with detailed health metrics and actionable insights about their device. The application monitors various system components and presents real-time data to help users understand their device's performance, identify potential issues, and optimize device health.

## Technology Stack

### Core Technologies
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material You design system
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 36
- **Architecture:** Clean Architecture with MVVM pattern
- **Dependency Injection:** Hilt (Dagger)
- **State Management:** StateFlow and Compose State
- **Build System:** Gradle with Kotlin DSL

### Key Libraries
- AndroidX Core KTX
- Lifecycle Runtime KTX
- Activity Compose
- Compose BOM (Bill of Materials)
- Hilt Navigation Compose
- Material3 Components

## Project Structure

### Architecture Layers

#### UI Layer
- Jetpack Compose screens for all features
- ViewModels managing UI state with StateFlow
- Navigation graph with type-safe routing
- Material You theming with dynamic colors

#### Domain Layer
- Use cases encapsulating business logic
- Clean separation between data and presentation
- Domain models independent of data sources

#### Data Layer
- Repository pattern abstracting data sources
- Android system service integrations
- Data models representing device metrics

### Module Organization

```
app/
├── data/              # Data layer with repositories
├── domain/            # Business logic and use cases
└── ui/
    ├── features/      # Feature-specific screens
    ├── navigation/    # Navigation configuration
    └── theme/         # Material3 theme setup
```

## Android System Services Used

The application integrates with various Android system services to collect device metrics:

### BatteryManager
- Battery level and charging status
- Battery temperature and voltage
- Battery health status
- Charge counter and cycle count
- Current flow measurements (charging/discharging rates)
- Energy counter metrics

### SensorManager
- Access to all device sensors (accelerometer, gyroscope, magnetometer, etc.)
- Sensor availability detection
- Sensor status monitoring
- Real-time sensor data collection

### PowerManager
- Thermal status monitoring
- Device thermal state detection
- Temperature thresholds

### ActivityManager
- Running applications tracking
- Memory usage statistics
- Process information

### StorageManager
- Internal and external storage metrics
- Available and used storage space
- Storage health status

### ConnectivityManager
- Network connectivity status
- Network type detection (WiFi, Cellular, Ethernet)
- Network quality assessment

### AudioManager
- Audio device information
- Audio routing status
- Volume levels

### WindowManager / Display
- Screen properties and resolution
- Screen-on time tracking
- Display refresh rate

## Features Currently Implemented

### 1. Dashboard
- Central health monitoring hub
- Grid-based metrics overview
- Color-coded status indicators (Good, Warning, Critical)
- Quick navigation to detailed feature screens
- Real-time metric updates

### 2. Battery Health Monitoring
- Current battery level percentage
- Battery temperature in Celsius
- Voltage measurements
- Battery health status (Good, Overheat, Dead, Over-voltage, Cold)
- Charging status and type detection
- Advanced metrics: charge counter, current flow, energy counter, cycle count
- Battery technology information

### 3. Sensor Diagnostics
- Complete list of available device sensors
- Sensor status monitoring (Available, Unavailable, Accuracy levels)
- Sensor type identification
- Sensor vendor and version information
- Overall sensors health assessment

### 4. Thermal Monitoring
- Device temperature tracking
- Thermal status classification
- Heat level warnings
- Temperature trends

### 5. Performance Metrics
- CPU usage monitoring
- RAM utilization tracking
- Memory statistics
- Performance health indicators

### 6. Storage Analysis
- Total storage capacity
- Used and available storage
- Storage usage percentage
- Storage health status
- Breakdown by storage type

### 7. Network Monitoring
- Network connectivity status
- Connection type identification
- Network quality assessment (Excellent, Good, Fair, Poor)
- Data usage tracking capabilities

### 8. Screen Health
- Screen-on time tracking
- Display properties
- Screen health metrics
- Usage patterns

### 9. Audio System Diagnostics
- Audio device health
- Audio routing information
- Output device detection
- Audio system status

### 10. App Behavior Analysis
- Running applications count
- App resource usage
- Background app detection
- App behavior profiling

### 11. Insights & Recommendations
- AI-powered device insights
- Actionable recommendations
- Health score calculations
- Optimization suggestions

### 12. Onboarding Experience
- First-run introduction
- Feature overview
- Permission requests flow

## Current Implementation Status

### Completed Components
- All 12 feature screens with UI implementation
- Complete navigation system with proper back stack management
- Repository layer for all features with real Android API integrations
- Use case layer following clean architecture principles
- ViewModel implementation with state management
- Material3 theming and design system
- Error handling and loading states
- Hilt dependency injection setup

## Suggested Future Enhancements

### Data Persistence
- Implement Room database for historical data storage
- Track metrics over time (daily, weekly, monthly trends)
- Store sensor readings and battery statistics
- Enable data export and import functionality

### Background Monitoring
- Implement WorkManager for periodic metric collection
- Add foreground service for continuous monitoring (opt-in)
- Create background jobs for data cleanup and aggregation
- Implement adaptive sampling based on battery level

### Advanced Analytics
- Add TensorFlow Lite for predictive analytics
- Battery life prediction based on usage patterns
- Device health forecasting
- Anomaly detection for unusual behavior

### Data Visualization
- Integrate chart libraries (Vico or MPAndroidChart)
- Add historical trend graphs
- Create visual comparisons over time periods
- Interactive chart controls

### Notifications & Alerts
- Critical battery health warnings
- Thermal threshold notifications
- Storage space alerts
- Customizable notification preferences

### Settings & Customization
- User preferences with DataStore
- Monitoring interval configuration
- Data retention policies
- Theme customization options
- Privacy controls

### Reports & Export
- Generate comprehensive device health reports
- Export data in multiple formats (JSON, CSV, PDF)
- Share reports via standard Android sharing
- Scheduled report generation

### Automation Features
- Automated optimization routines
- Battery saver mode automation
- Performance profile switching
- Conditional action triggers

### Testing
- Unit tests for ViewModels and use cases
- Repository layer testing with mocks
- UI tests for critical flows
- Integration tests for system service interactions

## Build & Run Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK API 36
- Gradle 8.x

### Building the Project
```bash
# Clone the repository
git clone <repository-url>

# Navigate to project directory
cd phonechecker

# Build the project
./gradlew build

# Install on connected device
./gradlew installDebug
```

### Running Tests
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Permissions Required

The application requires the following permissions to function properly:

- **Battery Stats:** Implicit access through BatteryManager
- **Sensors:** Implicit access through SensorManager
- **Storage:** READ_EXTERNAL_STORAGE (for storage metrics)
- **Network State:** ACCESS_NETWORK_STATE
- **Phone State:** For device information (may require runtime permission)

Note: Additional permissions may be required for future features like background monitoring or detailed app usage statistics.

## Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Document complex logic with comments
- Prefer immutability where possible

### Architecture Principles
- Maintain clear separation of concerns
- Keep UI layer stateless (state hoisting)
- Repository pattern for data access
- Use cases for business logic
- Unidirectional data flow (UDF)

### Compose Best Practices
- Prefer stateless composables
- Use remember and derivedStateOf appropriately
- Avoid recomposition issues
- Follow Material3 design guidelines

### Testing Strategy
- Unit test ViewModels and use cases
- Mock repositories for testing
- Test state transformations
- Verify error handling paths

## Contributing

When contributing to this project:

1. Follow the existing architecture patterns
2. Maintain consistency with current UI/UX design
3. Write unit tests for new features
4. Update documentation for significant changes
5. Ensure code passes lint checks before committing

## Known Limitations

### Platform Constraints
- Some metrics require Android 8.0+ (API 26+)
- Battery cycle count requires Android 14+ (API 34+)
- Thermal status availability varies by OEM
- Certain sensors may not be available on all devices

### Current Implementation
- No historical data persistence (in-memory only)
- No background monitoring service
- No data export functionality
- Limited predictive analytics
- No chart visualizations yet

## Troubleshooting

### Common Issues

**Metrics showing as unavailable:**
- Some features may not be supported on all devices
- Check if running on physical device vs emulator
- Verify Android API level compatibility

**Permission errors:**
- Ensure all required permissions are granted
- Check AndroidManifest.xml for proper declarations
- Request runtime permissions where needed

**Build errors:**
- Sync project with Gradle files
- Invalidate caches and restart Android Studio
- Check Kotlin and Gradle versions

---

**Version:** 1.0  
**Last Updated:** January 28, 2026  
**Status:** Active Development
