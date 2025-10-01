# Lumina Client

![Lumina Client Logo](./images/lumina.jpg)  
[![Download count](https://img.shields.io/github/downloads/TheProjectLumina/LuminaClient/total.svg)](https://github.com/TheProjectLumina/LuminaClient/releases)[![Netlify Status](https://api.netlify.com/api/v1/badges/679c08db-f713-4384-b052-1fd2f90d35f3/deploy-status)](https://app.netlify.com/projects/projectlumina/deploys) [![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)[![GitHub issues](https://img.shields.io/github/issues/TheProjectLumina/LuminaClient.svg) ](https://github.com/TheProjectLumina/LuminaClient/issues)[![GitHub stars](https://img.shields.io/github/stars/TheProjectLumina/LuminaClient.svg)](https://github.com/TheProjectLumina/LuminaClient/stargazers)[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?logo=intellij-idea&logoColor=white)](https://www.jetbrains.com/idea/)[![Firebase](https://img.shields.io/badge/Firebase-039BE5?logo=Firebase&logoColor=white)](https://firebase.google.com/)[![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/studio)

## Introduction

Lumina Client is a tool built for Minecraft Bedrock Edition players who want to step up their game in competitive PvP. It’s designed to work smoothly across different devices, offering a reliable and user-friendly experience. Our team has worked hard to create a client that supports players on popular servers while keeping performance steady and dependable. Whether you’re a player looking to improve your skills or a developer wanting to dive into our code, Lumina is here to help you succeed.

---

## Why Lumina?

Lumina is all about giving Minecraft players the tools they need to shine in PvP. We’ve focused on making it easy to use, fast, and compatible with a range of platforms. The Project Lumina team is passionate about building a tool that’s both powerful and open for the community to explore and improve. We encourage everyone to use Lumina responsibly, contribute ideas, and join us in making it even better.

---

## Core Features

Lumina Client offers a set of tools to help you perform better in Minecraft Bedrock Edition PvP. We’ve built it to be fast, reliable, and easy to customize, so you can tweak it to fit your playstyle. The client works well on many servers, with features designed to keep your gameplay smooth and effective. Every part of Lumina has been tested to ensure it runs stably, making it a solid choice for players who want to take their skills to the next level.

---

## Community Highlights

The Project Lumina community is growing fast, and we love seeing all the energy and support from everyone! Our [Discord server](https://discord.gg/78bqDpAHmK) is full of people chatting, sharing ideas, and just hanging out every day.

Whether you're new or a regular, it's a great place to:

- Meet other Lumina players
- Share your creations
- Ask questions and get help
- Stay up to date with what’s new

Come say hello in the [Discord](https://discord.gg/78bqDpAHmK) — we’d love to have you around!

---

## System Requirements

To run Lumina Client, your device must meet the following specifications:

- **Android**: Android 9.0 (Pie) or later, supporting both 64-bit and 32-bit architectures.
- **Minecraft Version**: Minecraft Bedrock Edition 1.21.80 or later is recommended for optimal performance and compatibility.
- **Non-Android Platforms**: For PC, Mac, or other devices, Lumina operates remotely through an Android device running the client.
- **Storage**: At least 200 MB of free storage for the APK and associated data.
- **Internet**: A stable internet connection is recommended, especially for multiplayer servers.

No additional hardware is required, but enabling developer options on Android may improve the installation process.

---

## Installation Instructions

Setting up Lumina Client is quick and straightforward. Follow these steps:

1. **Download the APK**: Head to our [Releases page](https://github.com/TheProjectLumina/LuminaClient/releases) and download the latest Lumina Client APK.
2. **Allow Unknown Sources**: On your Android device, go to **Settings > Security** (or **Apps & Notifications** on newer versions) and enable **Install from Unknown Sources**.
3. **Install the APK**: Open the downloaded APK file using a file manager, tap to install, and follow the prompts.
4. **Launch Lumina**: Find Lumina in your app drawer, open it, and log in with your Minecraft account.
5. **Configure Settings**: Adjust keybinds, enable desired tools, and customize the UI to suit your preferences.
6. **Test on a Server**: Join a test server to ensure everything works as expected. For best results, start with a non-protected server to familiarize yourself with the features.

If you encounter issues, visit our [Discord](https://discord.gg/78bqDpAHmK) for support or consult the [NOTICE](https://github.com/TheProjectLumina/LuminaClient/blob/main/NOTICE) file.

---

## Development Setup

For developers looking to contribute or customize Lumina, here’s how to set up your environment:

1. **Clone the Repository**:

    ```bash
    git clone https://github.com/TheProjectLumina/LuminaClient.git
    ```

   This downloads the full source code to your local machine.

2. **Open in Android Studio**:

   - Launch Android Studio and select **Open an existing project**.
   - Choose the `LuminaClient` directory and open it.
3. **Replace google-services.json**:

   - The repository includes a dummy `google-services.json` file, which must be replaced with your own for the app to build successfully.
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app to your Firebase project, following the prompts to download your `google-services.json` file.
   - Place the downloaded `google-services.json` file in the `app` directory of the LuminaClient project, overwriting the dummy file.
   - Ensure the package name in Firebase matches the one in `build.gradle` (e.g., `com.projectlumina.luminaclient`).
4. **Sync Gradle**:

   - Click **Sync Project with Gradle Files** to fetch dependencies.
   - Ensure you have the Android SDK (API level 28 or higher) and Gradle installed.
5. **Build and Test**:

   - Connect an Android device or configure an emulator.
   - Select **Run > Run 'app'** to build and deploy Lumina.
   - Test changes on a local Minecraft Bedrock server to verify functionality.

Refer to inline comments and the [NOTICE](https://github.com/TheProjectLumina/LuminaClient/blob/main/NOTICE) file for guidance. For advanced development tips, join our [Discord](https://discord.gg/78bqDpAHmK) or check our [YouTube](https://youtube.com/@prlumina) for tutorials.

---

## Contribution Guidelines

We welcome contributions to improve Lumina Client. To ensure a high-quality codebase, please adhere to these guidelines:

- **Code Quality**: Write clean, modular code with descriptive variable names and consistent formatting (follow Kotlin style guidelines).
- **Commit Structure**: Use small, focused commits with clear messages (e.g., "Fixed lag issue on Hive server").
- **Documentation**: Add comments for complex logic and update relevant documentation in the repository.
- **Performance**: Optimize additions to maintain low latency, especially for combat and networking modules.
- **Testing**: Test changes on multiple servers (e.g., CubeCraft, Hive) to ensure compatibility and stability.
- **Pull Requests**: Submit PRs with a detailed description of changes, including the problem solved or feature added.
- **Community Standards**: Follow our [Code of Conduct](https://github.com/TheProjectLumina/LuminaClient/blob/main/CODE_OF_CONDUCT.md) to maintain a respectful environment.

Before submitting, run a full build and test cycle to minimize errors. We review all contributions promptly and appreciate your efforts to enhance Lumina.

---

## Usage Guidelines

Lumina is designed for personal use and experimentation, but there are rules to follow:

### Permitted Uses

- Modify Lumina for personal gameplay or to test new features.
- Create educational content (e.g., YouTube videos, tutorials) showcasing Lumina’s capabilities.
- Fork the repository for learning or to create derivative projects, provided you comply with the [GNU GPL v3.0](https://github.com/TheProjectLumina/LuminaClient/blob/main/LICENSE).

### Prohibited Uses

- Do not distribute modified versions without sharing the source code, as required by the GPL.
- Do not claim Lumina as your own without crediting the Project Lumina team and its contributors.
- Selling Lumina or derivatives without adhering to the GPL is prohibited.
- Do not distribute Lumina as closed-source or under a non-GPL license.
- The authors are not responsible for bans, damages, or issues arising from Lumina’s use.

See the [LICENSE](https://github.com/TheProjectLumina/LuminaClient/blob/main/LICENSE) and [NOTICE](https://github.com/TheProjectLumina/LuminaClient/blob/main/NOTICE) files for full details.

---

## Community and Support

Join our community to connect with other Lumina users and developers:

- **Discord**: Our [Discord server](https://discord.gg/78bqDpAHmK) is the best place for real-time support, bug reports, and feature discussions.
- **YouTube**: Visit our [YouTube channel](https://youtube.com/@prlumina) for tutorials, feature demos, and project updates.
- **GitHub**: Report issues or suggest enhancements on our [GitHub Issues page](https://github.com/TheProjectLumina/LuminaClient/issues).

We strive to maintain a welcoming and inclusive community. Please review our [Code of Conduct](https://github.com/TheProjectLumina/LuminaClient/blob/main/CODE_OF_CONDUCT.md) before participating.

---

## Credits and Acknowledgments

Lumina Client Version 4 is a fork of [MuCuteClient](https://github.com/isuckatcodingfr/MuCuteClient) by SuMuCheng and [Protohax](https://github.com/hax0r31337/ProtoHax) by Haxor, with extensive enhancements by the Project Lumina team. Copyright © 2025 Project Lumina, with portions © 2025 MuCuteClient/SuMuCheng and © 2023 Protohax/Haxor.

We are deeply grateful to the open-source community for providing the tools and libraries that make Lumina possible. Below are the key projects we rely on:

||||
|---|---|---|
|![Netty Logo](./netty.png)  <br>**[Netty](https://netty.io/)**  <br>High-performance network framework for efficient packet handling.  <br>[Documentation](https://netty.io/wiki/user-guide.html)|![Kotlin Logo](./images/kotlin.png)  <br>**[Kotlin](https://kotlinlang.org/)**  <br>Modern language for concise, maintainable code.  <br>[Documentation](https://kotlinlang.org/docs/home.html)|![OkHttp Logo](./images/okhttp.webp)  <br>**[OkHttp](https://square.github.io/okhttp/)**  <br>Robust HTTP client for reliable network requests.  <br>[Documentation](https://square.github.io/okhttp/recipes/)|
|![Okio Logo](./okio.png)  <br>**[Okio](https://square.github.io/okio/)**  <br>Streamlined I/O library for efficient data processing.  <br>[Documentation](https://square.github.io/okio/recipes/)|![CloudBurst Logo](./cloudburst.png)  <br>**[CloudBurst](https://cloudburstmc.org/)**  <br>Minecraft Bedrock server software for seamless interoperability.  <br>[Documentation](https://github.com/CloudburstMC)|![JetBrains Logo](./images/jetbrains.png)  <br>**[JetBrains](https://www.jetbrains.com/)**  <br>Powerful IDEs for streamlined development.  <br>[Documentation](https://www.jetbrains.com/help/)|
|![Netlify Logo](./netlify.png)  <br>**[Netlify](https://www.netlify.com/)**  <br>Hosting platform for our documentation and web presence.  <br>[Documentation](https://docs.netlify.com/)|![Android Logo](./images/android.jpg)  <br>**[Android](https://developer.android.com/)**  <br>Mobile OS for native app deployment and features.  <br>[Documentation](https://developer.android.com/docs)|![AndroidX Logo](./material3.png)  <br>**[Material3](https://m3.material.io/)**  <br>Libraries for modern Android UI development.  <br>[Documentation](https://m3.material.io/)|

Additional dependencies include:

- **[LevelDB](https://github.com/google/leveldb)**: Fast key-value storage for efficient data management.
- **[AndroidX Compose Material](https://developer.android.com/jetpack/androidx/releases/compose-material)**: Material Design components for building modern, responsive UI with Jetpack Compose.
- **[AndroidX Foundation](https://developer.android.com/jetpack/androidx/releases/foundation)**: Core utilities for Android app development, enhancing compatibility and functionality.
- **[AndroidX Window](https://developer.android.com/jetpack/androidx/releases/window)**: Support for advanced window management, such as foldable device layouts.
- **[Log4j](https://logging.apache.org/log4j/2.x/)**: Flexible logging framework for debugging and monitoring (used in debug builds).
- **[Netty](https://netty.io/)**: High-performance network framework for efficient packet handling in Minecraft Bedrock networking.
- **[ExpiringMap](https://github.com/jhalterman/expiringmap)**: Caching library with automatic expiration for optimized performance.
- **[Fastutil](https://fastutil.di.unimi.it/)**: High-performance collections for optimized data handling (long, int, and object maps).
- **[Jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home)**: JWT support for secure authentication in network operations.
- **[CloudburstMC Math](https://github.com/CloudburstMC/math)**: Precise mathematical utilities for combat and movement calculations.
- **[CloudburstMC NBT](https://github.com/CloudburstMC/NBT)**: Handles Minecraft-specific NBT data formats for item and world data.
- **[Snappy](https://github.com/airlift/aircompressor)**: Data compression library for efficient storage and transfer.
- **[Google Guava](https://github.com/google/guava)**: General-purpose utilities for robust development.
- **[Gson](https://github.com/google/gson)**: JSON parsing and serialization for data exchange.
- **[Apache HttpClient](https://hc.apache.org/httpcomponents-client-ga/)**: Advanced HTTP functionality for network operations.
- **[Bouncycastle](https://www.bouncycastle.org/)**: Cryptographic library for secure encryption and authentication.
- **[OkHttp](https://square.github.io/okhttp/)**: Robust HTTP client for reliable network requests.
- **[Amplitude Analytics](https://www.amplitude.com/)**: Analytics SDK for tracking user behavior and app performance.
- **[Compose Colorful Sliders](https://github.com/SmartToolFactory/Compose-Colorful-Sliders)**: Custom UI components for interactive sliders in Jetpack Compose.
- **[Coil Compose](https://coil-kt.github.io/coil/compose/)**: Image loading library for efficient image handling in Jetpack Compose.
- **[AnimatedUX](https://github.com/TheProjectLumina/AnimatedUX)**: Custom animation library for dynamic UI transitions (project dependency).
- **[Pixie](https://github.com/TheProjectLumina/Lumina-v4-dev/tree/main/Pixie)**: ImGui setup for Android using native surface rendering for optimized graphics (project dependency).
- **[Lunaris](https://github.com/TheProjectLumina/Lumina-v4-dev/tree/main/Lunaris)**: Custom utility library for Lumina’s core functionality (project dependency).
- **[SSC](https://github.com/TheProjectLumina/Lumina-v4-dev/tree/main/SSC)**: Custom Utility Dependency (project dependency).
- **[Android Native Surface](https://github.com/SsageParuders/Android_Native_Surface)**: Imgui And Native Rendering.
- **[Sign Verification](https://github.com/aizuzi/SignatureVerificationDemo)**: Signature Verification And integrity Check
- **[Kotlinx Serialization JSON](https://github.com/Kotlin/kotlinx.serialization)**: JSON serialization for Kotlin-based data handling.
- **[AndroidX Core KTX](https://developer.android.com/jetpack/androidx/releases/core)**: Kotlin extensions for Android core APIs.
- **[AndroidX Lifecycle Runtime KTX](https://developer.android.com/jetpack/androidx/releases/lifecycle)**: Lifecycle-aware components for reactive programming.
- **[AndroidX Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity)**: Support for Jetpack Compose in Android activities.
- **[AndroidX Compose](https://developer.android.com/jetpack/compose)**: Modern Android UI toolkit for building Lumina’s interface.
- **[AndroidX UI Graphics](https://developer.android.com/jetpack/androidx/releases/compose-ui)**: Graphics utilities for Compose-based UI rendering.
- **[AndroidX UI Tooling Preview](https://developer.android.com/jetpack/androidx/releases/compose-ui)**: Preview tools for Compose UI development.
- **[AndroidX Material3](https://developer.android.com/jetpack/androidx/releases/compose-material3)**: Material You components for modern, adaptive UI design.
- **[AndroidX Navigation Compose](https://developer.android.com/jetpack/androidx/releases/navigation)**: Navigation framework for Compose-based apps.
- **[AndroidX Material Icons Extended](https://developer.android.com/jetpack/androidx/releases/core)**: Extended icon set for Material Design UI.
- **[Firebase BOM](https://firebase.google.com/docs/android/setup)**: Bill of Materials for Firebase dependencies, ensuring version compatibility.
- **[Jackson Databind](https://github.com/FasterXML/jackson-databind)**: Advanced JSON data binding for complex data structures.
- **[Firebase Analytics](https://firebase.google.com/docs/analytics)**: Analytics for tracking user engagement and app performance.
- **[Firebase Crashlytics](https://firebase.google.com/docs/crashlytics)**: Crash reporting for identifying and resolving app issues.
- **[JUnit](https://junit.org/junit4/)**: Testing framework for unit tests.
- **[AndroidX JUnit](https://developer.android.com/jetpack/androidx/releases/test)**: Android-specific testing extensions for JUnit.
- **[AndroidX Espresso Core](https://developer.android.com/training/testing/espresso)**: UI testing framework for Android apps.
- **[AndroidX UI Test JUnit4](https://developer.android.com/jetpack/androidx/releases/test)**: Compose-specific UI testing framework.
- **[AndroidX UI Tooling](https://developer.android.com/jetpack/androidx/releases/compose-ui)**: Debugging and inspection tools for Compose UI (debug builds).
- **[AndroidX UI Test Manifest](https://developer.android.com/jetpack/androidx/releases/test)**: Manifest utilities for UI testing (debug builds).

We also acknowledge [HavensGrace Studios](https://github.com/HavensGrace) by @aiko and @aoi for development and Technical Support Without them this project may not have been possible ❤️

---

## License

Lumina Client Version 4 is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html). Earlier versions have separate freeware licenses; please review them if applicable. Key license terms:

- You may use, modify, and distribute Lumina, provided the source code is shared under the same GPL license.
- Derivative works must remain open-source and cannot impose additional restrictions.
- The software is provided "as is" without warranties, and the authors are not liable for any damages or misuse.

For complete details, see the [LICENSE](https://github.com/TheProjectLumina/LuminaClient/blob/main/LICENSE) and [NOTICE](https://github.com/TheProjectLumina/LuminaClient/blob/main/NOTICE) files.

---

## Final Thoughts

Lumina Client is a form of love from the Project Lumina team, built for Minecraft Bedrock community. We’re excited to see how you use, customize, and contribute to Lumina. Join us on [Discord](https://discord.gg/78bqDpAHmK) or [YouTube](https://youtube.com/@prlumina) to stay connected, share your creations, and help shape the future of Lumina!