# Aura Music 🎵✨

Aura Music is a modern, high-fidelity, **100% free music streaming platform** built natively for Android using Kotlin and Jetpack Compose. Designed with a provider-independent architecture, Aura Music offers edge-to-edge glassmorphic design, offline listening, AI-powered music curation, dynamic audio playback, interactive lyrics, and deep music insights.

---

## 🚀 Major Features

### 🎧 High-Fidelity Audio Engine
- **Media3 ExoPlayer Integration**: Gapless playback with background audio support and media notifications.
- **Smart Queue Management**: Reorder, shuffle, repeat, and dynamic AI auto-queuing.
- **Equalizer & Audio FX**: Custom equalizer presets and bass boost options.
- **Live Lyrics & Sync**: Real-time timed lyrics display with auto-scroll and synchronization.

### 🤖 Aura AI DJ & Music Companion
- **Natural Language Prompting**: Generate custom mood playlists, request track recommendations, or filter music by vibe.
- **Smart Queue & Mood Engine**: 14 distinct emotional moods (Calm, Energy, Workout, Study, Rainy Day, Night Drive, etc.).
- **Live AI DJ Host**: Dynamic contextual voice/speech announcements between tracks.
- **Listening Insights & Persona**: AI-generated reports on streaming habits, peak listening hours, and musical personality.
- **Offline Fallback Guarantee**: Intelligent local rule provider ensuring 100% functionality even without an active internet connection or API key.

### 📚 Library & Offline Downloads
- **Room Local Persistence**: Instant local caching for liked tracks, custom playlists, listening history, and user preferences.
- **Offline Listening**: Track download manager supporting full offline playback without data connectivity.
- **Custom Playlists**: Create, edit, and organize personalized playlists.

### 🎨 Glassmorphic Material 3 Design
- **Edge-to-Edge UI**: Fluid layouts with customizable color themes (Aura Neon, Cyberpunk Dark, Ethereal Light, Sunset Glow).
- **Responsive Layouts**: Fully responsive design supporting phones, foldables, tablets, and landscape orientations.
- **Accessibility Ready**: Scalable text, high-contrast indicators, and touch targets meeting 48dp minimums.

---

## 🛠️ Tech Stack & Architecture

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Audio Playback**: AndroidX Media3 (ExoPlayer & MediaSession)
- **Database & Persistence**: Room Database + Coroutines Flow
- **AI Engine**: Gemini REST API + Local Rule-Based Fallback Engine
- **Image Caching**: Coil Compose
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 📦 How to Build & Run

### Prerequisites
- Android Studio Ladybug or later
- JDK 17
- Android SDK 36 (Min SDK 24)

### Building Debug APK
```bash
# Clone the repository
git clone https://github.com/your-username/aura-music.git
cd aura-music

# Build Debug APK
./gradlew assembleDebug
```
The compiled APK will be available at:
`app/build/outputs/apk/debug/app-debug.apk`

### Optional: Gemini AI API Key Setup
To enable cloud-powered Gemini AI features:
1. Open or create `.env` in the project root.
2. Add your Gemini API key:
   ```env
   GEMINI_API_KEY=your_actual_api_key_here
   ```
*(Note: If left blank, the app seamlessly falls back to the built-in offline Local Rule AI Engine).*

---

## 📄 License & Pricing
Aura Music is **100% Free** for all users. No subscriptions, no ads, no paywalls, and no hidden tiers.
