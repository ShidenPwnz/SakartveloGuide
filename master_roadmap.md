🗺️ The Master Roadmap: Sakartvelo Guide

🏗️ Phase 0: The Iron Foundation (Weeks 1-2) — [COMPLETED]
Goal: Scalable infrastructure and Brand Identity.
Architecture: Clean Architecture (Domain, Data, Presentation) with Hilt.
Modern Kartli Design System: Brand tokens (Sakartvelo Red, Wine Dark, Snow White).
The Model: Defined TripPath and Waypoint domain entities.

🧠 Phase 1: The Memory Layer (Weeks 3-4) — [COMPLETED]
Goal: Resilience and Offline-First capability.
Persistence: Room DB implementation with Mappers.
State Management: Jetpack DataStore for UserJourneyState (Browsing vs. Locked).
The Repository: Single Source of Truth for trip data.

🎨 Phase 2: The Discovery Experience (Weeks 5-7) — [COMPLETED]
Goal: Visualizing the Decision Engine.
Home UI: Horizontal carousel of Cinematic Trip Cards.
Logistics Dashboard: High-level metrics (Drive time vs. Walk time).
UI State: Implementation of HomeViewModel using StateFlow for reactive UI updates.

🔒 Phase 3: The "Lock" Event (Weeks 8-9) — [COMPLETED]
Goal: The transition from Browser to Traveler.
The Pivot: A "point of no return" UI transition.
Logistics Prep: Dynamic checklist generation (e.g., "Book car for Kazbegi").
State Pivot: DataStore triggers the app to hide "Discovery" and show "Current Trip."

🗺️ Phase 4: The Spatial Engine (Weeks 10-12) — [COMPLETED]
Goal: Navigation without the "Google Tax."
MapLibre Integration: Rendering free OpenStreetMap tiles.
Offline Engine: Bounding-box tile downloader (critical for mountain passes).
Route Overlays: Custom polyline rendering for the selected path.

🧭 Phase 5: The Live Assistant (Weeks 13-15) — [COMPLETED]
Goal: Real-time decision support.
Timeline UI: Vertical stepper showing current progress.
Logic Engine: Proactive alerts (e.g., "Leave in 10 mins to reach the church before sunset").
Sensors: Altitude/Elevation tracking to warn about steep ascents in Tbilisi or the Highlands.

🍷 Phase 6: The Georgian Passport (Weeks 16-17) — [COMPLETED]
Goal: Viral loops and Gamification.
Check-in Logic: GPS-based "Stamp" unlocking.
Passport UI: A skeuomorphic booklet showing regions visited (Imereti, Kakheti, etc.).
Social Engine: Shareable "Path Summaries" for Instagram.

💰 Phase 7: Monetization & Polish (Weeks 18-20) — [CURRENT]
Goal: Turning the app into a business.
Affiliate Engine: Integration with Booking.com/Bolt via deep links.
Premium Tier: "Hidden Gems" routes and full offline map downloads.
App Hardening: Performance profiling and Extreme Logging for crashes.
