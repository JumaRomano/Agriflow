# AgriFlow

**AgriFlow** is an offline-first Android application designed to empower agricultural producers, distributors, and buyers. It provides a digital marketplace, inventory management, role-based workflows, and real-time order tracking.

---

## Key Features

- **Marketplace & Sourcing**: Browse agricultural products by category, filter by search terms, and inspect detailed product specifications and live stock availability.
- **Store & Inventory Management**: Farmers and suppliers can list products, edit pricing and stock status, upload images, and draft inventory offline.
- **Cart & Checkout**: Manage cart items, compute totals, and place orders with integrated payment method support.
- **Role-Based Accounts**: Multi-role support for **Buyers**, **Farmers**, and **Enterprise** users with dedicated role upgrade paths.
- **Supplier Network**: Discover and connect with verified agricultural distributors and partners.
- **Offline-First Architecture**: Built on Room database for persistent local storage, ensuring smooth operation even in low-connectivity areas.

---

## Tech Stack & Architecture

- **UI Framework**: Jetpack Compose with Material 3 design components.
- **Architecture**: Unidirectional Data Flow (UDF) with MVVM, Clean Architecture boundaries, and StateFlow/SharedFlow.
- **Dependency Injection**: Dagger Hilt.
- **Networking**: Retrofit 2 & OkHttp 4 for REST API interactions.
- **Local Database**: Room DB for offline cache and data persistence.
- **Firebase Services**: Firebase Messaging (FCM) for push notifications & Firebase Storage.
- **Navigation**: Type-safe Navigation Compose using Kotlinx Serialization.

---

## Prerequisites & Setup

- **Android Studio**: Ladybug (2024.2.1+) or newer.
- **JDK**: Java 17 or Java 21 (Android Studio Embedded JDK / JBR recommended).
- **Minimum SDK**: API Level 24 (Android 7.0).
- **Target SDK**: API Level 36.

### Build from Command Line

To build the debug APK:

```bash
./gradlew assembleDebug
```

To run unit tests:

```bash
./gradlew test
```

---

## License

This project is proprietary and maintained for the AgriFlow platform.
