# Changelog - Aura Music

All notable changes to the Aura Music project will be documented in this file.

## [1.0.0] - Production Release

### Added
- **Core Architecture**: Provider-independent MVVM + Repository design with Room local database persistence.
- **Player & Media Engine**: Media3 ExoPlayer integration, background service support, lock screen controls, and MediaSession integration.
- **Interactive Lyrics**: Real-time timed lyrics auto-scroll visualizer.
- **Aura AI DJ & Assistant**: Natural language music companion, AI playlist generator, mood flow selector, and contextual DJ host speech.
- **AI Music Insights**: Personal music personality breakdown, discovery score, and peak listening reports.
- **Offline Downloads Manager**: Complete local storage management for downloading and streaming songs offline.
- **Social & Profile Hub**: User profile customization, favorite artists, listening stats, and community sharing feed.
- **Search & Discovery**: NLP search query interpretation with instant mood and genre tag extraction.
- **Custom Themes**: Dynamic palette switches including Aura Neon, Cyberpunk, Ethereal Light, and Sunset Glow.
- **Responsive Layouts**: Seamless compatibility across mobile screens, foldables, tablets, and landscape modes.

### Changed
- Refactored player queue logic for zero-flicker track transitions.
- Optimized image loading and bitmap caching via Coil.
- Enhanced accessibility touch targets across all Compose screens.
