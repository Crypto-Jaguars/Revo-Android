# Revolutionary Farmers Marketplace
Revolutionary Farmers Marketplace is an Android application designed to facilitate secure trading between farmers and buyers using the [Trustless Work API](https://docs.trustlesswork.com/trustless-work) and Stellar network. This application enables farmers to list their harvested products and buyers to purchase them with the security of smart escrows.

## 📁 Project Structure
The project is located in the `Revo-Android` folder and is developed using Android Studio.

## 📝 Prerequisites
### Minimum Hardware Requirements:
- Operating System: Windows (8, 10, or 11) / Linux / macOS (10.14 Mojave or later) / ChromeOS
- RAM: 8 GB (16 GB recommended)
- Storage: 4 GB of free space for Android Studio and dependencies
- CPU: 64-bit processor (Intel or AMD)

### Software Requirements:
- [Android Studio](https://developer.android.com/studio): Version 4.0 or higher
- [JDK](https://www.oracle.com/java/technologies/javase-downloads.html): Version 8 or higher
- An Android device or a configured emulator

## 🛠️ Setup & Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Crypto-Jaguars/Revo-Android.git
   cd Revo-Android
   ```

2. **Setup Development Environment**
   - Install Android Studio (latest version)
   - Install JDK 11 or higher
   - Install the Android SDK (minimum API 28)

3. **Project Configuration**
   - Open the project in Android Studio
   - Wait for Gradle sync to complete
   - Add these to your gradle.properties if not present:
     ```properties
     android.useAndroidX=true
     kotlin.code.style=official
     android.nonTransitiveRClass=true
     ```

4. **Clean and Build**
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

5. **Run the Application**
   - Connect an Android device or start an emulator (API 28+)
   - Run the app using Android Studio's "Run" button
   - Or use command line:
     ```bash
     ./gradlew installDebug
     ```

## 🔧 Troubleshooting

If you encounter build errors:
1. File -> Invalidate Caches / Restart
2. Delete the .gradle and build folders:
   ```bash
   rm -rf .gradle
   rm -rf app/build
   ```
3. Sync project with Gradle files
4. Clean and rebuild project

## 🚀 Usage
Once you have installed and configured the project, follow these steps to run and use the application:

1. **Compile and Run**
   In **Android Studio**, select a device or emulator on which you want to run the application. Then, click the "Run" button (or press **Shift + F10**) to compile and run the application.

2. **Interacting with the Application**
   Upon opening the application, you will be able to:
   - **List Products:** Farmers can list their harvested products with details and pricing
   - **Browse Products:** Buyers can browse available products from different farmers
   - **Secure Transactions:** Execute trades using smart escrows on the Stellar network
   - **Track Orders:** Monitor the status of ongoing transactions and deliveries

## 🛠 Tech Stack
- **[Android SDK](https://developer.android.com/studio)** - Framework for building Android applications
- **[Kotlin](https://kotlinlang.org/)** - Programming language for building Android apps
- **[Stellar SDK](https://developers.stellar.org/docs/software-and-sdks)** - For integration with the Stellar network
- **[Trustless Work API](https://docs.trustlesswork.com/trustless-work)** - Enables trustless payments via smart contracts, securing funds in escrow until successful delivery

## 👨🏼‍🌾👩🏼‍🌾 Working together 
This repository is part of the Revolutionary Farmers project, working in conjunction with the Revo-Frontend and Revo-Backend repositories to create a complete marketplace ecosystem for farmers.
