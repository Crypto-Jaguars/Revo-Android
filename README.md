
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

## ⚙️ Installation
Follow these steps to set up the project on your local machine:

1. **Clone the Repository**
   Open a terminal and run the following command to clone the repository:
   ```bash
   git clone https://github.com/Crypto-Jaguars/Revo-Android.git
   ```

2. **Open the Project in Android Studio**
   Navigate to the android folder inside the cloned repository:
   ```bash
   cd Revo-Android
   ```
   Then, open the project in Android Studio:
   - Launch **Android Studio**
   - Click on "Open an existing Android Studio project"

3. **Configure Dependencies**
   Add the Stellar SDK and other required dependencies in your build.gradle file:
   ```bash
   implementation 'stellar-sdk:stellar-sdk:latest_version'
   ```

4. **Configure the Trustless Work API**
   Follow the [Trustless Work documentation](https://docs.trustlesswork.com/trustless-work) to correctly integrate the API into your application. Make sure to have all the necessary credentials and configurations, such as API keys and corresponding endpoints.

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
