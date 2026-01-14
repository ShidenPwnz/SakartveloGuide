package com.example.sakartveloguide.data.repository

import android.util.Log
import com.example.sakartveloguide.data.local.dao.TripDao
import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.data.mapper.toDomain
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val dao: TripDao
) : TripRepository {

    override fun getAvailableTrips(): Flow<List<TripPath>> {
        return dao.getAllTrips().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTripById(id: String): TripPath? {
        return dao.getTripById(id)?.toDomain()
    }

    override suspend fun lockTrip(tripId: String) {
        dao.updateLockStatus(tripId, true)
    }

    // ARCHITECT'S FIX: Fulfilling the abstract member requirement
    override suspend fun nukeAllData() {
        dao.nukeTable()
    }

    override suspend fun refreshTrips() {
        try {
            val existingList = dao.getAllTrips().first()
            val isCorrupt = existingList.any { it.durationDays == 0 }

            if (existingList.isNotEmpty() && !isCorrupt) return

            if (isCorrupt) {
                Log.w("SAKARTVELO", "⚠️ CORRUPT DATA DETECTED. WIPING...")
                dao.nukeTable()
            }

            val tripsToInsert = mutableListOf<TripEntity>()
                // --- META SECTOR: SYSTEM & COUNTRY ---
            tripsToInsert += TripEntity(
                id = "meta_tutorial",
                title = "SYSTEM TUTORIAL",
                description = "Operational briefing: How to navigate the Matrix, secure tactical assets, and execute the Battle Plan.",
                imageUrl = "https://images.pexels.com/photos/7319307/pexels-photo-7319307.jpeg", // Tactical tech image
                category = "GUIDE",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 0,
                durationDays = 0,
                route = emptyList(),
                itinerary = emptyList()
            )

            tripsToInsert += TripEntity(
                id = "meta_about",
                title = "ABOUT SAKARTVELO",
                description = "Essential Intelligence: Currency (GEL), Safety protocols, Emergency frequencies, and Cultural norms.",
                imageUrl = "https://images.pexels.com/photos/15878368/pexels-photo-15878368.jpeg", // Georgian flag or iconic landmark
                category = "GUIDE",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 0,
                durationDays = 0,
                route = emptyList(),
                itinerary = emptyList()
            )
            // ==================================================================================
            // ... (Previous imports and class setup remain the same)

            // ===================================================================================
            // THEME 1: CAPITAL (TBILISI) - DEEP HISTORY & ARISTOCRACY
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "tbilisi_deep_history_4d",
                title = "Tbilisi: Silk Road Aristocrat",
                description = "Peel back the Soviet layers to reveal the Tiflis of the 19th century. Explore Persian citadels, Sololaki mansions, and the Golden Treasury.",
                imageUrl = "https://images.pexels.com/photos/14894364/pexels-photo-14894364.jpeg?auto=compress&cs=tinysrgb&w=1200",
                category = "CULTURE",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 60,
                durationDays = 4,
                route = listOf(GeoPoint(41.6925, 44.7985), GeoPoint(41.6880, 44.8085)),
                itinerary = listOf(
                    // DAY 1: The Bourgeoisie of Sololaki
                    BattleNode(
                        "Writer’s House",
                        "Day 1 10:00: Art Nouveau mansion of brandy tycoon Sarajishvili.",
                        "D1 10:00",
                        location = GeoPoint(41.6896, 44.8010)
                    ),
                    BattleNode(
                        "Kalantarov House",
                        "Day 1 11:30: Pseudo-Moorish architectural gem on Machabeli St.",
                        "D1 11:30",
                        location = GeoPoint(41.6894, 44.8015)
                    ),
                    BattleNode(
                        "Cafe Littera",
                        "Day 1 13:00: Lunch in the Writer's House garden. Modern Georgian cuisine.",
                        "D1 13:00",
                        location = GeoPoint(41.6896, 44.8010)
                    ),
                    BattleNode(
                        "Galaktioni 22",
                        "Day 1 15:00: Explore the painted entrance halls (Sadarbazo).",
                        "D1 15:00",
                        location = GeoPoint(41.6910, 44.7995)
                    ),
                    BattleNode(
                        "Gudaishvili Square",
                        "Day 1 16:30: The oldest preserved square in the district.",
                        "D1 16:30",
                        location = GeoPoint(41.6935, 44.8005)
                    ),
                    BattleNode(
                        "Ezo Restaurant",
                        "Day 1 19:30: Dinner in a traditional 'Italian Courtyard' setting.",
                        "D1 19:30",
                        location = GeoPoint(41.6905, 44.7990)
                    ),

                    // DAY 2: Persian Roots & Sulfur
                    BattleNode(
                        "Narikala Fortress",
                        "Day 2 09:00: 4th-century citadel overlooking the city.",
                        "D2 09:00",
                        alertType = "STEEP_HIKE",
                        location = GeoPoint(41.6880, 44.8085)
                    ),
                    BattleNode(
                        "Botanical Garden",
                        "Day 2 11:00: Walk to the hidden waterfall behind the fortress.",
                        "D2 11:00",
                        location = GeoPoint(41.6870, 44.8060)
                    ),
                    BattleNode(
                        "Culinarium Khasheria",
                        "Day 2 13:00: Famous for hangover-curing Khashi soup.",
                        "D2 13:00",
                        location = GeoPoint(41.6885, 44.8105)
                    ),
                    BattleNode(
                        "Orbeliani Baths",
                        "Day 2 14:30: The 'Blue Bath' favored by Pushkin. Scrub is mandatory.",
                        "D2 14:30",
                        alertType = "RESERVATION_NEEDED",
                        location = GeoPoint(41.6883, 44.8109)
                    ),
                    BattleNode(
                        "Jumah Mosque",
                        "Day 2 17:00: Unique mosque where Sunnis and Shias pray together.",
                        "D2 17:00",
                        location = GeoPoint(41.6875, 44.8115)
                    ),
                    BattleNode(
                        "Gabriadze Theater",
                        "Day 2 19:00: Watch the angel strike the bell at the leaning clock tower.",
                        "D2 19:00",
                        location = GeoPoint(41.6958, 44.8065)
                    ),

                    // DAY 3: National Treasures
                    BattleNode(
                        "National Museum",
                        "Day 3 10:00: See the Golden Fleece gold jewelry in the treasury.",
                        "D3 10:00",
                        location = GeoPoint(41.6963, 44.8002)
                    ),
                    BattleNode(
                        "Blue Gallery",
                        "Day 3 12:30: Works of naive painter Niko Pirosmani.",
                        "D3 12:30",
                        location = GeoPoint(41.6970, 44.7990)
                    ),
                    BattleNode(
                        "Barbarestan",
                        "Day 3 14:00: Lunch based on a 19th-century duchess's cookbook.",
                        "D3 14:00",
                        alertType = "BOOK_AHEAD",
                        location = GeoPoint(41.7105, 44.7960)
                    ),
                    BattleNode(
                        "Dry Bridge Market",
                        "Day 3 16:00: Antiques, Soviet medals, and vintage cameras.",
                        "D3 16:00",
                        location = GeoPoint(41.7005, 44.8055)
                    ),
                    BattleNode(
                        "Mtatsminda Funicular",
                        "Day 3 18:30: Ride the train to the mountain top for sunset.",
                        "D3 18:30",
                        location = GeoPoint(41.6948, 44.7845) // Lower station
                    ),
                    BattleNode(
                        "Funicular Restaurant",
                        "Day 3 20:00: Legendary Ponchiki (donuts) with a view of the city lights.",
                        "D3 20:00",
                        location = GeoPoint(41.6948, 44.7800) // Upper station
                    )
                )
            )

            // ===================================================================================
            // THEME 2: CAPITAL (TBILISI) - BOHEMIAN & BRUTALIST
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "tbilisi_bohemian_3d",
                title = "Tbilisi: Concrete & Techno",
                description = "For the urbex explorer and night owl. Soviet Brutalism, street art factories, and the world-famous Bassiani.",
                imageUrl = "https://images.pexels.com/photos/6253995/pexels-photo-6253995.jpeg?auto=compress&cs=tinysrgb&w=1200",
                category = "URBAN_EXPLORER",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 90,
                durationDays = 3,
                route = listOf(GeoPoint(41.7355, 44.7708), GeoPoint(41.7095, 44.8028)),
                itinerary = listOf(
                    // DAY 1: Concrete Utopias
                    BattleNode(
                        "Bank of Georgia HQ",
                        "Day 1 10:00: The 'Tetris Building'. Icon of Soviet Brutalism.",
                        "D1 10:00",
                        location = GeoPoint(41.7355, 44.7708)
                    ),
                    BattleNode(
                        "Nutsubidze Skybridge",
                        "Day 1 11:30: Three towers connected by high-altitude bridges.",
                        "D1 11:30",
                        alertType = "ELEVATOR_COIN",
                        location = GeoPoint(41.7270, 44.7350)
                    ),
                    BattleNode(
                        "Mapshalia",
                        "Day 1 13:30: Authentic Soviet-style canteen. Cheap and delicious.",
                        "D1 13:30",
                        location = GeoPoint(41.7085, 44.7965)
                    ),
                    BattleNode(
                        "Archaeology Museum",
                        "Day 1 15:00: Abandoned majestic structure with a massive bas-relief.",
                        "D1 15:00",
                        location = GeoPoint(41.7750, 44.7670)
                    ),
                    BattleNode(
                        "Chronicles of Georgia",
                        "Day 1 17:00: Massive monument dubbed 'The Georgian Stonehenge'.",
                        "D1 17:00",
                        location = GeoPoint(41.7705, 44.8105)
                    ),
                    BattleNode(
                        "Stamba Hotel",
                        "Day 1 20:00: Dinner in a converted Soviet publishing house.",
                        "D1 20:00",
                        location = GeoPoint(41.7060, 44.7865)
                    ),

                    // DAY 2: Hipster Hubs
                    BattleNode(
                        "Fabrika",
                        "Day 2 11:00: Old sewing factory turned creative city. Street art & coffee.",
                        "D2 11:00",
                        location = GeoPoint(41.7095, 44.8028)
                    ),
                    BattleNode(
                        "Shavi Lomi",
                        "Day 2 13:30: 'Black Lion'. The birthplace of modern Georgian fusion.",
                        "D2 13:30",
                        location = GeoPoint(41.7115, 44.8040)
                    ),
                    BattleNode(
                        "Vodkast Records",
                        "Day 2 15:30: Vinyl digging and electronic music culture.",
                        "D2 15:30",
                        location = GeoPoint(41.7045, 44.7900) // Approx Vera location
                    ),
                    BattleNode(
                        "Wine Factory N1",
                        "Day 2 18:00: Pre-drinks in a historic wine factory complex.",
                        "D2 18:00",
                        location = GeoPoint(41.7065, 44.7830)
                    ),
                    BattleNode(
                        "Bassiani / Khidi",
                        "Day 2 23:55: The Techno Cathedral under the stadium. Strict face control.",
                        "D2 23:55",
                        alertType = "FACE_CONTROL",
                        location = GeoPoint(41.7230, 44.7897)
                    ),

                    // DAY 3: Hidden Mosaics
                    BattleNode(
                        "Palace of Rituals",
                        "Day 3 12:00: Post-modern Soviet wedding palace.",
                        "D3 12:00",
                        location = GeoPoint(41.6780, 44.8320)
                    ),
                    BattleNode(
                        "Expo Georgia",
                        "Day 3 14:00: Hunt for Soviet-era mosaics in the pavilions.",
                        "D3 14:00",
                        location = GeoPoint(41.7380, 44.7810)
                    ),
                    BattleNode(
                        "Dezerter Bazaar",
                        "Day 3 16:00: Raw, chaotic central market. Photographer's paradise.",
                        "D3 16:00",
                        location = GeoPoint(41.7210, 44.7930)
                    ),
                    BattleNode(
                        "Stalin's Printing Press",
                        "Day 3 17:30: Secret underground bunker used by young Bolsheviks.",
                        "D3 17:30",
                        location = GeoPoint(41.6850, 44.8250)
                    ),
                    BattleNode(
                        "Bina N37",
                        "Day 3 20:00: Rooftop apartment restaurant with Qvevris on the balcony.",
                        "D3 20:00",
                        location = GeoPoint(41.7180, 44.7610)
                    )
                )
            )

            // ===================================================================================
            // THEME 3: KAKHETI - WINE & KINGS
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "kakheti_royal_2d",
                title = "Kakheti: The 8000 Vintage",
                description = "Visit the Cradle of Wine. From 8,000-year-old Qvevri traditions to 19th-century princely estates.",
                imageUrl = "https://images.pexels.com/photos/5225000/pexels-photo-5225000.jpeg?auto=compress&cs=tinysrgb&w=1200",
                category = "WINE_REGION",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 240,
                durationDays = 2,
                route = listOf(GeoPoint(41.6205, 45.9255), GeoPoint(41.8965, 45.5680)),
                itinerary = listOf(
                    // DAY 1: Signagi & Natural Wine
                    BattleNode(
                        "Badiauri",
                        "Day 1 09:30: Stop for hot Shoti bread and cheese on the highway.",
                        "D1 09:30",
                        location = GeoPoint(41.6660, 45.4330)
                    ),
                    BattleNode(
                        "Bodbe Monastery",
                        "Day 1 11:00: Burial place of St. Nino with valley views.",
                        "D1 11:00",
                        alertType = "DRESS_CODE",
                        location = GeoPoint(41.6068, 45.9328)
                    ),
                    BattleNode(
                        "Pheasant’s Tears",
                        "Day 1 13:00: Lunch. The winery that revived the Qvevri tradition.",
                        "D1 13:00",
                        location = GeoPoint(41.6195, 45.9220)
                    ),
                    BattleNode(
                        "Signagi Walls",
                        "Day 1 15:30: Walk the 18th-century fortifications.",
                        "D1 15:30",
                        location = GeoPoint(41.6210, 45.9250)
                    ),
                    BattleNode(
                        "Vakho’s Winery",
                        "Day 1 17:30: Tasting amber wine directly from the clay pot.",
                        "D1 17:30",
                        location = GeoPoint(41.6180, 45.9240)
                    ),
                    BattleNode(
                        "The Terrace Signagi",
                        "Day 1 20:00: Dinner overlooking the Alazani Valley.",
                        "D1 20:00",
                        location = GeoPoint(41.6200, 45.9210)
                    ),

                    // DAY 2: The Great Estates
                    BattleNode(
                        "Tsinandali Estate",
                        "Day 2 10:30: Prince Chavchavadze's palace and gardens.",
                        "D2 10:30",
                        location = GeoPoint(41.8965, 45.5680)
                    ),
                    BattleNode(
                        "Vazisubani Estate",
                        "Day 2 13:00: Lunch at a restored noble mansion.",
                        "D2 13:00",
                        location = GeoPoint(41.8380, 45.7100)
                    ),
                    BattleNode(
                        "Alaverdi Monastery",
                        "Day 2 15:00: 11th-century cathedral and monastic wine cellar.",
                        "D2 15:00",
                        location = GeoPoint(42.0325, 45.3770)
                    ),
                    BattleNode(
                        "Khareba Tunnel",
                        "Day 2 16:30: 7km wine tunnel carved into the rock.",
                        "D2 16:30",
                        alertType = "COLD_INSIDE",
                        location = GeoPoint(41.9485, 45.8340)
                    ),
                    BattleNode(
                        "Kindzmarauli Corp",
                        "Day 2 18:00: Industrial wine tour in Kvareli.",
                        "D2 18:00",
                        location = GeoPoint(41.9470, 45.8130)
                    ),
                    BattleNode(
                        "Kapiloni",
                        "Day 2 20:00: Best Mtsvadi (BBQ) in Telavi.",
                        "D2 20:00",
                        location = GeoPoint(41.9180, 45.4740)
                    )
                )
            )

            // ===================================================================================
            // THEME 4: KAZBEGI - ALPINE ADVENTURE
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "kazbegi_explorer_2d",
                title = "Kazbegi: The Sky Piercer",
                description = "Drive the legendary Military Highway to the foot of Mt. Kazbek (5,047m). Glaciers, giants, and dumplings.",
                imageUrl = "https://images.pexels.com/photos/13440788/pexels-photo-13440788.jpeg?auto=compress&cs=tinysrgb&w=1200",
                category = "MOUNTAIN",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 200,
                durationDays = 2,
                hasSnowWarning = true,
                route = listOf(GeoPoint(42.6605, 44.6430)),
                itinerary = listOf(
                    // DAY 1: The Highway
                    BattleNode(
                        "Ananuri Fortress",
                        "Day 1 10:00: Medieval castle complex on the Aragvi River.",
                        "D1 10:00",
                        location = GeoPoint(42.1636, 44.7030)
                    ),
                    BattleNode(
                        "Friendship Monument",
                        "Day 1 12:00: Soviet mosaic balcony over the Devil's Valley.",
                        "D1 12:00",
                        location = GeoPoint(42.4862, 44.4542)
                    ),
                    BattleNode(
                        "Rooms Hotel",
                        "Day 1 13:30: Lunch on the famous terrace facing the mountain.",
                        "D1 13:30",
                        location = GeoPoint(42.6600, 44.6430)
                    ),
                    BattleNode(
                        "Gergeti Trinity",
                        "Day 1 15:30: The iconic church at 2,170m. 4x4 or hike.",
                        "D1 15:30",
                        alertType = "4X4_ONLY",
                        location = GeoPoint(42.6625, 44.6200)
                    ),
                    BattleNode(
                        "Gveleti Waterfall",
                        "Day 1 17:30: Short hike to a powerful waterfall near the border.",
                        "D1 17:30",
                        location = GeoPoint(42.7050, 44.6150)
                    ),
                    BattleNode(
                        "Shorena's",
                        "Day 1 20:00: Hearty mountain dinner. Try the Khinkali.",
                        "D1 20:00",
                        location = GeoPoint(42.6570, 44.6410)
                    ),

                    // DAY 2: The Dolomites of Georgia
                    BattleNode(
                        "Sno Heads",
                        "Day 2 09:30: Giant granite heads of poets carved in an open field.",
                        "D2 09:30",
                        location = GeoPoint(42.6050, 44.6350)
                    ),
                    BattleNode(
                        "Juta Valley",
                        "Day 2 11:00: Hike towards the Chaukhi Massif (Georgian Dolomites).",
                        "D2 11:00",
                        alertType = "HIKING_SHOES",
                        location = GeoPoint(42.5790, 44.7450)
                    ),
                    BattleNode(
                        "Fifth Season Hut",
                        "Day 2 13:00: Lunch with an alpine view in Juta.",
                        "D2 13:00",
                        location = GeoPoint(42.5720, 44.7500)
                    ),
                    BattleNode(
                        "Dariali Monastery",
                        "Day 2 16:00: Massive complex right on the Russian border.",
                        "D2 16:00",
                        location = GeoPoint(42.7420, 44.6290)
                    ),
                    BattleNode(
                        "Tsdo Village",
                        "Day 2 17:00: Semi-abandoned village with animist shrines.",
                        "D2 17:00",
                        location = GeoPoint(42.7010, 44.6300)
                    ),
                    BattleNode(
                        "Pasanauri",
                        "Day 2 19:30: Stop for Khinkali in their birthplace on the way back.",
                        "D2 19:30",
                        location = GeoPoint(42.3520, 44.6880)
                    )
                )
            )


            // ===================================================================================
            // WEST GEORGIA - COLCHIS LOOP
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "west_colchis_3d",
                title = "West: Canyons & Ruins",
                description = "Discover the land of the Golden Fleece. Abandoned Soviet spas, emerald canyons, and spicy food.",
                imageUrl = "https://images.pexels.com/photos/356269/pexels-photo-356269.jpeg?auto=compress&cs=tinysrgb&w=1200",
                category = "NATURE",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 180,
                durationDays = 3,
                route = listOf(GeoPoint(42.2770, 42.7040), GeoPoint(42.4570, 42.3770)),
                itinerary = listOf(
                    // DAY 1: The Abandoned Spas
                    BattleNode(
                        "Sanatorium Medea",
                        "Day 1 10:00: Tskaltubo. Romanesque columns and decay.",
                        "D1 10:00",
                        location = GeoPoint(42.3250, 42.6000)
                    ),
                    BattleNode(
                        "Stalin's Bath",
                        "Day 1 11:30: Bathhouse No. 6. See the dictator's private pool.",
                        "D1 11:30",
                        location = GeoPoint(42.3280, 42.5970)
                    ),
                    BattleNode(
                        "Magnolia",
                        "Day 1 13:00: Lunch in Tskaltubo.",
                        "D1 13:00",
                        location = GeoPoint(42.3260, 42.5980)
                    ),
                    BattleNode(
                        "Prometheus Cave",
                        "Day 1 15:00: Underground boat ride and stalactites.",
                        "D1 15:00",
                        location = GeoPoint(42.3765, 42.6005)
                    ),
                    BattleNode(
                        "Sisters",
                        "Day 1 20:00: Dinner in Kutaisi. Live piano and Imeretian wine.",
                        "D1 20:00",
                        location = GeoPoint(42.2710, 42.7030)
                    ),

                    // DAY 2: Canyons
                    BattleNode(
                        "Martvili Canyon",
                        "Day 2 10:00: Emerald waters. Former bathing place of nobles.",
                        "D2 10:00",
                        alertType = "BOAT_RIDE",
                        location = GeoPoint(42.4570, 42.3770)
                    ),
                    BattleNode(
                        "Oda Family Marani",
                        "Day 2 13:00: Lunch. Authentic Megrelian cuisine (Spicy!).",
                        "D2 13:00",
                        location = GeoPoint(42.4100, 42.3700)
                    ),
                    BattleNode(
                        "Okatse Canyon",
                        "Day 2 15:30: Hanging walkway over the abyss.",
                        "D2 15:30",
                        location = GeoPoint(42.4550, 42.5500)
                    ),
                    BattleNode(
                        "Kinchkha Waterfall",
                        "Day 2 17:00: Massive waterfall near Okatse.",
                        "D2 17:00",
                        location = GeoPoint(42.4950, 42.5530)
                    ),
                    BattleNode(
                        "Bikentia's Kebabery",
                        "Day 2 20:00: Cult spot in Kutaisi. Kebabs and lemonade only.",
                        "D2 20:00",
                        location = GeoPoint(42.2720, 42.7050)
                    ),

                    // DAY 3: Batumi Transfer
                    BattleNode(
                        "Bagrati Cathedral",
                        "Day 3 09:00: 11th-century symbol of united Georgia.",
                        "D3 09:00",
                        location = GeoPoint(42.2770, 42.7040)
                    ),
                    BattleNode(
                        "Botanical Garden",
                        "Day 3 14:00: Arrival in Batumi. Massive cliffside gardens.",
                        "D3 14:00",
                        location = GeoPoint(41.6930, 41.7070)
                    ),
                    BattleNode(
                        "Fish Market",
                        "Day 3 17:00: Buy raw fish, get it fried next door.",
                        "D3 17:00",
                        location = GeoPoint(41.6490, 41.6620)
                    ),
                    BattleNode(
                        "Ali & Nino",
                        "Day 3 19:00: Moving steel statues of lovers.",
                        "D3 19:00",
                        location = GeoPoint(41.6555, 41.6430)
                    ),
                    BattleNode(
                        "Retro",
                        "Day 3 20:30: The best Adjaruli Khachapuri (Boat shape) in town.",
                        "D3 20:30",
                        location = GeoPoint(41.6510, 41.6360)
                    )
                )
            )

            dao.insertTrips(tripsToInsert)
            Log.d("SAKARTVELO", "Seeding successful.")

        } catch (e: Exception) {
            Log.e("SAKARTVELO", "Seeding error: ${e.message}")
        }
    }
}