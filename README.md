# Android User Directory

A modern Android application showcasing best practices in Android development with Jetpack Compose, implementing a clean MVVM architecture pattern. This app demonstrates fetching user data from a REST API, displaying it in a responsive UI, and providing features like search, swipe-to-action, and detailed user profiles.



## ✨ Features

- **User List**: Display users in a modern UI with smooth scrolling
- **Search Functionality**: Filter users by name or email in real-time
- **Swipe Actions**: Swipe-to-delete or swipe-to-approve user entries
- **User Details**: View comprehensive user information including address and company details
- **Error Handling**: Robust error handling for network issues
- **Offline Support**: Graceful degradation when network is unavailable

## 🏗️ Architecture

This project follows the MVVM (Model-View-ViewModel) architectural pattern 

### Key Components:

- **ViewModel**: Manages UI-related data in a lifecycle-conscious way
- **StateFlow**: Handles reactive UI updates
- **Repository Pattern**: Abstracts data sources from the rest of the app
- **Dependency Injection**: Uses Hilt to manage dependencies throughout the app

## 🛠️ Tech Stack

### Framework & UI
- **Jetpack Compose**: Modern declarative UI toolkit
- **Material Design**: Implementation of Google's Material Design guidelines
- **Navigation Compose**: For handling navigation between screens

### Architecture Components
- **ViewModel**: Store and manage UI-related data
- **StateFlow**: Reactive state holder for UI state
- **Kotlin Coroutines**: For asynchronous programming
- **Hilt**: Dependency injection library

### Networking
- **Retrofit**: Type-safe HTTP client
- **OkHttp**: HTTP client for network requests
- **Gson**: For JSON serialization/deserialization



⭐ **If you found this project helpful, please give it a star!** ⭐

![Podgląd aplikacji](https://i.imgur.com/iVAYYXN.png)
![Podgląd aplikacji](https://i.imgur.com/reIhDQ3.png)
![Podgląd aplikacji](https://i.imgur.com/zWFugjb.png)
