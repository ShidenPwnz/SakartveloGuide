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
                imageUrl = "https://wander-lush.org/wp-content/uploads/2020/12/The-ultimate-Georgia-itinerary-map.jpg",
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
                description = "Essential Intelligence: Georgia (Sakartvelo) is not merely a collection of scenic overlooks; it is a complex palimpsest where 8,000 years of viticulture intersect with 19th-century European aristocracy, Soviet industrial brutalism, and a modern, techno-fueled cultural renaissance. This guide articulates the tension between the ancient and the hyper-modern.",
                imageUrl = "https://flagcdn.com/w1280/ge.jpg",
                category = "GUIDE",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 0,
                durationDays = 0,
                route = emptyList(),
                itinerary = emptyList()
            )

            // ===================================================================================
            // THEME 1: CAPITAL (TBILISI) - DEEP HISTORY & ARISTOCRACY
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "tbilisi_deep_history_4d",
                title = "Tbilisi: Silk Road Aristocrat",
                description = "This itinerary is designed to immerse the user in the 'Tiflis' of the late 19th and early 20th centuries—a cosmopolitan hub where Persian influence waned as a distinct European bourgeoisie emerged. The narrative arc traverses the Sololaki district, once the Beverly Hills of the Caucasus, before descending into the ancient sulfur baths that birthed the city.",
                imageUrl = "https://wander-lush.org/wp-content/uploads/2020/05/Emily-Lush-Tbilisi-in-spring-11.jpg",
                category = "CULTURE",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 60,
                durationDays = 4,
                route = listOf(GeoPoint(41.6925, 44.7985), GeoPoint(41.6880, 44.8085)),
                itinerary = listOf(
                    // DAY 1: The Bourgeoisie of Sololaki
                    BattleNode(
                        "Writer’s House",
                        "Located at 13 Ivane Machabeli Street, this mansion is the crown jewel of Art Nouveau in the Caucasus. Commissioned by David Sarajishvili, the philanthropist and industrialist who founded the first brandy factory in the Russian Empire, the house was constructed between 1903 and 1905 by the German architect Karl Zaar. The architecture is a masterful synthesis of Art Nouveau fluidity and Neo-Baroque grandeur, characterized by a facade that ripples with organic ornamentation. The terrace is particularly notable for its mosaic floor, composed of tiles imported specifically from Villeroy & Boch—a detail that underscores the 'no expense spared' philosophy of the Georgian bourgeoisie. The building’s beauty belies a tragic history. In the early 1920s, it served as the headquarters for the 'Blue Horns' (Tsisperkantselebi), a group of symbolist poets. However, during the Soviet 'Great Terror,' the house became a stage for political theatre and tragedy. It was here, on the second floor, that the celebrated poet Paolo Iashvili committed suicide on July 22, 1937, shooting himself to escape the inevitability of arrest and torture by the NKVD.",
                        "D1 10:00",
                        location = GeoPoint(41.6896, 44.8010),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-best-museums-in-Tbilisi-Georgia-David-Sarajashvili-Museum.jpg"
                    ),
                    BattleNode(
                        "Kalantarov House",
                        "Situated at 17 Machabeli Street, the Kalantarov House is a striking example of the eclectic 'Pseudo-Moorish' style that became fashionable in Tiflis at the turn of the century. Built in 1908 by the Armenian architect Gazaros Sarkisyan, the facade is a theatrical display of Orientalism, featuring lancet arches, intricate 'stalactite' stucco work (muqarnas), and a vibrant turquoise and orange color palette. The interior vestibule is equally lavish, boasting a 'carpet-like' ceiling and a stained-glass skylight that casts a kaleidoscopic glow over the marble staircase. The construction of the house is shrouded in a romantic urban legend. It is said that Mikhail Kalantarov, a wealthy oil industrialist, fell in love with an opera singer. When he proposed, she set a condition: she would only marry him if he built her a house that resembled an opera theatre. Kalantarov accepted the challenge, and the resulting structure is indeed theatrical.",
                        "D1 11:30",
                        location = GeoPoint(41.6894, 44.8015),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-52-things-to-do-in-tbilisi-georgia-Sololaki-house.jpg"
                    ),
                    BattleNode(
                        "Cafe Littera",
                        "Located in the hidden garden of the Writer's House, Cafe Littera is the laboratory of Chef Tekuna Gachechiladze, the 'Queen of Georgian Fusion'. Gachechiladze is a pivotal figure in modern Georgian gastronomy; she was the first to challenge the rigid dogmas of the traditional Supra (feast) by lightening heavy peasant dishes and introducing international techniques. The restaurant is famous for reimagining classics. Highlights include Chakapuli with Mussels (substituting lamb for mussels in a tart tarragon stew) and Elarji Balls (fried croquettes of cornmeal and Sulguni cheese served with spiced almond Baje sauce). The setting, under a massive pine tree in the historic garden, offers a respite from the city's heat and noise.",
                        "D1 13:00",
                        location = GeoPoint(41.6896, 44.8010),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/06/Emily-Lush-restaurants-in-Tbilisi-new-Cafe-Littera-food.jpg"
                    ),
                    BattleNode(
                        "Galaktioni 22",
                        "The concept of the Sadarbazo (entrance hall) is central to understanding 19th-century Tiflis social life. These halls were the 'calling cards' of the owners, designed to bridge the gap between the dusty public street and the private sanctuary of the home. The address at Galaktion Tabidze Street #18 features one of the most spectacular examples. This specific hall was the residence of the Seilanov brothers, prominent tobacco merchants. They commissioned the Italian artist Benno Telingater to paint the vestibule in a style that synthesized Baroque ornamentation with Romantic landscapes. The floor welcomes visitors with the Latin inscription SALVE ('Welcome') embedded in the mosaic—a clear signal of the owner's European aspirations.",
                        "D1 15:00",
                        location = GeoPoint(41.6910, 44.7995),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-52-things-to-do-in-tbilisi-georgia-Sololaki-entryway.jpg"
                    ),
                    BattleNode(
                        "Gudiashvili Square",
                        "Lado Gudiashvili Square is a rare survival of Tbilisi's medieval urban fabric. Unlike much of the city, which was reshaped by Russian and Soviet planners, this square retains the irregular, organic layout of the middle ages. Known historically as 'Bejana's Garden', it served as the center of the 'Kala' district. The square is also a monument to modern civic activism. In the 2010s, it was the site of a fierce preservation battle led by the 'Tiflis Hamkari' to save the historic 'Blue House' and other structures from demolition by developers. A major restoration project stabilized the 19th-century buildings that sit atop 16th-century cellar foundations. The square now serves as a quiet, pedestrianized zone, offering a glimpse of the city's pre-industrial scale.",
                        "D1 16:30",
                        location = GeoPoint(41.6935, 44.8005),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-Gudiashvili-Square.jpg"
                    ),
                    BattleNode(
                        "Ezo Restaurant",
                        "'Ezo' translates simply to 'Yard' in Georgian. Located at 16 Geronti Kikodze Street, the restaurant is situated within the inner courtyard of a residential block, embodying the spirit of the 'Tbilisuri Ezo' (Tbilisi Yard)—a communal space where private lives spilled out into the public domain. The kitchen adheres to a strict philosophy of organic, farm-to-table sourcing, rejecting the industrialization of food. Unlike the smooth, pureed Pkhali found in tourist traps, Ezo serves a chopped, textured version that mimics home cooking. The cellar focuses exclusively on natural, organic wines produced by small family vintners, reinforcing the connection between the urban table and the rural soil.",
                        "D1 19:30",
                        location = GeoPoint(41.6905, 44.7990),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-52-things-to-do-in-tbilisi-georgia-Sololaki-courtyard.jpg"
                    ),

                    // DAY 2: Persian Roots & Sulfur
                    BattleNode(
                        "Narikala Fortress",
                        "Narikala is the acropolis of Tbilisi, a defensive singularity that has guarded the Mtkvari River gorge for 1,600 years. Originally established in the 4th century by the Sassanid Persians as 'Shuris-Tsikhe' (Invidious Fortress), it predates the capital itself. The fortress was significantly expanded by the Umayyad emirs in the 7th century and later by King David the Builder. While the walls appear ancient, much of the interior structure was destroyed not by medieval siege, but by a massive accidental explosion in 1827. The Russian imperial army used the fortress as a munitions depot, and a lightning strike (or negligence) detonated the gunpowder, shattering the Church of St. Nicholas and demolishing the governor's palace.",
                        "D2 09:00",
                        alertType = "STEEP_HIKE",
                        location = GeoPoint(41.6880, 44.8085),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/01/Emily-Lush-Tbilisi-Georgia-Kartlis-Deda-drone.jpg"
                    ),
                    BattleNode(
                        "Botanical Garden",
                        "Walk behind the fortress to the hidden waterfall. A lush escape situated in the ancient Tsavkisis-Tskali gorge.",
                        "D2 11:00",
                        location = GeoPoint(41.6870, 44.8060),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Emily-Lush-best-Tbilisi-views-Turtle-Lake-hills-view.jpg"
                    ),
                    BattleNode(
                        "Culinarium Khasheria",
                        "Located near the baths, famous for Khashi—the tripe and garlic soup historically eaten by carousers to cure hangovers.",
                        "D2 13:00",
                        location = GeoPoint(41.6885, 44.8105),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/06/Emily-Lush-restaurants-in-Tbilisi-new-Culinarium-food.jpg"
                    ),
                    BattleNode(
                        "Orbeliani Baths",
                        "Amidst the brick domes of the Abanotubani district, the Orbeliani Bath (also known as Chreli Abano or the Colorful Bath) stands out for its flamboyant facade. Resembling a Central Asian madrasa, the exterior is covered in blue and turquoise mosaic tiles, a testament to the Persian influence that permeated Tiflis culture for centuries. A plaque near the entrance immortalizes the visit of Alexander Pushkin in 1829, who wrote, 'I have never encountered anything more luxurious than this Tbilisi bath'. The experience is incomplete without the 'Kisa' scrub, where a Mekise uses a coarse, woolen mitt to vigorously exfoliate the bather, removing layers of dead skin and stimulating circulation.",
                        "D2 14:30",
                        alertType = "RESERVATION_NEEDED",
                        location = GeoPoint(41.6883, 44.8109),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-Chreli-Abano-sulfur-baths.jpg"
                    ),
                    BattleNode(
                        "Jumah Mosque",
                        "The Jumah Mosque is a potent symbol of Tbilisi's historical tolerance. Located at the foot of the Narikala ridge, it is one of the few places in the Islamic world where Sunni and Shia Muslims worship together in the same sanctuary. Historically, Tbilisi had two mosques: the Sunni Jumah Mosque and the Shia Blue Mosque. In 1951, the Communist government demolished the Blue Mosque to construct the Metekhi Bridge. The displaced Shia community was welcomed into the Sunni mosque. For decades, a black curtain separated the two denominations during prayer, but in 1996, the curtain was removed. Today, the community prays side-by-side, a rare example of intra-faith unity driven by necessity and solidarity.",
                        "D2 17:00",
                        location = GeoPoint(41.6875, 44.8115),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/10/Emily-Lush-Tbilisi-State-Academy-of-Arts-mosque.jpg"
                    ),
                    BattleNode(
                        "Gabriadze Theater",
                        "Watch the angel strike the bell at the leaning clock tower.",
                        "D2 19:00",
                        location = GeoPoint(41.6958, 44.8065),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-Gabriadze-Marionette-Theatre.jpg"
                    ),

                    // DAY 3: National Treasures
                    BattleNode(
                        "National Museum",
                        "The Archaeological Treasury in the basement of the National Museum fundamentally challenges the Greek myth of the 'Golden Fleece.' The collection demonstrates that Colchis was not a mythical land of barbarians, but a highly sophisticated kingdom with advanced goldsmithing techniques that predated classical Greece. Key artifacts include the Tortoise Necklace, a gold necklace from the Vani site (5th century BC) featuring 31 microscopic tortoises. The granulation technique used to create the eyes and shells is so precise that modern jewelers struggle to replicate it without advanced optics.",
                        "D3 10:00",
                        location = GeoPoint(41.6963, 44.8002),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-best-museums-in-Tbilisi-Georgia-National-Museum-Soviet-Occupation-Hall-desk.jpg"
                    ),
                    BattleNode(
                        "Blue Gallery",
                        "The Blue Gallery houses the definitive collection of Niko Pirosmani (1862–1918), Georgia’s most beloved painter. A self-taught 'primitivist,' Pirosmani painted on black oilcloth because it was the cheapest material available in the dukhans (taverns) where he traded his art for food and vodka. Don't miss the masterpiece 'Actress Margarita.' This portrait depicts Marguerite de Sèvres, a French singer who toured Tiflis in 1905. The painting is the subject of a famous romantic legend: it is said that Pirosmani sold his shop and all his possessions to buy 'a million scarlet roses,' filling the square beneath her hotel window with flowers to declare his love.",
                        "D3 12:30",
                        location = GeoPoint(41.6970, 44.7990),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-Sighnaghi-Museum-Kakheti-Pirosmani-Fruit-Stall.jpg"
                    ),
                    BattleNode(
                        "Barbarestan",
                        "Barbarestan is a culinary museum. The entire menu is based on a single historical text: Complete Cooking (1874) by Duchess Barbare Eristavi-Jorjadze, Georgia’s first feminist, poet, and culinary scholar. The Kurasbediani family, who founded the restaurant, discovered a copy of the cookbook at the Dry Bridge flea market and dedicated themselves to reviving these lost aristocratic recipes. The food differs significantly from standard Georgian fare. Try the Duck in Pomegranate & Wine Sauce or the Almond Rose Meringue, a delicate dessert using rose water and mascarpone, recreating the refined palate of the 19th-century nobility.",
                        "D3 14:00",
                        alertType = "BOOK_AHEAD",
                        location = GeoPoint(41.7105, 44.7960),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/05/Emily-Lush-Tbilisi-restaurant-Barbarestan.jpg"
                    ),
                    BattleNode(
                        "Dry Bridge Market",
                        "The Dry Bridge Market is an open-air museum of the Soviet collapse. The market emerged organically in the early 1990s, during the dark years of civil war and economic ruin, when desperate citizens brought their household possessions to the street to sell for bread. The 'bridge' itself is 'dry' because the branch of the Kura River that once flowed beneath it was filled in to create roads. Today, it is a haven for collectors. The stalls are laden with Soviet memorabilia: medals from the Great Patriotic War, vintage Zenit and Kiev film cameras, maps of the USSR, and surgical instruments. It is a place to haggle for pieces of a dissolved empire.",
                        "D3 16:00",
                        location = GeoPoint(41.7005, 44.8055),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-Dry-Bridge-antiques.jpg"
                    ),
                    BattleNode(
                        "Mtatsminda Funicular",
                        "The Mtatsminda Funicular is a piece of transport history, originally constructed in 1905 by a Belgian company to develop the plateau into a European-style 'upper town'. The railway climbs 501 meters at a remarkably steep gradient of 33 degrees.",
                        "D3 18:30",
                        location = GeoPoint(41.6948, 44.7845), // Lower station
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/10/Emily-Lush-Mtatsminda-Cable-Car-Lower-Station-building-restored.jpg"
                    ),
                    BattleNode(
                        "Funicular Restaurant",
                        "The Funicular Restaurant at the summit is less about fine dining and more about a specific ritual: eating Ponchiki. These are massive, deep-fried donuts filled with hot custard cream. It is a Tbilisi tradition to ride the train up, order a plate of Ponchiki and a distinctively fizzy Lagidze water (typically tarragon or pear), and watch the city lights flicker on from the terrace.",
                        "D3 20:00",
                        location = GeoPoint(41.6948, 44.7800), // Upper station
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/10/Emily-Lush-Mtatsminda-Cafe-Funicular-ponchiki-pumpkin-cream.jpg"
                    )
                )
            )

            // ===================================================================================
            // THEME 2: CAPITAL (TBILISI) - BOHEMIAN & BRUTALIST
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "tbilisi_bohemian_3d",
                title = "Tbilisi: Concrete & Techno",
                description = "This itinerary targets the 'Urbex' traveler and the electronic music enthusiast, exploring the aesthetic of Soviet decay and its reclamation by a progressive youth culture.",
                imageUrl = "https://www.kathmanduandbeyond.com/wp-content/uploads/2022/04/Bank-of-Georgia-headquarters-former-Ministry-of-Highway-Construction-Tbilisi-Georgia-5.jpg",
                category = "URBAN_EXPLORER",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 90,
                durationDays = 3,
                route = listOf(GeoPoint(41.7355, 44.7708), GeoPoint(41.7095, 44.8028)),
                itinerary = listOf(
                    // DAY 1: Concrete Utopias
                    BattleNode(
                        "Bank of Georgia HQ",
                        "Completed in 1975, this building is a globally recognized masterpiece of Soviet Constructivism and Brutalism. Designed by architect George Chakhava (who was also the Minister of Highway Construction, essentially serving as his own client), the structure realizes the concept of the 'Space City'. The design consists of interlocking concrete blocks stacked like Jenga pieces on a central core. This 'horizontal skyscraper' approach was intended to minimize the building's footprint, allowing the forest to flow uninterrupted beneath the structure. Acquired by the Bank of Georgia in 2007, it remains a dystopian, sci-fi landmark visible from across the city.",
                        "D1 10:00",
                        location = GeoPoint(41.7355, 44.7708),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-Former-Ministry-of-Roads.jpg"
                    ),
                    BattleNode(
                        "Nutsubidze Skybridge",
                        "The Nutsubidze Plateau complex features three massive residential towers connected at the 14th floor by steel skybridges. This was a Soviet experiment in topography management, designed to link the upper and lower roads of the hillside district without forcing pedestrians to hike. The elevators in these towers are famously coin-operated. Residents and visitors must insert a 20-tetri coin into a box to activate the lift. In one of the towers, a 'lift operator' (a local resident named Mzia) actually lives in a small booth inside the elevator mechanism room, collecting coins and maintaining the system—a living relic of communal infrastructure.",
                        "D1 11:30",
                        alertType = "ELEVATOR_COIN",
                        location = GeoPoint(41.7270, 44.7350),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Emily-Lush-Nutsubidze-Skybridge-Tbilisi-new-bridges.jpg"
                    ),
                    BattleNode(
                        "Mapshalia",
                        "Mapshalia is a time capsule—a former Soviet workers' canteen (Sasadilo) that has refused to gentrify. Located at 137 David Agmashenebeli Ave, the interior is dominated by a massive, crumbling Soviet bas-relief of the town of Gagra (in Abkhazia) on the back wall. This is arguably the best budget spot in the city for Megrelian cuisine (from West Georgia), known for its liberal use of spice and cheese. Must-Order: Elarji (cornmeal with excessive amounts of Sulguni cheese) and Kharsho (a spicy beef and walnut soup). The prices are incredibly low, attracting a democratic mix of pensioners, students, and tourists.",
                        "D1 13:30",
                        location = GeoPoint(41.7085, 44.7965),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/05/Emily-Lush-Tbilisi-restaurant-Mapshalia.jpg"
                    ),
                    BattleNode(
                        "Archaeology Museum",
                        "Abandoned majestic structure with a massive bas-relief.",
                        "D1 15:00",
                        location = GeoPoint(41.7750, 44.7670),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-Former-Archaeology-Museum.jpg"
                    ),
                    BattleNode(
                        "Chronicles of Georgia",
                        "Looming over the Tbilisi Sea, the Chronicles of Georgia is a gargantuan bronze and stone memorial created by Zurab Tsereteli starting in 1985. The monument remains unfinished, which adds to its eerie, ancient atmosphere. The complex consists of 16 pillars, each 35 meters tall. The lower sections depict the life of Christ, while the upper sections illustrate the history of Georgian kings, queens, and heroes. The sheer scale evokes a feeling of awe and insignificance, earning it the nickname 'The Georgian Stonehenge.' Its location, far from the center and often windswept, provides a stark, contemplative contrast to the bustle of the Old Town.",
                        "D1 17:00",
                        location = GeoPoint(41.7705, 44.8105),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-Chronicles-of-Georgia.jpg"
                    ),
                    BattleNode(
                        "Stamba Hotel",
                        "Stamba is the flagship of Tbilisi's industrial-chic revolution. Housed in the historic Soviet-era publishing house of the Communist newspaper, the hotel preserves the raw concrete skeleton and massive printing machinery of the 1930s. The lobby is a five-story atrium where the printing presses once roared. Today, trees grow indoors, and a glass-bottomed pool on the roof casts rippling light shadows down the concrete walls. The hotel also houses 'Space Farms,' the first vertical indoor farm in Georgia, growing microgreens and edible flowers used in the restaurant's kitchen.",
                        "D1 20:00",
                        location = GeoPoint(41.7060, 44.7865),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Tbilisi-restaurant-Cafe-Stamba-Space-Farms.jpg"
                    ),

                    // DAY 2: Hipster Hubs
                    BattleNode(
                        "Fabrika",
                        "Old sewing factory turned creative city. Street art & coffee.",
                        "D2 11:00",
                        location = GeoPoint(41.7095, 44.8028),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2017/09/EmilyLushTbilisiGeorgia-24.jpg"
                    ),
                    BattleNode(
                        "Shavi Lomi",
                        "'Black Lion'. The birthplace of modern Georgian fusion.",
                        "D2 13:30",
                        location = GeoPoint(41.7115, 44.8040),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/07/Emily-Lush-Shavi-Lomi-Gobi-sharing-bowl.jpg"
                    ),
                    BattleNode(
                        "Vodkast Records",
                        "Vinyl digging and electronic music culture.",
                        "D2 15:30",
                        location = GeoPoint(41.7045, 44.7900), // Approx Vera location
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/08/Emily-Lush-bars-in-Tbilisi-Craft-Wine-Bar-pink.jpg"
                    ),
                    BattleNode(
                        "Wine Factory N1",
                        "Built between 1894 and 1896 by architect Alexander Ozerov, this was the first major industrial wine factory in Tbilisi. For over a century, its deep cellars were closed to the public, storing wines dating back to the Napoleonic era and the private stash of Stalin. Reopened in 2017, the complex is now a gastronomy cluster. The original architecture—eclectic brickwork with industrial arches—has been preserved. Visitors can explore the 'Wine Library,' seeing bottles covered in a century of dust, or drink cocktails in the repurposed industrial courtyards.",
                        "D2 18:00",
                        location = GeoPoint(41.7065, 44.7830),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-52-things-to-do-in-tbilisi-georgia-wine-factory-n1-terrace.jpg"
                    ),
                    BattleNode(
                        "Bassiani / Khidi",
                        "Bassiani is not merely a nightclub; it is a political movement and a sanctuary for the marginalized. Located in the drained swimming pool of the Dinamo Arena (the national football stadium), it is the largest techno club in Georgia. The dance floor is the tiled bottom of the pool, with the sloping sides acting as seating. Bassiani is the headquarters of the 'rave revolution.' In May 2018, armed police raided the club, sparking massive protests in front of the Parliament where thousands danced to techno music as a form of non-violent resistance. Access is strictly controlled via online verification ('Face Control'). Inside, photography is banned.",
                        "D2 23:55",
                        alertType = "FACE_CONTROL",
                        location = GeoPoint(41.7230, 44.7897),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-metro-station-artwork.jpg"
                    ),

                    // DAY 3: Hidden Mosaics
                    BattleNode(
                        "Palace of Rituals",
                        "Also known as the 'Wedding Palace,' this building is a bizarre and beautiful example of Soviet Expressionism fused with medieval Georgian church architecture. Built in 1984 by architects Victor Djorbenadze and Vazha Orbeladze, its phallic, cathedral-like silhouette was intended to bring a sense of sacred ritual to secular Soviet civil ceremonies. In 2002, the building was purchased by the oligarch Badri Patarkatsishvili as a private residence. While the interior is private, the exterior remains a pilgrimage site for architecture lovers. Margaret Thatcher famously visited the site in 1987, where she was treated to a Georgian dance performance.",
                        "D3 12:00",
                        location = GeoPoint(41.6780, 44.8320),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-Palace-of-Rituals-view.jpg"
                    ),
                    BattleNode(
                        "Expo Georgia",
                        "The Expo Georgia trade fair grounds in the Didube district are a hidden park of Soviet Modernist pavilions. The highlight is the preservation of Soviet-era mosaics. The key work is the mosaic by Leonardo Shengelia (1963) near the entrance depicting cosmonauts and scientists, reflecting the Soviet obsession with space exploration and technological progress. The park is quiet, filled with palm trees and ponds, offering a retro-futuristic atmosphere largely untouched by modern development.",
                        "D3 14:00",
                        location = GeoPoint(41.7380, 44.7810),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-Expo-Georgia-mosaic.jpg"
                    ),
                    BattleNode(
                        "Dezerter Bazaar",
                        "Raw, chaotic central market. Photographer's paradise.",
                        "D3 16:00",
                        location = GeoPoint(41.7210, 44.7930),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2019/07/Emily-Lush-Tbilisi-Dezerter-Bazaar-2.jpg"
                    ),
                    BattleNode(
                        "Stalin's Printing Press",
                        "Hidden 17 meters beneath a nondescript house in the Avlabari district is the clandestine printing press where a young Joseph Stalin (then known by his revolutionary alias 'Koba') printed Bolshevik propaganda between 1903 and 1906. The press was accessed via a well shaft. Revolutionaries would climb down the well to reach the printing room. If the police raided the house, they had an escape tunnel leading out of the complex. The museum is currently operated by the Georgian Communist Party and receives no state funding. It is dusty, rusting, and feels trapped in time.",
                        "D3 17:30",
                        location = GeoPoint(41.6850, 44.8250),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/01/Emily-Lush-Stalin-Printing-Press-Museum-Avlabari-Tbilisi-machinery.jpg"
                    ),
                    BattleNode(
                        "Bina N37",
                        "Bina N37 is Tbilisi's only 'rooftop winery.' Located in a residential apartment block on the 8th floor (Apartment 37), the owner, Zura Natroshvili, executed an audacious plan: he embedded 43 massive clay Qvevri vessels into the concrete of his penthouse balcony. Dining here feels like visiting a friend's home. You ring the doorbell of a regular apartment, but inside you find a fully functioning restaurant where wine is fermented high above the city traffic. It is proof that the ancient Georgian tradition of Qvevri winemaking can adapt to the modern urban jungle.",
                        "D3 20:00",
                        location = GeoPoint(41.7180, 44.7610),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/05/Emily-Lush-Tbilisi-restaurant-Bina-37.jpg"
                    )
                )
            )

            // ===================================================================================
            // THEME 3: KAKHETI - WINE & KINGS
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "kakheti_royal_2d",
                title = "Kakheti: The 8000 Vintage",
                description = "This module explores the 'Cradle of Wine.' Georgia is the oldest wine-producing country in the world, with a continuous history of 8,000 years. The itinerary contrasts the peasant traditions of the Qvevri (buried clay jars) with the 19th-century European estates of the Georgian nobility.",
                imageUrl = "https://wander-lush.org/wp-content/uploads/2017/07/GeorgiaKakheti-28.jpg",
                category = "WINE_REGION",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 240,
                durationDays = 2,
                route = listOf(GeoPoint(41.6205, 45.9255), GeoPoint(41.8965, 45.5680)),
                itinerary = listOf(
                    // DAY 1: Signagi & Natural Wine
                    BattleNode(
                        "Badiauri",
                        "Badiauri is a roadside village that serves as the unofficial 'drive-thru' of Kakheti. It is famous for Shoti bread (Dedis Puri - 'Mother's Bread'). Visitors can watch the women bakers slap the dough against the scorching inner walls of the Tone (a deep, circular clay oven similar to a Tandoor). The bread is shaped like a sword (Khmali) or a crescent to fit the curve of the oven. Local vendors sell salty, white Guda cheese (a sheep's milk cheese aged in sheepskin bags, distinct from Gouda) to eat with the hot bread. The combination of the scalding hot, yeasty bread and the pungent, salty cheese is the quintessential Georgian road snack.",
                        "D1 09:30",
                        location = GeoPoint(41.6660, 45.4330),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-shotis-bread.jpg"
                    ),
                    BattleNode(
                        "Bodbe Monastery",
                        "Bodbe is one of the holiest sites in Georgia, housing the tomb of Saint Nino, the Cappadocian woman who converted Georgia to Christianity in the 4th century. A steep path (approximately 667 steps) leads down from the monastery to St. Nino’s Spring. Legend holds that the spring emerged through Nino's prayers. Pilgrims bathe in the freezing cold water (wearing white gowns) for spiritual cleansing and healing. The monastery grounds offer a serene, manicured view over the Alazani Valley, often filled with the scent of cypress and roses.",
                        "D1 11:00",
                        alertType = "DRESS_CODE",
                        location = GeoPoint(41.6068, 45.9328),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/04/Emily-Lush-Kakheti-itinerary-Bodbe-Monastery-view.jpg"
                    ),
                    BattleNode(
                        "Pheasant’s Tears",
                        "Located in Signagi, Pheasant’s Tears is the headquarters of the natural wine revolution in Georgia. Founded by John Wurdeman, an American painter, and Gela Patalishvili, an 8th-generation winemaker, the winery fought against the Soviet-style industrialization of wine, advocating a return to organic farming and the Qvevri method. The name derives from a Georgian legend that claims only a wine beyond measure could make a pheasant cry tears of joy. Their restaurant serves 'Polyphonic' cuisine—dishes sourced from wild foraged ingredients and ancient grain varieties, designed to harmonize with the tannins of amber wines.",
                        "D1 13:00",
                        location = GeoPoint(41.6195, 45.9220),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/06/Emily-Lush-Sighnaghi-restaurant-Pheasants-Tears-cellar.jpg"
                    ),
                    BattleNode(
                        "Signagi Walls",
                        "Signagi is often called the 'City of Love' (due to its 24/7 wedding registry), but its origins are strictly military. The city is surrounded by a massive defensive wall built by King Erekle II in 1762 to protect the population from Lezgin raids. The wall is 4.5 kilometers long and features 23 towers and 6 gates. It is one of the longest existing fortification walls in the world. Each tower was named after a nearby village; during attacks, the villagers from that specific hamlet would retreat to their designated tower for protection.",
                        "D1 15:30",
                        location = GeoPoint(41.6210, 45.9250),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Emily-Lush-visit-Kakheti-wine-region-Georgia-Sighnagh-Walls.jpg"
                    ),
                    BattleNode(
                        "Vakho’s Winery",
                        "In contrast to the fame of Pheasant’s Tears, Vakho Oqruashvili’s cellar in the village of Velistsikhe offers an intimate, domestic experience. This is a small family Marani (wine cellar). Vakho often invites guests to stomp grapes (during harvest) or taste wine straight from the Qvevri using an Orshimo (a long-handled gourd ladle). The experience feels less like a commercial tour and more like visiting a Georgian grandfather, focusing on the hospitality that defines the region.",
                        "D1 17:30",
                        location = GeoPoint(41.6180, 45.9240),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2019/07/Emily-Lush-Tbilisi-to-Sighnaghi-10.jpg"
                    ),
                    BattleNode(
                        "The Terrace Signagi",
                        "Perched on the steep cliffs of Signagi, this restaurant offers the definitive 'Golden Hour' view. The terrace looks directly out over the vast Alazani Valley towards the Greater Caucasus mountain range. While known for excellent Mtsvadi (grilled meat) and Khachapuri, the primary draw is the panoramic vantage point that captures the essence of Signagi’s 'Italianate' architecture against the rugged backdrop of the Caucasus.",
                        "D1 20:00",
                        location = GeoPoint(41.6200, 45.9210),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2025/06/Emily-Lush-Sighnaghi-Kakheti-Georgia-city-golden-hour.jpg"
                    ),

                    // DAY 2: The Great Estates
                    BattleNode(
                        "Tsinandali Estate",
                        "Tsinandali is the birthplace of Georgian Romanticism and modern winemaking. It was the estate of Prince Alexander Chavchavadze (1786–1846), an aristocrat, poet, and godson of Catherine the Great. Alexander was the first to bottle Georgian wine using European techniques, bridging the gap between the Qvevri and the barrel. His Oenotheque (wine library) still preserves bottles from the 1841 Saperavi vintage. The estate features the first European-style landscape garden in Georgia. Designed to resemble Kew Gardens or Richmond, it symbolized Georgia's cultural pivot toward the West in the 19th century.",
                        "D2 10:30",
                        location = GeoPoint(41.8965, 45.5680),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Emily-Lush-visit-Kakheti-wine-region-Georgia-Tsinandali.jpg"
                    ),
                    BattleNode(
                        "Vazisubani Estate",
                        "Lunch at a restored noble mansion.",
                        "D2 13:00",
                        location = GeoPoint(41.8380, 45.7100),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/04/Emily-Lush-Kakheti-accommodation-Vazisubani-Estate-heritage.jpg"
                    ),
                    BattleNode(
                        "Alaverdi Monastery",
                        "The Alaverdi Cathedral (Cathedral of St. George) towers over the valley at 50 meters, making it the tallest church in Georgia for nearly a millennium (until the modern Trinity Cathedral in Tbilisi was built in 2004). Built in the early 11th century by King Kvirike the Great, its austere facade is a masterpiece of medieval proportion. The monks produce wine under the label 'Since 1011.' They have maintained the monastery's Marani continuously since the 11th century. Their wine is highly tannic and austere, produced with prayer and strict discipline, reflecting the spiritual intensity of the site.",
                        "D2 15:00",
                        location = GeoPoint(42.0325, 45.3770),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/02/Emily-Lush-Alaverdi-Monastery-Kakheti-closeup.jpg"
                    ),
                    BattleNode(
                        "Khareba Tunnel",
                        "This site in Kvareli is a repurposed relic of the Cold War. In the 1950s, the Soviet military drilled 7.7 kilometers of tunnels into the solid granite of the Caucasus Mountains, intended as a bomb shelter against nuclear attack. The shelter was never used for war. In the 1960s, winemakers discovered that the tunnels maintained a constant natural temperature of 12-14°C with 70% humidity—perfect conditions for aging wine. Today, Winery Khareba stores over 25,000 bottles here. Visitors walk deep into the mountain, surrounded by walls of granite and oak barrels.",
                        "D2 16:30",
                        alertType = "COLD_INSIDE",
                        location = GeoPoint(41.9485, 45.8340),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/04/Emily-Lush-Winery-Khareba-Wine-Tunnel-Kakheti-bottles.jpg"
                    ),
                    BattleNode(
                        "Kindzmarauli Corp",
                        "Industrial wine tour in Kvareli.",
                        "D2 18:00",
                        location = GeoPoint(41.9470, 45.8130),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/06/Emily-Lush-best-wineries-in-Kakheti-Georgia-Kindzmarauli-factory.jpg"
                    ),
                    BattleNode(
                        "Kapiloni",
                        "Kapiloni is a local legend in Telavi, located near the Bastonistsikhe Castle. It is unpretentious and focused entirely on the art of meat. Signature Dish: Mtsvadi. In Kakheti, BBQ is a religion. Kapiloni uses vine clippings (sarmenti) to fire the grill, which imparts a distinct, sweet smokiness to the pork. The meat is served simply with raw onions and fresh pomegranate juice. There are no heavy marinades—just high-quality meat, salt, and fire.",
                        "D2 20:00",
                        location = GeoPoint(41.9180, 45.4740),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/04/Emily-Lush-Kakheti-itinerary-Telavi-architecture.jpg"
                    )
                )
            )

            // ===================================================================================
            // THEME 4: KAZBEGI - ALPINE ADVENTURE
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "kazbegi_explorer_2d",
                title = "Kazbegi: The Sky Piercer",
                description = "This route follows the Georgian Military Highway, a strategic road connecting Tbilisi to Russia through the high Caucasus. It is a landscape defined by giants, glaciers, and geopolitical tension.",
                imageUrl = "https://wander-lush.org/wp-content/uploads/2019/11/Emily-Lush-Kazbegi-Gergeti-Trinity-Georgia-11.jpg",
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
                        "Ananuri is a 17th-century fortress that served as the seat of the Eristavis (Dukes) of Aragvi, a feudal dynasty notorious for their ruthlessness. The name 'Ananuri' is derived from a legend about a woman named Ana. During a siege, she refused to reveal the location of the secret tunnel to the river, choosing torture and death over betrayal. Hence, the name 'Ana-nuri' (Ana from Nuri). The complex contains the Church of the Assumption (1689), which features some of the finest stone carvings in Georgia. The facade is adorned with intricate depictions of vines, animals, and the cross, representing the peak of late medieval stone masonry.",
                        "D1 10:00",
                        location = GeoPoint(42.1636, 44.7030),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/04/Emily-Lush-Kazbegi-Gergeti-Trinity-Georgia-10.jpg"
                    ),
                    BattleNode(
                        "Friendship Monument",
                        "Perched on the edge of the Devil’s Valley gorge near Gudauri, this massive concrete semi-circle is a vibrant paradox. Built in 1983 to celebrate the bicentennial of the Treaty of Georgievsk (which made Georgia a Russian protectorate), it is a monument to a 'friendship' that has often been fraught with conflict. The interior features a massive, colorful tile mosaic depicting scenes from Georgian and Russian history and mythology. Despite the complicated political context, the monument remains a stunning example of late Soviet monumental art, offering terrifyingly steep views into the abyss below.",
                        "D1 12:00",
                        location = GeoPoint(42.4862, 44.4542),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Valley-mountains.jpg"
                    ),
                    BattleNode(
                        "Rooms Hotel",
                        "Rooms Kazbegi is the pioneer of modern Georgian hospitality. The building was originally a Soviet workers' sanatorium, a place for the proletariat to recuperate. In 2012, it was transformed into a luxury design hotel. The architects preserved the brutalist concrete structure but filled the interior with vintage rugs, leather armchairs, and timber, creating a 'Wes Anderson' aesthetic. The massive terrace provides the single best view of Mount Kazbek (5,047m) and the Trinity Church, allowing guests to view the wild landscape from a position of supreme comfort.",
                        "D1 13:30",
                        location = GeoPoint(42.6600, 44.6430),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/04/Best-hotels-in-Kazbegi-Georgia-Rooms-Kazbegi-small.jpg"
                    ),
                    BattleNode(
                        "Gergeti Trinity",
                        "The Holy Trinity Church (Tsminda Sameba) is the visual shorthand for Georgia. Built in the 14th century, it stands in solitary isolation at 2,170 meters, with the glacier-covered Mount Kazbek looming directly behind it. During the Persian invasion of 1795, the cross of St. Nino and other precious relics from Mtskheta were brought here to the clouds for safekeeping, as the location was deemed impregnable. In the Soviet era, authorities built a cable car to the church to ease access. The local people, feeling it desecrated the sanctity of the pilgrimage, destroyed it. Today, access is by a steep hike or 4x4 Delica vans, preserving the effort required to reach the shrine.",
                        "D1 15:30",
                        alertType = "4X4_ONLY",
                        location = GeoPoint(42.6625, 44.6200),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/06/Emily-Lush-Kazbegi-Georgia-Gergeti-Trinity-Church-hike-viewpoint-shrine-cross.jpg"
                    ),
                    BattleNode(
                        "Gveleti Waterfall",
                        "Short hike to a powerful waterfall near the border.",
                        "D1 17:30",
                        location = GeoPoint(42.7050, 44.6150),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Chaukhi-river.jpg"
                    ),
                    BattleNode(
                        "Shorena's",
                        "Hearty mountain dinner. Try the Khinkali.",
                        "D1 20:00",
                        location = GeoPoint(42.6570, 44.6410),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/06/Emily-Lush-Kazbegi-restaurants-Maisi.jpg"
                    ),

                    // DAY 2: The Dolomites of Georgia
                    BattleNode(
                        "Sno Heads",
                        "In the tiny village of Sno (birthplace of the Georgian Patriarch Ilia II), a surreal collection of sculptures sits in an open field. Merab Piranishvili, a local sculptor, has spent decades carving massive blocks of granite into the faces of Georgia's literary giants: Shota Rustaveli, Ilia Chavchavadze, and Vazha-Pshavela. The heads resemble the Moai of Easter Island. Piranishvili carves them by hand, driven by a dream to create a gallery of 500 national heroes. It is a solitary, monumental labor of love that transforms a rural field into a pantheon.",
                        "D2 09:30",
                        location = GeoPoint(42.6050, 44.6350),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Sno-Giant-Stone-Heads.jpg"
                    ),
                    BattleNode(
                        "Juta Valley",
                        "Hike towards the Chaukhi Massif (Georgian Dolomites).",
                        "D2 11:00",
                        alertType = "HIKING_SHOES",
                        location = GeoPoint(42.5790, 44.7450),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Valley-Hike-Fifth-Season-HERO.jpg"
                    ),
                    BattleNode(
                        "Fifth Season Hut",
                        "Lunch with an alpine view in Juta.",
                        "D2 13:00",
                        location = GeoPoint(42.5720, 44.7500),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Fifth-Season-Cabin.jpg"
                    ),
                    BattleNode(
                        "Dariali Monastery",
                        "The Dariali Monastery of the Archangels Michael and Gabriel is a modern fortress of faith. Construction began in 2005 right next to the Larsi border crossing with Russia. Its location is deeply symbolic, acting as a spiritual sentinel guarding Georgia’s northern gate. The complex is massive, built from grey-rose stone to match the surrounding cliffs of the Dariali Gorge. Despite being new, it adheres strictly to medieval architectural canons, asserting cultural continuity at the edge of the state.",
                        "D2 16:00",
                        location = GeoPoint(42.7420, 44.6290),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Emily-Lush-visit-Kakheti-wine-region-Georgia-Ninotsminda-Cathedral.jpg"
                    ),
                    BattleNode(
                        "Tsdo Village",
                        "Tsdo is a 'ghost village' clinging to the mountainside near the border, with very few permanent residents. It is famous for retaining pre-Christian, animist traditions alongside Orthodoxy. The village has a Khati (shrine) where rituals involving animal sacrifice were historically performed to appease local spirits. These syncretic rituals are characteristic of the Mokheve people of the highlands, who maintained distinct customs separate from the lowlands.",
                        "D2 17:00",
                        location = GeoPoint(42.7010, 44.6300),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-road-walking.jpg"
                    ),
                    BattleNode(
                        "Pasanauri",
                        "Pasanauri is the undisputed capital of the Khinkali dumpling. While urban Tbilisi serves 'Kalakuri' khinkali (filled with meat and herbs like cilantro), Pasanauri serves the original 'Mtiuluri' or 'Khevsuruli' style. The filling is purely minced meat (lamb/beef), onions, chili, salt, and cumin—no greens. Crucially, the meat is traditionally chopped with a dagger rather than ground, retaining a texture and juice that machines cannot replicate.",
                        "D2 19:30",
                        location = GeoPoint(42.3520, 44.6880),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Tbilisi-restaurants-Kade-mountain-khinkali.jpg"
                    )
                )
            )


            // ===================================================================================
            // WEST GEORGIA - COLCHIS LOOP
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "west_colchis_3d",
                title = "West: Canyons & Ruins",
                description = "This route descends into the humid subtropics of Imereti and Samegrelo, the land of the Golden Fleece (Colchis). It contrasts the decay of the Soviet empire with the lush, Jurassic-park nature of the canyons.",
                imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Ultimate-Georgia-itinerary-Stalins-bathhouse-Tskaltubo-Imereti-replacement.jpg",
                category = "NATURE",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 180,
                durationDays = 3,
                route = listOf(GeoPoint(42.2770, 42.7040), GeoPoint(42.4570, 42.3770)),
                itinerary = listOf(
                    // DAY 1: The Abandoned Spas
                    BattleNode(
                        "Sanatorium Medea",
                        "Tskaltubo was once the 'Spa Capital' of the USSR, with a direct train line from Moscow delivering the Soviet elite. Sanatorium Medea, built in the Stalinist Empire style, is the most photogenic of these abandoned giants. Following the collapse of the USSR and the war in Abkhazia (1992-93), thousands of Internally Displaced Persons (IDPs) took refuge in these palaces. For 30 years, families have lived in the crumbling ballrooms and wards. The site is a haunting mix of grand Romanesque colonnades, creeping ivy, and the laundry of refugees hanging between marble pillars—a living museum of displacement.",
                        "D1 10:00",
                        location = GeoPoint(42.3250, 42.6000),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/11/Emily-Lush-Tskaltubo-guide-Sanatorium-Medea-entryway-HERO.jpg"
                    ),
                    BattleNode(
                        "Stalin's Bath",
                        "While most of Tskaltubo rots, Bathhouse No. 6 has been partially restored. This facility was built specifically for Joseph Stalin in 1951. The facade features a relief of Stalin greeting happy citizens. Inside, visitors can see the private pool, tiled with mosaics, where the dictator soaked in the radon-carbonate waters to treat his aching joints. It is the only bathhouse in the park that remains fully operational, offering a surreal opportunity to bathe in the same room as the Soviet leader once did.",
                        "D1 11:30",
                        location = GeoPoint(42.3280, 42.5970),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/11/Emily-Lush-Tskaltubo-guide-Bathhouse-6-entry.jpg"
                    ),
                    BattleNode(
                        "Magnolia",
                        "Magnolia is the primary dining hub in the sleepy town of Tskaltubo. Located in a park setting, it offers a reliable menu of Imeretian classics. Must-Try: Imeretian Khachapuri. Unlike the open-faced Adjarian boat, the Imeretian version is a closed, circular pie. It is thinner, softer, and filled with local Imeretian cheese. It is the standard by which all cheese breads in the region are judged.",
                        "D1 13:00",
                        location = GeoPoint(42.3260, 42.5980),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/11/Emily-Lush-Tskaltubo-guide-Magnolia-restaurant-exterior.jpg"
                    ),
                    BattleNode(
                        "Prometheus Cave",
                        "Discovered in 1984, this cave system is massive, with 22 halls (6 of which are open to tourists). It was renamed 'Prometheus Cave' in 2010 by President Saakashvili to link it to the myth of Prometheus, who was chained to the Caucasus mountains for stealing fire from the gods. The tour concludes with an optional boat ride along the underground Kumi River. Visitors float through narrow, illuminated tunnels 40 meters underground before emerging into the daylight—a magical exit from the underworld.",
                        "D1 15:00",
                        location = GeoPoint(42.3765, 42.6005),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/08/Emily-Lush-Tskaltubo-Tetra-Cave.jpg"
                    ),
                    BattleNode(
                        "Sisters",
                        "Located at 35 Paliashvili Street, 'Sisters' is the soul of Kutaisi dining. Run by... actual sisters, it is housed in a historical 19th-century home with a cozy, vintage interior. It is famous for its live music—often just a piano and a singer performing soulful Georgian ballads or jazz. It feels less like a commercial restaurant and more like a cultured salon from the turn of the century.",
                        "D1 20:00",
                        location = GeoPoint(42.2710, 42.7030),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/09/Emily-Lush-restaurants-in-Kutaisi-new-Sisters-interior.jpg"
                    ),

                    // DAY 2: Canyons
                    BattleNode(
                        "Martvili Canyon",
                        "Martvili is a Jurassic wonderland of moss-covered rocks and turquoise waters. Historically, this was the private bathing pool of the Dadiani noble family, the rulers of Samegrelo. Visitors take inflatable rafts upriver to see the waterfalls. The canyon is also a paleontological site; fossilized dinosaur footprints (75 million years old) and mosasaur remains have been found in the canyon walls, adding to the prehistoric atmosphere.",
                        "D2 10:00",
                        alertType = "BOAT_RIDE",
                        location = GeoPoint(42.4570, 42.3770),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/06/Emily-Lush-Batumi-Botanical-Garden-H-10.jpg"
                    ),
                    BattleNode(
                        "Oda Family Marani",
                        "Located in Martvili, this estate is a benchmark for gastro-tourism. The Gagua family restored their ancestral 'Oda' (traditional wooden Megrelian house) and vineyard to produce organic natural wines. Megrelian food is the spiciest in Georgia. Must-Try: Kupati (spicy sausage flavored with cinnamon and savory) and Elarji (cornmeal whipped with cheese until elastic). They specialize in rare endemic grape varieties like Ojaleshi, grown organically in the humid subtropical climate.",
                        "D2 13:00",
                        location = GeoPoint(42.4100, 42.3700),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/07/Emily-Lush-Imereti-Wine-Route-Baias-Wine-qvevri.jpg"
                    ),
                    BattleNode(
                        "Okatse Canyon",
                        "Hanging walkway over the abyss.",
                        "D2 15:30",
                        location = GeoPoint(42.4550, 42.5500),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/05/Emily-Lush-day-trips-from-Batumi-Waterfall.jpg"
                    ),
                    BattleNode(
                        "Kinchkha Waterfall",
                        "Massive waterfall near Okatse.",
                        "D2 17:00",
                        location = GeoPoint(42.4950, 42.5530),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/05/Emily-Lush-day-trips-from-Batumi-Waterfall.jpg"
                    ),
                    BattleNode(
                        "Bikentia's Kebabery",
                        "Bikentia's (Bikentias Sakababe) is legendary in Kutaisi. There are no chairs. There is no menu variety. Visitors stand at high tables, Soviet-style. They serve only one thing: Beef Kebabs with spicy tomato sauce (Satsebeli) and onions, served with bread. You wash it down with distinctively fizzy Georgian lemonade (usually Pear or Tarragon) or draft beer. It is fast, cheap, and culturally essential.",
                        "D2 20:00",
                        location = GeoPoint(42.2720, 42.7050),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2019/07/Emily-Lush-Kutaisi-Restaurants-Bikentinas.jpg"
                    ),

                    // DAY 3: Batumi Transfer
                    BattleNode(
                        "Bagrati Cathedral",
                        "Bagrati stands on Ukimerioni Hill overlooking Kutaisi. Built in the early 11th century by Bagrat III, it was the symbol of the first unified Kingdom of Georgia. In 1692, it was blown up by the Ottomans and stood as a roofless ruin for 300 years. In 2012, against UNESCO's wishes, the government rebuilt it with modern materials, including a glass elevator. This led to its removal from the UNESCO World Heritage list (though the nearby Gelati Monastery remained listed). Despite the controversy, the reconstruction has restored its function as an active cathedral and a symbol of statehood.",
                        "D3 09:00",
                        location = GeoPoint(42.2770, 42.7040),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/04/Emily-Lush-things-to-do-in-Batumi-Georgia-Cathedral.jpg"
                    ),
                    BattleNode(
                        "Botanical Garden",
                        "Located at Mtsvane Kontskhi (Green Cape), this is one of the largest botanical gardens in the former USSR. It was founded in 1912 by the Russian botanist Andrey Krasnov. The garden is unique because it mimics the climate zones of the world in vertical tiers—starting from the Himalayas at the top and descending to the Mediterranean and humid subtropics at sea level. Krasnov is buried here, overlooking his creation.",
                        "D3 14:00",
                        location = GeoPoint(41.6930, 41.7070),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/06/Emily-Lush-Batumi-Botanical-Garden-H-10.jpg"
                    ),
                    BattleNode(
                        "Fish Market",
                        "The 'Blue Market' near the port is a Batumi institution. Visitors walk through the stalls of fresh catch—Black Sea Turbot (Kalkan), Red Mullet (Barbunia), and Horse Mackerel. You buy your fish by the kilo. Then, you take your bag of raw fish to the adjacent restaurants (like 'Blue Wave'). For a small fee, they clean and fry it immediately, serving it with garlic sauce, lemon, and cold beer. It is the freshest seafood experience in the country.",
                        "D3 17:00",
                        location = GeoPoint(41.6490, 41.6620),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/07/Emily-Lush-things-to-do-in-Batumi-Georgia-Fish-Market-3.jpg"
                    ),
                    BattleNode(
                        "Ali & Nino",
                        "Created by Georgian artist Tamara Kvesitadze, this kinetic sculpture is based on the 1937 novel Ali and Nino by Kurban Said, which tells the tragic love story of a Muslim Azeri boy and a Christian Georgian girl during World War I. Every evening at 7 PM, the two 8-meter steel figures begin to move towards each other. They merge briefly—passing through one another due to their segmented design—and then drift apart again. It symbolizes the eternal cycle of meeting and separation.",
                        "D3 19:00",
                        location = GeoPoint(41.6555, 41.6430),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/07/Emily-Lush-things-to-do-in-Batumi-Georgia-Ali-and-Nino-statue.jpg"
                    ),
                    BattleNode(
                        "Retro",
                        "Adjaruli Khachapuri (the boat-shaped cheese bread with egg) originated here in the Adjara region. Retro is famous for perfecting it. Retro serves a version called the 'Titanic.' It is monstrously large. The dough is crisp, and the cheese is removed from the edges to make it lighter (a technique perfected by the owner, Gia Agirba). The key ritual is to mix the egg and butter into the boiling cheese immediately upon serving to cook the egg.",
                        "D3 20:30",
                        location = GeoPoint(41.6510, 41.6360),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/05/Emily-Lush-Tbilisi-restaurant-Retro.jpg"
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