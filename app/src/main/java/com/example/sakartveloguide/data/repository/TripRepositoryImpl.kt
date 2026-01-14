package com.example.sakartveloguide.data.repository

import android.util.Log
import com.example.sakartveloguide.data.local.dao.TripDao
import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.data.mapper.toDomain
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first // ARCHITECT'S FIX: Critical Import
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

    override suspend fun refreshTrips() {
        try {
            // ARCHITECT'S FIX: Wait for the flow to emit the first list
            val existingList = dao.getAllTrips().first()

            // Check if existing data has the "0 Days" bug
            val isCorrupt = existingList.any { it.durationDays == 0 }

            if (existingList.isNotEmpty() && !isCorrupt) return

            if (isCorrupt) {
                Log.w("SAKARTVELO", "⚠️ DETECTED CORRUPT DATA. WIPING...")
                dao.nukeTable()
            }

            val tripsToInsert = mutableListOf<TripEntity>()

            // ==================================================================================
            // REGION I: TBILISI – THE URBAN PALIMPSEST
            // Category: CAPITAL
            // ==================================================================================

            // --- THEME A: THE "BOHEMIAN-BRUTALIST" NEXUS (3 DAYS) ---
            tripsToInsert += TripEntity(
                id = "tbilisi_bohemian_brutalist_3d",
                title = "Tbilisi: Bohemian & Brutalist",
                description = "The aesthetic of decay and regeneration. Soviet Space City architecture, urban exploration, and the techno-political renaissance.",
                imageUrl = "https://images.pexels.com/photos/35563480/pexels-photo-35563480.jpeg?auto=compress&cs=tinysrgb&w=800", // Concrete/Underground vibe
                category = "CAPITAL",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 0,
                durationDays = 3,
                route = listOf(GeoPoint(41.7355, 44.7708), GeoPoint(41.7230, 44.7900)),
                itinerary = listOf(
                    // Day 1: Concrete Utopias
                    BattleNode(
                        "Bank of Georgia HQ",
                        "The crown jewel of Brutalism. 'Space City' architecture with interlocking concrete blocks.",
                        "09:00",
                        "https://www.georgianholidays.com/storage/psp5Oc5qYkcjZjlxOjIHXscwqPiWewCzkCDWZgTe.jpeg1?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Nutsubidze Plato Skybridge",
                        "High-altitude metal footbridge connecting massive Soviet residential blocks.",
                        "11:00",
                        "https://images.pexels.com/photos/2820884/pexels-photo-2820884.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Sasadilo 'Zeche'",
                        "Authentic workers' canteen serving Kharsho and Elarji.",
                        "13:00",
                        "https://images.pexels.com/photos/6253997/pexels-photo-6253997.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "The Chronicles of Georgia",
                        "The 'Georgian Stonehenge'. Massive bronze pillars overlooking the Tbilisi Sea.",
                        "17:00",
                        "https://images.pexels.com/photos/15878368/pexels-photo-15878368.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Stamba Hotel",
                        "Former Soviet publishing house turned luxury creative hub.",
                        "19:30",
                        "https://images.pexels.com/photos/331107/pexels-photo-331107.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 2: Creative Industrial Complex
                    BattleNode(
                        "Fabrika Tbilisi",
                        "Former sewing factory, now a walled city of creative resistance and street art.",
                        "10:00",
                        "https://images.pexels.com/photos/163811/graffiti-wall-art-scrawl-melbourne-163811.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Dry Bridge Flea Market",
                        "Open-air museum of the Soviet collapse. Medals, gas masks, and antiques.",
                        "12:30",
                        "https://images.pexels.com/photos/3642468/pexels-photo-3642468.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Shavi Lomi (Black Lion)",
                        "Pioneering modern Georgian cuisine in a house with a glass balcony.",
                        "14:30",
                        "https://images.pexels.com/photos/6253995/pexels-photo-6253995.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Bassiani / Khidi",
                        "Techno cathedral in a drained stadium pool. Strict face control.",
                        "23:00",
                        "https://images.pexels.com/photos/1190298/pexels-photo-1190298.jpeg?auto=compress&cs=tinysrgb&w=800",
                        "FACE_CONTROL"
                    ),

                    // Day 3: Mosaics & Underground
                    BattleNode(
                        "Palace of Rituals",
                        "Late Soviet postmodernism mimicking a cathedral.",
                        "10:00",
                        "https://images.pexels.com/photos/14894364/pexels-photo-14894364.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Stalin’s Printing Press",
                        "Clandestine press hidden beneath a well shaft.",
                        "14:00",
                        "https://images.pexels.com/photos/2086676/pexels-photo-2086676.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Dezerter Bazaar",
                        "The belly of the city. Chaos, spices, and raw food market.",
                        "18:00",
                        "https://images.pexels.com/photos/15496350/pexels-photo-15496350.jpeg?auto=compress&cs=tinysrgb&w=800"
                    )
                )
            )

            // --- THEME B: THE "SILK ROAD ARISTOCRAT" (3 DAYS) ---
            tripsToInsert += TripEntity(
                id = "tbilisi_silk_road_3d",
                title = "Tbilisi: Silk Road Aristocrat",
                description = "The Tiflis of the 19th century. Sololaki mansions, Persian sulfur baths, and the gastronomic heritage of the bourgeoisie.",
                imageUrl = "https://images.pexels.com/photos/14894363/pexels-photo-14894363.jpeg?auto=compress&cs=tinysrgb&w=800", // Old balconies
                category = "CAPITAL",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 0,
                durationDays = 3,
                route = listOf(GeoPoint(41.6925, 44.7985), GeoPoint(41.6880, 44.8085)),
                itinerary = listOf(
                    // Day 1: Sololaki Mansions
                    BattleNode(
                        "Writer’s House",
                        "Art Nouveau masterpiece with Villeroy & Boch tiles. Formerly Sarajishvili mansion.",
                        "09:30",
                        "https://images.pexels.com/photos/15878368/pexels-photo-15878368.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Kalantarov House",
                        "Pseudo-Moorish interior designed to mimic the Alhambra.",
                        "11:15",
                        "https://images.pexels.com/photos/14894364/pexels-photo-14894364.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Cafe Littera",
                        "Fine dining in the garden of the Writer's House.",
                        "13:00",
                        "https://images.pexels.com/photos/11382415/pexels-photo-11382415.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Ezo",
                        "Organic food in a traditional 'Italian Courtyard'.",
                        "19:30",
                        "https://images.pexels.com/photos/6253997/pexels-photo-6253997.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 2: Persian Core
                    BattleNode(
                        "Narikala Fortress",
                        "Persian citadel dating back to the 4th century.",
                        "09:00",
                        "https://images.pexels.com/photos/15650084/pexels-photo-15650084.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Orbeliani Baths",
                        "Blue-tiled bathhouse where Pushkin bathed. Essential sulfur ritual.",
                        "14:30",
                        "https://images.pexels.com/photos/16335194/pexels-photo-16335194.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Rezo Gabriadze Theater",
                        "Marionette theater with a leaning clock tower.",
                        "19:00",
                        "https://images.pexels.com/photos/14894363/pexels-photo-14894363.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 3: National Treasures
                    BattleNode(
                        "Georgian National Museum",
                        "Archaeological Treasury with Colchian gold.",
                        "10:00",
                        "https://images.pexels.com/photos/11382415/pexels-photo-11382415.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Barbarestan",
                        "Menu based on a 19th-century aristocratic cookbook.",
                        "14:00",
                        "https://images.pexels.com/photos/6253995/pexels-photo-6253995.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Funicular Restaurant",
                        "Stalinist landmark with the definitive view of the city.",
                        "20:00",
                        "https://images.pexels.com/photos/2600336/pexels-photo-2600336.jpeg?auto=compress&cs=tinysrgb&w=800"
                    )
                )
            )

            // ==================================================================================
            // REGION II: KAKHETI – THE CRADLE OF WINE
            // Category: WINE_CELLAR
            // ==================================================================================

            // --- THEME C: THE "QVEVRI HEARTLAND" (2 DAYS) ---
            tripsToInsert += TripEntity(
                id = "kakheti_qvevri_heartland_2d",
                title = "Kakheti: Qvevri Heartland",
                description = "Monastic viniculture and the 8,000-year-old Qvevri tradition. From fortified walls to wine tunnels.",
                imageUrl = "https://images.pexels.com/photos/5225000/pexels-photo-5225000.jpeg?auto=compress&cs=tinysrgb&w=800",
                category = RouteCategory.WINE_CELLAR.name,
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 240,
                durationDays = 2,
                route = listOf(GeoPoint(41.6205, 45.9255), GeoPoint(41.9485, 45.8340)),
                itinerary = listOf(
                    // Day 1: Fortified City
                    BattleNode(
                        "Bodbe Monastery",
                        "Resting place of St. Nino overlooking the Alazani Valley.",
                        "11:00",
                        "https://images.pexels.com/photos/11382415/pexels-photo-11382415.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Pheasant’s Tears",
                        "Natural amber wine and foraged food by John Wurdeman.",
                        "13:00",
                        "https://images.pexels.com/photos/6253997/pexels-photo-6253997.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Signagi City Walls",
                        "18th-century fortifications defending against Lezgin raids.",
                        "15:30",
                        "https://images.pexels.com/photos/13440788/pexels-photo-13440788.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Vakho’s Winery",
                        "Intimate Marani tasting directly from the buried Qvevri.",
                        "17:30",
                        "https://images.pexels.com/photos/331107/pexels-photo-331107.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 2: Great Estates
                    BattleNode(
                        "Tsinandali Estate",
                        "Prince Chavchavadze's palace and historic Enoteca.",
                        "10:00",
                        "https://images.pexels.com/photos/15878368/pexels-photo-15878368.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Alaverdi Monastery",
                        "Monks producing wine here since the 11th century.",
                        "14:30",
                        "https://images.pexels.com/photos/14894363/pexels-photo-14894363.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Khareba Wine Tunnel",
                        "7.7km military tunnel carved into rock, now storing wine.",
                        "16:30",
                        "https://images.pexels.com/photos/2912447/pexels-photo-2912447.jpeg?auto=compress&cs=tinysrgb&w=800"
                    )
                )
            )

            // ==================================================================================
            // REGION III: THE HIGH CAUCASUS – ALPINE FRONTIERS
            // Category: MOUNTAIN
            // ==================================================================================

            // --- THEME D: "SVANETI TOWER TREKKING" (4 DAYS) ---
            tripsToInsert += TripEntity(
                id = "svaneti_trek_4d",
                title = "Svaneti: Tower Trekking",
                description = "The classic medieval adventure. Trekking from Mestia to Ushguli past glaciers and defensive stone towers.",
                imageUrl = "https://images.pexels.com/photos/13440788/pexels-photo-13440788.jpeg?auto=compress&cs=tinysrgb&w=800",
                category = RouteCategory.MOUNTAIN.name,
                difficulty = Difficulty.WARRIOR.name,
                totalRideTimeMinutes = 0,
                durationDays = 4,
                route = listOf(GeoPoint(43.0450, 42.7300), GeoPoint(42.9180, 43.0160)),
                itinerary = listOf(
                    // Day 1: Zhabeshi
                    BattleNode(
                        "Mestia Ethnographic Museum",
                        "Icons and manuscripts hidden from invasions.",
                        "08:00",
                        "https://images.pexels.com/photos/14894364/pexels-photo-14894364.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Zhabeshi Village",
                        "Quiet village with a high concentration of towers.",
                        "16:00",
                        "https://images.pexels.com/photos/13440788/pexels-photo-13440788.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 2: Adishi
                    BattleNode(
                        "Tetnuldi Ski Slope",
                        "Steep ascent towards the ski resort.",
                        "09:00",
                        "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Adishi Village",
                        "Isolated village trapped in time with 11th-century frescoes.",
                        "17:00",
                        "https://images.pexels.com/photos/14894363/pexels-photo-14894363.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 3: The Pass
                    BattleNode(
                        "Adishischala River",
                        "Freezing river crossing on horseback.",
                        "08:30",
                        "https://images.pexels.com/photos/12209749/pexels-photo-12209749.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Chkhunderi Pass",
                        "Best views of the Adishi Glacier at 2,700m.",
                        "11:00",
                        "https://images.pexels.com/photos/6253995/pexels-photo-6253995.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Khalde Village",
                        "Hero Village destroyed in the 19th century.",
                        "14:00",
                        "https://images.pexels.com/photos/15878368/pexels-photo-15878368.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 4: Ushguli
                    BattleNode(
                        "Lamaria Church",
                        "Pagano-Christian church backed by Mt. Shkhara.",
                        "12:00",
                        "https://images.pexels.com/photos/14894364/pexels-photo-14894364.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Chazhashi Towers",
                        "UNESCO site with dense defensive structures.",
                        "17:30",
                        "https://images.pexels.com/photos/13440788/pexels-photo-13440788.jpeg?auto=compress&cs=tinysrgb&w=800"
                    )
                )
            )

            // --- THEME E: "ALPINE LUXURY & MILITARY HISTORY" (KAZBEGI) (2 DAYS) ---
            tripsToInsert += TripEntity(
                id = "kazbegi_luxury_2d",
                title = "Kazbegi: Alpine Luxury",
                description = "The Georgian Military Highway, Rooms Hotel luxury, and the iconic Gergeti Trinity Church.",
                imageUrl = "https://images.pexels.com/photos/6253995/pexels-photo-6253995.jpeg?auto=compress&cs=tinysrgb&w=800",
                category = RouteCategory.MOUNTAIN.name,
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 300,
                durationDays = 2,
                route = listOf(GeoPoint(42.1635, 44.7030), GeoPoint(42.6605, 44.6430)),
                itinerary = listOf(
                    // Day 1: The Highway
                    BattleNode(
                        "Ananuri Fortress",
                        "Fortress overlooking the Aragvi river.",
                        "09:00",
                        "https://images.pexels.com/photos/12209749/pexels-photo-12209749.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Rooms Hotel Kazbegi",
                        "Lunch on the terrace that defined Georgian tourism.",
                        "12:30",
                        "https://images.pexels.com/photos/2600336/pexels-photo-2600336.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Gergeti Trinity Church",
                        "Iconic church silhouetted against the glacier.",
                        "15:00",
                        "https://images.pexels.com/photos/6253995/pexels-photo-6253995.jpeg?auto=compress&cs=tinysrgb&w=800",
                        "ALTITUDE"
                    ),

                    // Day 2: Juta
                    BattleNode(
                        "Sno Heads",
                        "Giant granite heads of poets carved by a local artist.",
                        "09:00",
                        "https://images.pexels.com/photos/14894363/pexels-photo-14894363.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Juta Valley",
                        "The 'Georgian Dolomites'. Hike to Fifth Season hut.",
                        "11:00",
                        "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Dariali Monastery",
                        "Monastery standing sentinel at the Russian border.",
                        "15:00",
                        "https://images.pexels.com/photos/11382415/pexels-photo-11382415.jpeg?auto=compress&cs=tinysrgb&w=800"
                    )
                )
            )

            // --- THEME F: "THE HIGHLAND VINTAGE" (RACHA) (1 DAY) ---
            tripsToInsert += TripEntity(
                id = "racha_vintage_1d",
                title = "Racha: Highland Vintage",
                description = "The 'Switzerland of Georgia'. Shaori Lake, intricately carved cathedrals, and Khvanchkara wine.",
                imageUrl = "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=800", // Green/Alpine
                category = RouteCategory.WINE_CELLAR.name,
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 240,
                durationDays = 1,
                route = listOf(GeoPoint(42.3850, 43.0300), GeoPoint(42.4600, 43.0850)),
                itinerary = listOf(
                    BattleNode(
                        "Nakerala Pass",
                        "9 Crosses viewpoint overlooking the region.",
                        "10:00",
                        "https://images.pexels.com/photos/13440788/pexels-photo-13440788.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Shaori Lake",
                        "Alpine reservoir surrounded by forests.",
                        "12:30",
                        "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Nikortsminda Cathedral",
                        "11th-century cathedral with intricate stone carvings.",
                        "14:00",
                        "https://images.pexels.com/photos/15878368/pexels-photo-15878368.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Royal Khvanchkara",
                        "Tasting of semi-sweet red wine in its micro-zone.",
                        "15:30",
                        "https://images.pexels.com/photos/331107/pexels-photo-331107.jpeg?auto=compress&cs=tinysrgb&w=800"
                    )
                )
            )

            // ==================================================================================
            // REGION IV: THE WEST – COLCHIS AND THE COAST
            // Category: HISTORICAL / COASTAL
            // ==================================================================================

            // --- THEME G: "URBEX & ANTIQUITY" (KUTAISI) (2 DAYS) ---
            tripsToInsert += TripEntity(
                id = "kutaisi_urbex_2d",
                title = "Kutaisi: Urbex & Antiquity",
                description = "Abandoned Soviet spas, emerald canyons, and the myth of the Golden Fleece.",
                imageUrl = "https://images.pexels.com/photos/2242171/pexels-photo-2242171.jpeg?auto=compress&cs=tinysrgb&w=800",
                category = RouteCategory.HISTORICAL.name,
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 240,
                durationDays = 2,
                route = listOf(GeoPoint(42.3250, 42.6000), GeoPoint(42.4570, 42.3770)),
                itinerary = listOf(
                    // Day 1: Tskaltubo
                    BattleNode(
                        "Sanatorium Medea",
                        "Abandoned Romanesque sanatorium.",
                        "09:00",
                        "https://images.pexels.com/photos/14894363/pexels-photo-14894363.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Bathhouse No. 6",
                        "Features a private pool built for Stalin.",
                        "12:00",
                        "https://images.pexels.com/photos/2086676/pexels-photo-2086676.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Prometheus Cave",
                        "Boat ride on an underground river.",
                        "16:00",
                        "https://images.pexels.com/photos/2242171/pexels-photo-2242171.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 2: Canyons
                    BattleNode(
                        "Bagrati Cathedral",
                        "Cathedral overlooking the city.",
                        "09:00",
                        "https://images.pexels.com/photos/15878368/pexels-photo-15878368.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Martvili Canyon",
                        "Emerald waters used by Dadiani nobles.",
                        "11:00",
                        "https://images.pexels.com/photos/9783471/pexels-photo-9783471.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Oda Family Marani",
                        "Megrelian cuisine in a vine-covered family home.",
                        "13:30",
                        "https://images.pexels.com/photos/6253997/pexels-photo-6253997.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Gelati Monastery",
                        "Contains the grave of King David the Builder.",
                        "18:00",
                        "https://images.pexels.com/photos/11382415/pexels-photo-11382415.jpeg?auto=compress&cs=tinysrgb&w=800"
                    )
                )
            )

            // --- THEME H: "SUBTROPICAL GASTRONOMY" (BATUMI) (2 DAYS) ---
            tripsToInsert += TripEntity(
                id = "batumi_gastronomy_2d",
                title = "Batumi: Subtropical Gastronomy",
                description = "Black Sea cuisine, botanical diversity, and the green mountains of Adjara.",
                imageUrl = "https://images.pexels.com/photos/14736528/pexels-photo-14736528.jpeg?auto=compress&cs=tinysrgb&w=800",
                category = RouteCategory.COASTAL.name,
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 300,
                durationDays = 2,
                route = listOf(GeoPoint(41.6930, 41.7070), GeoPoint(41.5750, 41.8600)),
                itinerary = listOf(
                    // Day 1: Fish & Gardens
                    BattleNode(
                        "Batumi Botanical Garden",
                        "Imperial botany legacy at Green Cape.",
                        "09:30",
                        "https://images.pexels.com/photos/14736528/pexels-photo-14736528.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Batumi Fish Market",
                        "Buy raw Red Mullet and have it fried immediately.",
                        "13:00",
                        "https://images.pexels.com/photos/3642468/pexels-photo-3642468.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Ali and Nino Statue",
                        "Moving metal sculpture representing tragic love.",
                        "17:00",
                        "https://images.pexels.com/photos/11424564/pexels-photo-11424564.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Retro",
                        "Famous for Adjaruli Khachapuri with light dough.",
                        "20:30",
                        "https://images.pexels.com/photos/6253997/pexels-photo-6253997.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),

                    // Day 2: Mountains of Adjara
                    BattleNode(
                        "Mtirala National Park",
                        "Subtropical rainforest hike.",
                        "09:00",
                        "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Makhuntseti Waterfall",
                        "Popular waterfall near Queen Tamar bridge.",
                        "12:30",
                        "https://images.pexels.com/photos/9783471/pexels-photo-9783471.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Adjarian Wine House",
                        "Winery in the mountains.",
                        "14:30",
                        "https://images.pexels.com/photos/5225000/pexels-photo-5225000.jpeg?auto=compress&cs=tinysrgb&w=800"
                    ),
                    BattleNode(
                        "Gonio Fortress",
                        "Roman fortification on the coast.",
                        "17:00",
                        "https://images.pexels.com/photos/15878368/pexels-photo-15878368.jpeg?auto=compress&cs=tinysrgb&w=800"
                    )
                )
            )

            // THEME 1: CAPITAL (TBILISI) - DEEP HISTORY & ARISTOCRACY
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "tbilisi_deep_history_4d",
                title = "Tbilisi: Silk Road Aristocrat",
                description = "Peel back the Soviet layers to reveal the Tiflis of the 19th century. Explore Persian citadels, Sololaki mansions, and the Golden Treasury.",
                imageUrl = "[https://images.pexels.com/photos/14894364/pexels-photo-14894364.jpeg?auto=compress&cs=tinysrgb&w=1200](https://images.pexels.com/photos/14894364/pexels-photo-14894364.jpeg?auto=compress&cs=tinysrgb&w=1200)",
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
                        "D1 10:00"
                    ),
                    BattleNode(
                        "Kalantarov House",
                        "Day 1 11:30: Pseudo-Moorish architectural gem on Machabeli St.",
                        "D1 11:30"
                    ),
                    BattleNode(
                        "Cafe Littera",
                        "Day 1 13:00: Lunch in the Writer's House garden. Modern Georgian cuisine.",
                        "D1 13:00"
                    ),
                    BattleNode(
                        "Galaktioni 22",
                        "Day 1 15:00: Explore the painted entrance halls (Sadarbazo).",
                        "D1 15:00"
                    ),
                    BattleNode(
                        "Gudaishvili Square",
                        "Day 1 16:30: The oldest preserved square in the district.",
                        "D1 16:30"
                    ),
                    BattleNode(
                        "Ezo Restaurant",
                        "Day 1 19:30: Dinner in a traditional 'Italian Courtyard' setting.",
                        "D1 19:30"
                    ),

                    // DAY 2: Persian Roots & Sulfur
                    BattleNode(
                        "Narikala Fortress",
                        "Day 2 09:00: 4th-century citadel overlooking the city.",
                        "D2 09:00",
                        alertType = "STEEP_HIKE"
                    ),
                    BattleNode(
                        "Botanical Garden",
                        "Day 2 11:00: Walk to the hidden waterfall behind the fortress.",
                        "D2 11:00"
                    ),
                    BattleNode(
                        "Culinarium Khasheria",
                        "Day 2 13:00: Famous for hangover-curing Khashi soup.",
                        "D2 13:00"
                    ),
                    BattleNode(
                        "Orbeliani Baths",
                        "Day 2 14:30: The 'Blue Bath' favored by Pushkin. Scrub is mandatory.",
                        "D2 14:30",
                        alertType = "RESERVATION_NEEDED"
                    ),
                    BattleNode(
                        "Jumah Mosque",
                        "Day 2 17:00: Unique mosque where Sunnis and Shias pray together.",
                        "D2 17:00"
                    ),
                    BattleNode(
                        "Gabriadze Theater",
                        "Day 2 19:00: Watch the angel strike the bell at the leaning clock tower.",
                        "D2 19:00"
                    ),

                    // DAY 3: National Treasures
                    BattleNode(
                        "National Museum",
                        "Day 3 10:00: See the Golden Fleece gold jewelry in the treasury.",
                        "D3 10:00"
                    ),
                    BattleNode(
                        "Blue Gallery",
                        "Day 3 12:30: Works of naive painter Niko Pirosmani.",
                        "D3 12:30"
                    ),
                    BattleNode(
                        "Barbarestan",
                        "Day 3 14:00: Lunch based on a 19th-century duchess's cookbook.",
                        "D3 14:00",
                        alertType = "BOOK_AHEAD"
                    ),
                    BattleNode(
                        "Dry Bridge Market",
                        "Day 3 16:00: Antiques, Soviet medals, and vintage cameras.",
                        "D3 16:00"
                    ),
                    BattleNode(
                        "Mtatsminda Funicular",
                        "Day 3 18:30: Ride the train to the mountain top for sunset.",
                        "D3 18:30"
                    ),
                    BattleNode(
                        "Funicular Restaurant",
                        "Day 3 20:00: Legendary Ponchiki (donuts) with a view of the city lights.",
                        "D3 20:00"
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
                imageUrl = "[https://images.pexels.com/photos/6253995/pexels-photo-6253995.jpeg?auto=compress&cs=tinysrgb&w=1200](https://images.pexels.com/photos/6253995/pexels-photo-6253995.jpeg?auto=compress&cs=tinysrgb&w=1200)",
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
                        "D1 10:00"
                    ),
                    BattleNode(
                        "Nutsubidze Skybridge",
                        "Day 1 11:30: Three towers connected by high-altitude bridges.",
                        "D1 11:30",
                        alertType = "ELEVATOR_COIN"
                    ),
                    BattleNode(
                        "Mapshalia",
                        "Day 1 13:30: Authentic Soviet-style canteen. Cheap and delicious.",
                        "D1 13:30"
                    ),
                    BattleNode(
                        "Archaeology Museum",
                        "Day 1 15:00: Abandoned majestic structure with a massive bas-relief.",
                        "D1 15:00"
                    ),
                    BattleNode(
                        "Chronicles of Georgia",
                        "Day 1 17:00: Massive monument dubbed 'The Georgian Stonehenge'.",
                        "D1 17:00"
                    ),
                    BattleNode(
                        "Stamba Hotel",
                        "Day 1 20:00: Dinner in a converted Soviet publishing house.",
                        "D1 20:00"
                    ),

                    // DAY 2: Hipster Hubs
                    BattleNode(
                        "Fabrika",
                        "Day 2 11:00: Old sewing factory turned creative city. Street art & coffee.",
                        "D2 11:00"
                    ),
                    BattleNode(
                        "Shavi Lomi",
                        "Day 2 13:30: 'Black Lion'. The birthplace of modern Georgian fusion.",
                        "D2 13:30"
                    ),
                    BattleNode(
                        "Vodkast Records",
                        "Day 2 15:30: Vinyl digging and electronic music culture.",
                        "D2 15:30"
                    ),
                    BattleNode(
                        "Wine Factory N1",
                        "Day 2 18:00: Pre-drinks in a historic wine factory complex.",
                        "D2 18:00"
                    ),
                    BattleNode(
                        "Bassiani / Khidi",
                        "Day 2 23:55: The Techno Cathedral under the stadium. Strict face control.",
                        "D2 23:55",
                        alertType = "FACE_CONTROL"
                    ),

                    // DAY 3: Hidden Mosaics
                    BattleNode(
                        "Palace of Rituals",
                        "Day 3 12:00: Post-modern Soviet wedding palace.",
                        "D3 12:00"
                    ),
                    BattleNode(
                        "Expo Georgia",
                        "Day 3 14:00: Hunt for Soviet-era mosaics in the pavilions.",
                        "D3 14:00"
                    ),
                    BattleNode(
                        "Dezerter Bazaar",
                        "Day 3 16:00: Raw, chaotic central market. Photographer's paradise.",
                        "D3 16:00"
                    ),
                    BattleNode(
                        "Stalin's Printing Press",
                        "Day 3 17:30: Secret underground bunker used by young Bolsheviks.",
                        "D3 17:30"
                    ),
                    BattleNode(
                        "Bina N37",
                        "Day 3 20:00: Rooftop apartment restaurant with Qvevris on the balcony.",
                        "D3 20:00"
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
                imageUrl = "[https://images.pexels.com/photos/5225000/pexels-photo-5225000.jpeg?auto=compress&cs=tinysrgb&w=1200](https://images.pexels.com/photos/5225000/pexels-photo-5225000.jpeg?auto=compress&cs=tinysrgb&w=1200)",
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
                        "D1 09:30"
                    ),
                    BattleNode(
                        "Bodbe Monastery",
                        "Day 1 11:00: Burial place of St. Nino with valley views.",
                        "D1 11:00",
                        alertType = "DRESS_CODE"
                    ),
                    BattleNode(
                        "Pheasant’s Tears",
                        "Day 1 13:00: Lunch. The winery that revived the Qvevri tradition.",
                        "D1 13:00"
                    ),
                    BattleNode(
                        "Signagi Walls",
                        "Day 1 15:30: Walk the 18th-century fortifications.",
                        "D1 15:30"
                    ),
                    BattleNode(
                        "Vakho’s Winery",
                        "Day 1 17:30: Tasting amber wine directly from the clay pot.",
                        "D1 17:30"
                    ),
                    BattleNode(
                        "The Terrace Signagi",
                        "Day 1 20:00: Dinner overlooking the Alazani Valley.",
                        "D1 20:00"
                    ),

                    // DAY 2: The Great Estates
                    BattleNode(
                        "Tsinandali Estate",
                        "Day 2 10:30: Prince Chavchavadze's palace and gardens.",
                        "D2 10:30"
                    ),
                    BattleNode(
                        "Vazisubani Estate",
                        "Day 2 13:00: Lunch at a restored noble mansion.",
                        "D2 13:00"
                    ),
                    BattleNode(
                        "Alaverdi Monastery",
                        "Day 2 15:00: 11th-century cathedral and monastic wine cellar.",
                        "D2 15:00"
                    ),
                    BattleNode(
                        "Khareba Tunnel",
                        "Day 2 16:30: 7km wine tunnel carved into the rock.",
                        "D2 16:30",
                        alertType = "COLD_INSIDE"
                    ),
                    BattleNode(
                        "Kindzmarauli Corp",
                        "Day 2 18:00: Industrial wine tour in Kvareli.",
                        "D2 18:00"
                    ),
                    BattleNode("Kapiloni", "Day 2 20:00: Best Mtsvadi (BBQ) in Telavi.", "D2 20:00")
                )
            )

            // ===================================================================================
            // THEME 4: KAZBEGI - ALPINE ADVENTURE
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "kazbegi_explorer_2d",
                title = "Kazbegi: The Sky Piercer",
                description = "Drive the legendary Military Highway to the foot of Mt. Kazbek (5,047m). Glaciers, giants, and dumplings.",
                imageUrl = "[https://images.pexels.com/photos/13440788/pexels-photo-13440788.jpeg?auto=compress&cs=tinysrgb&w=1200](https://images.pexels.com/photos/13440788/pexels-photo-13440788.jpeg?auto=compress&cs=tinysrgb&w=1200)",
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
                        "D1 10:00"
                    ),
                    BattleNode(
                        "Friendship Monument",
                        "Day 1 12:00: Soviet mosaic balcony over the Devil's Valley.",
                        "D1 12:00"
                    ),
                    BattleNode(
                        "Rooms Hotel",
                        "Day 1 13:30: Lunch on the famous terrace facing the mountain.",
                        "D1 13:30"
                    ),
                    BattleNode(
                        "Gergeti Trinity",
                        "Day 1 15:30: The iconic church at 2,170m. 4x4 or hike.",
                        "D1 15:30",
                        alertType = "4X4_ONLY"
                    ),
                    BattleNode(
                        "Gveleti Waterfall",
                        "Day 1 17:30: Short hike to a powerful waterfall near the border.",
                        "D1 17:30"
                    ),
                    BattleNode(
                        "Shorena's",
                        "Day 1 20:00: Hearty mountain dinner. Try the Khinkali.",
                        "D1 20:00"
                    ),

                    // DAY 2: The Dolomites of Georgia
                    BattleNode(
                        "Sno Heads",
                        "Day 2 09:30: Giant granite heads of poets carved in an open field.",
                        "D2 09:30"
                    ),
                    BattleNode(
                        "Juta Valley",
                        "Day 2 11:00: Hike towards the Chaukhi Massif (Georgian Dolomites).",
                        "D2 11:00",
                        alertType = "HIKING_SHOES"
                    ),
                    BattleNode(
                        "Fifth Season Hut",
                        "Day 2 13:00: Lunch with an alpine view in Juta.",
                        "D2 13:00"
                    ),
                    BattleNode(
                        "Dariali Monastery",
                        "Day 2 16:00: Massive complex right on the Russian border.",
                        "D2 16:00"
                    ),
                    BattleNode(
                        "Tsdo Village",
                        "Day 2 17:00: Semi-abandoned village with animist shrines.",
                        "D2 17:00"
                    ),
                    BattleNode(
                        "Pasanauri",
                        "Day 2 19:30: Stop for Khinkali in their birthplace on the way back.",
                        "D2 19:30"
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
                imageUrl = "[https://images.pexels.com/photos/356269/pexels-photo-356269.jpeg?auto=compress&cs=tinysrgb&w=1200](https://images.pexels.com/photos/356269/pexels-photo-356269.jpeg?auto=compress&cs=tinysrgb&w=1200)",
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
                        "D1 10:00"
                    ),
                    BattleNode(
                        "Stalin's Bath",
                        "Day 1 11:30: Bathhouse No. 6. See the dictator's private pool.",
                        "D1 11:30"
                    ),
                    BattleNode("Magnolia", "Day 1 13:00: Lunch in Tskaltubo.", "D1 13:00"),
                    BattleNode(
                        "Prometheus Cave",
                        "Day 1 15:00: Underground boat ride and stalactites.",
                        "D1 15:00"
                    ),
                    BattleNode(
                        "Sisters",
                        "Day 1 20:00: Dinner in Kutaisi. Live piano and Imeretian wine.",
                        "D1 20:00"
                    ),

                    // DAY 2: Canyons
                    BattleNode(
                        "Martvili Canyon",
                        "Day 2 10:00: Emerald waters. Former bathing place of nobles.",
                        "D2 10:00",
                        alertType = "BOAT_RIDE"
                    ),
                    BattleNode(
                        "Oda Family Marani",
                        "Day 2 13:00: Lunch. Authentic Megrelian cuisine (Spicy!).",
                        "D2 13:00"
                    ),
                    BattleNode(
                        "Okatse Canyon",
                        "Day 2 15:30: Hanging walkway over the abyss.",
                        "D2 15:30"
                    ),
                    BattleNode(
                        "Kinchkha Waterfall",
                        "Day 2 17:00: Massive waterfall near Okatse.",
                        "D2 17:00"
                    ),
                    BattleNode(
                        "Bikentia's Kebabery",
                        "Day 2 20:00: Cult spot in Kutaisi. Kebabs and lemonade only.",
                        "D2 20:00"
                    ),

                    // DAY 3: Batumi Transfer
                    BattleNode(
                        "Bagrati Cathedral",
                        "Day 3 09:00: 11th-century symbol of united Georgia.",
                        "D3 09:00"
                    ),
                    BattleNode(
                        "Botanical Garden",
                        "Day 3 14:00: Arrival in Batumi. Massive cliffside gardens.",
                        "D3 14:00"
                    ),
                    BattleNode(
                        "Fish Market",
                        "Day 3 17:00: Buy raw fish, get it fried next door.",
                        "D3 17:00"
                    ),
                    BattleNode(
                        "Ali & Nino",
                        "Day 3 19:00: Moving steel statues of lovers.",
                        "D3 19:00"
                    ),
                    BattleNode(
                        "Retro",
                        "Day 3 20:30: The best Adjaruli Khachapuri (Boat shape) in town.",
                        "D3 20:30"
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