package com.example.sakartveloguide.data.repository

import android.util.Log
import com.example.sakartveloguide.data.local.dao.TripDao
import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.data.mapper.toDomain
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val dao: TripDao
) : TripRepository {

    override fun getAvailableTrips(): Flow<List<TripPath>> = dao.getAllTrips().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getTripById(id: String): TripPath? = dao.getTripById(id)?.toDomain()

    override suspend fun lockTrip(tripId: String) = dao.updateLockStatus(tripId, true)

    override suspend fun nukeAllData() = dao.nukeTable()

    // HELPER: Compact Translation Wrapper
    // Order: English, Georgian, Russian, Turkish, Armenian, Hebrew, Arabic
    // Update the L helper in TripRepositoryImpl
    private fun L(en: String, ka: String, ru: String, tr: String, hy: String, iw: String, ar: String) =
        LocalizedString(en, ka, ru, tr, hy, iw, ar)

    override suspend fun refreshTrips() {
        try {
            val count = dao.getTripCount()
            if (count > 0) return

            val tripsToInsert = mutableListOf<TripEntity>()

            // --- META SECTOR: SYSTEM & COUNTRY ---
            tripsToInsert += TripEntity(
                id = "meta_tutorial",
                title = L(
                    "SYSTEM TUTORIAL", "სისტემური სახელმძღვანელო", "Системное руководство", "Sistem Eğitimi", "Համակարգի ձեռnարկ", "מדריך מערכת", "دليل النظام"
                ),
                description = L(
                    "Operational briefing: How to navigate the Matrix. TACTICAL NOTE: The 2026 logistical environment requires strict adherence to '3-2-2' timing (3h transport, 2h site, 2h dining).",
                    "ოპერატიული ბრიფინგი: როგორ ვმართოთ მატრიცა. ტაქტიკური შენიშვნა: 2026 წლის ლოგისტიკა მოითხოვს '3-2-2' წესის დაცვას (3სთ გზა, 2სთ ობიექტი, 2სთ კვება).",
                    "Оперативный брифинг. ТАКТИЧЕСКОЕ ПРИМЕЧАНИЕ: Логистика 2026 года требует строгого соблюдения тайминга '3-2-2' (3ч транспорт, 2ч объект, 2ч еда).",
                    "Operasyonel brifing: Matrix'te nasıl gezinilir. TAKTİK NOT: 2026 lojistik ortamı '3-2-2' zamanlamasına (3s ulaşım, 2s saha, 2s yemek) sıkı uyum gerektirir.",
                    "Օპერատիվ ճეպაზրույց. Ինչպես նავიարկել մատրիցայում: ՏԱԿՏԻԿԱԿԱՆ ՆՇՈՒՄ. 2026-ի լոգիստիկ միջավայրը պահանჯում է խստորեն պահպանել «3-2-2» ժամանակացույցը:",
                    "תדריך מבצעי: איך לנווט במטריקס. הערה טקטית: הסביבה הלוגיסטית של 2026 דורשת הקפדה על תזמון '3-2-2'.",
                    "موجز تشغيلي: كيفية التنقل في المصفوفة. ملاحظة تكتيكية: تتطلب البيئة اللوجستية لعام 2026 الالتزام الصارم بتوقيت '3-2-2'."
                ),
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
                title = L(
                    "ABOUT SAKARTVELO", "საქართველოს შესახებ", "О Грузии", "Sakartvelo Hakkında", "Սաքարթველոյի մասին", "על סקרטבלו", "حول ساكارتفيلو"
                ),
                description = L(
                    "Essential Intelligence: Georgia in 2026 defines a tension between 8,000-year history and hyper-modernity. WARNING: Dining is a multi-hour commitment.",
                    "მთავარი მონაცემები: საქართველო 2026 წელს არის 8000-წლიანი ისტორიისა და ჰიპერ-თანამედროვეობის ჭიდილი. გაფრთხილება: ვახშამი აქ საათობით გრძელდება.",
                    "Основные данные: Грузия в 2026 году — это напряжение между 8000-летней историей и гиперсовременностью. ВНИМАНИЕ: Ужин — это обязательство на несколько часов.",
                    "Temel İstihbarat: 2026'da Gürcistan, 8.000 yıllık tarih ile hiper-modernite arasındaki gerilimi tanımlıyor. UYARI: Yemek yemek saatler süren bir taahhüttür.",
                    "Հիմնական հետախուզություն. Վրաստանը 2026 թվականին սահմանում է լարվածություն 8000-ամյա պատմության և հիպեր-արդիականության միջև:",
                    "מודיעין חיוני: גאורגיה בשנת 2026 מגדירה מתח בין היסטוריה של 8,000 שנה להיפר-מודרניות. אזהרה: ארוחת ערב היא התחייבות רב-שעתית.",
                    "المعلومات الأساسية: تحدد جورجيا في عام 2026 التوتر بين تاريخ يمتد لـ 8000 عام والحداثة المفرطة. تحذير: تناول الطعام التزام يستغرق عدة ساعات."
                ),
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
                title = L(
                    "Tbilisi: Silk Road Aristocrat", "თბილისი: აბრეშუმის გზის არისტოკრატი", "Тбилиси: Аристократ Шелкового пути", "Tiflis: İpek Yolu Aristokratı", "Թբիլիսի. Մետաքսի ճանապարհի ազնվական", "טביליסי: אריסטוקרט דרך המשי", "تبليسي: أرستقراطي طريق الحرير"
                ),
                description = L(
                    "Immerse yourself in 19th-century 'Tiflis'. LOGISTICAL AUDIT: Most museums are closed Mondays. Evening meals at nodes like Ezo require a 2.5-hour buffer.",
                    "ჩაეფალით მე-19 საუკუნის 'ტფილისში'. ლოგისტიკა: მუზეუმები ორშაბათობით დაკეტილია. ვახშამი 'ეზოში' 2.5 საათს მოითხოვს.",
                    "Погрузитесь в Тифлис 19-го века. ЛОГИСТИКА: Музеи закрыты по понедельникам. Ужин в таких местах, как Ezo, требует буфера в 2,5 часа.",
                    "Kendinizi 19. yüzyıl 'Tiflis'ine bırakın. LOJİSTİK DENETİM: Çoğu müze Pazartesi günleri kapalıdır. Ezo gibi düğüm noktalarındaki akşam yemekleri 2,5 saatlik bir tampon gerektirir.",
                    "Ընկղմվեք 19-րդ դարի «Թիֆլիսում». ԼՈԳԻՍՏԻԿԱԿԱՆ ԱՈՒԴԻՏ. Թանգարանների մեծ մասը փակ է երկուշաբթի օրերին:",
                    "שקעו ב'טיפליס' של המאה ה-19. ביקורת לוגיסטית: רוב המוזיאונים סגורים בימי שני. ארוחות ערב דורשות חיץ של 2.5 שעות.",
                    "انغمس في 'تفليس' القرن التاسع عشر. التدقيق اللوجستي: معظم المتاحف مغلقة يوم الاثنين. تتطلب وجبات العشاء مخزونًا مؤقتًا لمدة 2.5 ساعة."
                ),
                imageUrl = "https://wander-lush.org/wp-content/uploads/2020/05/Emily-Lush-Tbilisi-in-spring-11.jpg",
                category = "CULTURE",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 60,
                durationDays = 4,
                route = listOf(GeoPoint(41.6925, 44.7985), GeoPoint(41.6880, 44.8085)),
                itinerary = listOf(
                    // DAY 1
                    BattleNode(
                        title = L("Writer’s House", "მწერალთა სახლი", "Дом писателей", "Yazarlar Evi", "Գրողների տուն", "בית הסופרים", "بيت الكتاب"),
                        description = L(
                            "1905 Art Nouveau masterpiece. Open 11:00-18:00. Frequently closed Sundays/Mondays.",
                            "1905 წლის არტ-ნუვოს შედევრი. ღიაა 11:00-18:00. ხშირად დაკეტილია კვირა/ორშაბათს.",
                            "Шедевр модерна 1905 года. Открыто 11:00-18:00. Часто закрыто по воскресеньям/понедельникам.",
                            "1905 Art Nouveau şaheseri. 11:00-18:00 arası açık. Pazar/Pazartesi günleri sıklıkla kapalı.",
                            "1905 Art Nouveau գլուխգործոց. Բաց է 11:00-18:00:",
                            "יצירת מופת בסגנון ארט נובו משנת 1905. פתוח 11:00-18:00.",
                            "تحفة فنية من فن الآرت نوفو عام 1905. مفتوح من 11:00 إلى 18:00."
                        ),
                        timeLabel = "D1 10:00",
                        alertType = "OPENS_11AM",
                        location = GeoPoint(41.6896, 44.8010),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-best-museums-in-Tbilisi-Georgia-David-Sarajashvili-Museum.jpg"
                    ),
                    BattleNode(
                        title = L("Kalantarov House", "კალანტაროვის სახლი", "Дом Калантарова", "Kalantarov Evi", "Կալանտարովի տուն", "בית קלנטרוב", "منزل كالانتاروف"),
                        description = L(
                            "Pseudo-Moorish gem (1908). The 'Opera House for Love.'",
                            "ფსევდო-მავრიტანული მარგალიტი (1908). 'ოპერის სახლი სიყვარულისთვის'.",
                            "Псевдомавританская жемчужина (1908). 'Оперный театр любви'.",
                            "Sözde Mağribi mücevheri (1908). 'Aşk için Opera Binası'.",
                            "Կեղծ-մավրիտանական գոհար (1908). «Օպերային թատրոն սիրո համար».",
                            "פנינה פסאודו-מורית (1908). 'בית האופרה לאהבה'.",
                            "جوهرة مغاربية زائفة (1908). 'دار الأوبرا للحب'."
                        ),
                        timeLabel = "D1 11:30",
                        location = GeoPoint(41.6894, 44.8015),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-52-things-to-do-in-tbilisi-georgia-Sololaki-house.jpg"
                    ),
                    BattleNode(
                        title = L("Cafe Littera", "კაფე ლიტერა", "Кафе Литтера", "Cafe Littera", "Սրճարան Littera", "קפה ליטרה", "مقهى ليتر"),
                        description = L(
                            "Chef Tekuna Gachechiladze's fusion lab. TACTICAL DINING: Do not rush.",
                            "შეფ თეკუნა გაჩეჩილაძის ფიუჟენ ლაბორატორია. ტაქტიკური კვება: არ იჩქაროთ.",
                            "Фьюжн-лаборатория шеф-повара Текуны Гачечиладзе. ТАКТИЧЕСКИЙ УЖИН: Не торопитесь.",
                            "Şef Tekuna Gachechiladze'nin füzyon laboratuvarı. TAKTİK YEMEK: Acele etmeyin.",
                            "Շեֆ Թեկունա Գաչեչիլաձեի ֆյուժն լաբորատորիան. ՏԱԿՏԻԿԱԿԱՆ ՃԱՇՈՒՄ. Մի շտապեք:",
                            "מעבדת הפיוז'ן של השפית טקונה גאצ'צ'ילדזה. ארוחה טקטית: אל תמהרו.",
                            "مختبر الاندماج للشيف تيكونا جاشيشيلادزه. تناول الطعام التكتيكي: لا تتعجل."
                        ),
                        timeLabel = "D1 13:00",
                        location = GeoPoint(41.6896, 44.8010),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/06/Emily-Lush-restaurants-in-Tbilisi-new-Cafe-Littera-food.jpg"
                    ),
                    BattleNode(
                        title = L("Galaktioni 22", "გალაკტიონის 22", "Галактиони 22", "Galaktioni 22", "Գալակտիոնի 22", "גלקטיוני 22", "جالاكتيوني 22"),
                        description = L(
                            "Entrance Hall of the Seilanov brothers. Features the 'SALVE' mosaic.",
                            "ძმები სეილანოვების სადარბაზო. იატაკზე დაგხვდებათ წარწერა 'SALVE'.",
                            "Парадная братьев Сейлановых. Мозаика 'SALVE'.",
                            "Seilanov kardeşlerin Giriş Salonu. 'SALVE' mozaiğine sahiptir.",
                            "Սեյլանով եղբայրների մուտքի սրահը. Ներկայացված է «SALVE» խճանկարը:",
                            "אולם הכניסה של האחים סיילאנוב. כולל את פסיפס 'SALVE'.",
                            "قاعة مدخل الأخوين سيلانوف. تتميز بفسيفساء 'SALVE'."
                        ),
                        timeLabel = "D1 15:00",
                        location = GeoPoint(41.6910, 44.7995),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-52-things-to-do-in-tbilisi-georgia-Sololaki-entryway.jpg"
                    ),
                    BattleNode(
                        title = L("Gudiashvili Square", "გუდიაშვილის მოედანი", "Площадь Гудиашвили", "Gudiashvili Meydanı", "Գուդիաշվիլու հրապարակ", "כיכר גודיאשווילי", "ساحة جودياشفيلي"),
                        description = L(
                            "Medieval urban fabric saved by civic activism.",
                            "სამოქალაქო აქტივიზმით გადარჩენილი უბანი.",
                            "Средневековая городская ткань, спасенная гражданским активизмом.",
                            "Sivil aktivizmle kurtarılan ortaçağ kentsel dokusu.",
                            "Միջնադարյան քաղաքային գործվածք, որը փրկվել է քաղաքացիական ակտիվության շնորհիվ:",
                            "מרקם עירוני מימי הביניים ניצל על ידי אקטיביזם אזרחי.",
                            "النسيج الحضري في العصور الوسطى الذي تم إنقاذه من خلال النشاط المدني."
                        ),
                        timeLabel = "D1 16:30",
                        location = GeoPoint(41.6935, 44.8005),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-Gudiashvili-Square.jpg"
                    ),
                    BattleNode(
                        title = L("Ezo Restaurant", "რესტორანი ეზო", "Ресторан Эзо", "Ezo Restoran", "Ռեստորան Էզո", "מסעדת אזו", "مطعم ايزو"),
                        description = L(
                            "Organic, farm-to-table sourcing means slow food. Allocate 2.5 hours.",
                            "ნატურალური პროდუქტები ნიშნავს ნელ მომზადებას. გამოყავით 2.5 საათი.",
                            "Органические продукты означают 'медленную еду'. Выделите 2,5 часа.",
                            "Organik, tarladan sofraya kaynak kullanımı yavaş yemek demektir. 2,5 saat ayırın.",
                            "Օրգանական ծագումը նշանակում է դանդաղ սնունդ: Տրամադրել 2,5 ժամ:",
                            "מקור אורגני, מהחווה לשולחן, פירושו אוכל איטי. הקצו 2.5 שעות.",
                            "المصادر العضوية تعني الطعام البطيء. خصص 2.5 ساعة."
                        ),
                        timeLabel = "D1 19:30",
                        alertType = "SLOW_FOOD",
                        location = GeoPoint(41.6905, 44.7990),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-52-things-to-do-in-tbilisi-georgia-Sololaki-courtyard.jpg"
                    ),

                    // DAY 2
                    BattleNode(
                        title = L("Narikala Fortress", "ნარიყალა", "Крепость Нарикала", "Narikala Kalesi", "Նարիկալա ամրոց", "מבצר נריקלה", "قلعة ناريكالا"),
                        description = L(
                            "The Acropolis of Tbilisi. Steep hike. Definitive views.",
                            "თბილისის აკროპოლისი. ციცაბო აღმართი. საუკეთესო ხედები.",
                            "Акрополь Тбилиси. Крутой подъем. Лучшие виды.",
                            "Tiflis Akropolisi. Dik yürüyüş. Kesin manzaralar.",
                            "Թբիլիսիի Ակրոպոլիս. Զառիթափ արշավ. Վերջնական տեսակետներ.",
                            "האקרופוליס של טביליסי. טיול תלול. נופים סופיים.",
                            "أكروبوليس تبليسي. ارتفاع حاد. وجهات نظر نهائية."
                        ),
                        timeLabel = "D2 09:00",
                        alertType = "STEEP_HIKE",
                        location = GeoPoint(41.6880, 44.8085),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/01/Emily-Lush-Tbilisi-Georgia-Kartlis-Deda-drone.jpg"
                    ),
                    BattleNode(
                        title = L("Botanical Garden", "ბოტანიკური ბაღი", "Ботанический сад", "Botanika Bahçesi", "Բուսաբանական այգի", "גן בוטני", "حديقة نباتية"),
                        description = L(
                            "Hidden waterfall behind the fortress.",
                            "დამალული ჩანჩქერი ციხესიმაგრის მიღმა.",
                            "Скрытый водопад за крепостью.",
                            "Kalenin arkasındaki gizli şelale.",
                            "Թաքնված ջրվեժ ամրոցի հետևում.",
                            "מפל נסתר מאחורי המבצר.",
                            "شلال مخفي خلف القلعة."
                        ),
                        timeLabel = "D2 11:00",
                        location = GeoPoint(41.6870, 44.8060),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Emily-Lush-best-Tbilisi-views-Turtle-Lake-hills-view.jpg"
                    ),
                    BattleNode(
                        title = L("Culinarium Khasheria", "კულინარიუმ ხაშერია", "Кулинариум Хашерия", "Culinarium Khasheria", "Խաշերիա", "קולינריום חאשריה", "الطهي خاشيريا"),
                        description = L(
                            "Famous for Khashi (tripe/garlic soup). Historically a hangover cure.",
                            "ცნობილია ხაშით. ისტორიულად, ნაბახუსევის წამალი.",
                            "Знаменит хаши (суп из рубца с чесноком). Исторически средство от похмелья.",
                            "Khashi (işkembe/sarımsak çorbası) ile ünlüdür. Tarihsel olarak akşamdan kalma tedavisi.",
                            "Հայտնի է Խաշիով (փորոտիք/սխտորով ապուր): Պատմականորեն խումհարի բուժում:",
                            "מפורסמת בזכות חאשי. תרופה היסטורית להנגאובר.",
                            "يشتهر بالخاشي (حساء الكرشة/الثوم). تاريخيا علاج مخلفات."
                        ),
                        timeLabel = "D2 13:00",
                        location = GeoPoint(41.6885, 44.8105),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/06/Emily-Lush-restaurants-in-Tbilisi-new-Culinarium-food.jpg"
                    ),
                    BattleNode(
                        title = L("Orbeliani Baths", "ორბელიანის აბანო", "Орбелиановские бани", "Orbeliani Hamamları", "Օրբելիանի բաղնիքներ", "מרחצאות אורבליאני", "حمامات أوربيلياني"),
                        description = L(
                            "The 'Blue Bath' with Persian mosaic facade. Reservation required.",
                            "'ჭრელი აბანო' სპარსული მოზაიკით. საჭიროა ჯავშანი.",
                            "'Голубая баня' с персидским мозаичным фасадом. Требуется бронирование.",
                            "Fars mozaiği cepheli 'Mavi Hamam'. Rezervasyon gerekli.",
                            "«Կապույտ բաղնիք»՝ պարսկական խճանկարային ճակատով։ Անհրաժեշտ է ամրագրում:",
                            "'האמבט הכחול' עם חזית פסיפס פרסית. דרושה הזמנה.",
                            "'الحمام الأزرق' بواجهة فسيفساء فارسية. الحجز مطلوب."
                        ),
                        timeLabel = "D2 14:30",
                        alertType = "RESERVATION_NEEDED",
                        location = GeoPoint(41.6883, 44.8109),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-Chreli-Abano-sulfur-baths.jpg"
                    ),
                    BattleNode(
                        title = L("Jumah Mosque", "ჯუმა მეჩეთი", "Джума-мечеть", "Cuma Camii", "Ջումա մզկիթ", "מסגד ג'ומה", "مسجد الجمعة"),
                        description = L(
                            "Sunnis and Shias pray side-by-side. A rare example of intra-faith unity.",
                            "სუნიტები და შიიტები გვერდიგვერდ ლოცულობენ. რელიგიური ერთიანობის იშვიათი მაგალითი.",
                            "Сунниты и шииты молятся бок о бок. Редкий пример единства.",
                            "Sünniler ve Şiiler yan yana ibadet ediyor. Nadir bir inanç içi birlik örneği.",
                            "Սուննիներն ու շիաները աղոթում են կողք կողքի. Ներհավատքային միասնության հազվագյուտ օրինակ:",
                            "סונים ושיעים מתפללים זה לצד זה. דוגמה נדירה לאחדות תוך-דתית.",
                            "يصلي السنة والشيعة جنباً إلى جنب. مثال نادر على الوحدة داخل العقيدة."
                        ),
                        timeLabel = "D2 17:00",
                        location = GeoPoint(41.6875, 44.8115),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/10/Emily-Lush-Tbilisi-State-Academy-of-Arts-mosque.jpg"
                    ),
                    BattleNode(
                        title = L("Gabriadze Theater", "გაბრიაძის თეატრი", "Театр Габриадзе", "Gabriadze Tiyatrosu", "Գաբրիաձե թատրոն", "תיאטרון גבריאדזה", "مسرح غابريادزي"),
                        description = L(
                            "Fairy-tale clock tower. Watch the mechanical angel strike the bell at 19:00.",
                            "ზღაპრული საათის კოშკი. დაელოდეთ ანგელოზს 19:00 საათზე.",
                            "Сказочная часовая башня. Ангел бьет в колокол в 19:00.",
                            "Masalsı saat kulesi. Mekanik meleğin saat 19:00'da zili çalmasını izleyin.",
                            "Հեքիաթային ժամացույցի աշտարակ. Դիտեք մեխանիկական հրեշտակին, որը խփում է զանգը 19:00-ին:",
                            "מגדל שעון מהאגדות. צפו במלאך המכני מכה בפעמון בשעה 19:00.",
                            "برج الساعة الخيالي. شاهد الملاك الميكانيكي يضرب الجرس الساعة 19:00."
                        ),
                        timeLabel = "D2 19:00",
                        location = GeoPoint(41.6958, 44.8065),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-Gabriadze-Marionette-Theatre.jpg"
                    ),

                    // DAY 3
                    BattleNode(
                        title = L("National Museum", "ეროვნული მუზეუმი", "Национальный музей", "Ulusal Müze", "Ազգային թանգարան", "המוזיאון הלאומי", "المتحف الوطني"),
                        description = L(
                            "Home of the Golden Fleece. Closed Mondays. Open 10:00-18:00.",
                            "ოქროს საწმისის სახლი. დაკეტილია ორშაბათობით. ღიაა 10:00-18:00.",
                            "Дом Золотого руна. Закрыт по понедельникам. Открыт 10:00-18:00.",
                            "Altın Post'un Evi. Pazartesi günleri kapalıdır. 10:00-18:00 saatleri arasında açıktır.",
                            "Ոսկե գեղμի տուն. Երկուշաբթի օրը փակ է: Բաց է 10:00-18:00:",
                            "הבית של גיזת הזהב. סגור בימי שני. פתוח 10:00-18:00.",
                            "منزل الصوف الذهبي. مغلق الاثنين. مفتوح من 10:00 إلى 18:00."
                        ),
                        timeLabel = "D3 10:00",
                        alertType = "CLOSED_MONDAYS",
                        location = GeoPoint(41.6963, 44.8002),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-best-museums-in-Tbilisi-Georgia-National-Museum-Soviet-Occupation-Hall-desk.jpg"
                    ),
                    BattleNode(
                        title = L("Blue Gallery", "ცისფერი გალერეა", "Голубая галерея", "Mavi Galeri", "Կապույտ պատկերասրահ", "הגלריה הכחולה", "المعرض الأزرق"),
                        description = L(
                            "Niko Pirosmani's masterpieces. Closed Mondays.",
                            "ნიკო ფიროსმანის შედევრები. დაკეტილია ორშაბათობით.",
                            "Шедевры Нико Пиросмани. Закрыта по понедельникам.",
                            "Niko Pirosmani'nin başyapıtları. Pazartesi günleri kapalıdır.",
                            "Նիկո Փիրոսմանիի գլուխգործոցները. Երկուշաբթի օրը փակ է:",
                            "יצירות המופת של ניקו פירוסמני. סגור בימי שני.",
                            "روائع نيكو بيروسماني. مغلق الاثنين."
                        ),
                        timeLabel = "D3 12:30",
                        alertType = "CLOSED_MONDAYS",
                        location = GeoPoint(41.6970, 44.7990),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-Sighnaghi-Museum-Kakheti-Pirosmani-Fruit-Stall.jpg"
                    ),
                    BattleNode(
                        title = L("Barbarestan", "ბარბარესთან", "Барбарестан", "Barbarestan", "Բարբարեստան", "ברברסטן", "باربارستان"),
                        description = L(
                            "Culinary Museum based on 1874 cookbook. Booking ahead is mandatory.",
                            "1874 წლის რეცეპტების წიგნზე დაფუძნებული რესტორანი. ჯავშანი აუცილებელია.",
                            "Кулинарный музей по поваренной книге 1874 года. Бронирование обязательно.",
                            "1874 tarihli yemek kitabına dayanan Mutfak Müzesi. Önceden rezervasyon zorunludur.",
                            "Խոհարարական թանգարան՝ հիմնված 1874 թվականի խոհարարական գրքի վրա։ Ամրագրումը պարտադիր է:",
                            "מוזיאון קולינרי המבוסס על ספר בישול משנת 1874. הזמנה מראש חובה.",
                            "متحف الطهي يعتمد على كتاب طبخ من عام 1874. الحجز المسبق إلزامي."
                        ),
                        timeLabel = "D3 14:00",
                        alertType = "BOOK_AHEAD",
                        location = GeoPoint(41.7105, 44.7960),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/05/Emily-Lush-Tbilisi-restaurant-Barbarestan.jpg"
                    ),
                    BattleNode(
                        title = L("Dry Bridge Market", "მშრალი ხიდის ბაზრობა", "Сухой мост", "Kuru Köprü Pazarı", "Չոր կամուրջի շուկա", "שוק הגשר היבש", "سوق الجسر الجاف"),
                        description = L(
                            "Open-air museum of the Soviet collapse. Best visited on weekends.",
                            "საბჭოთა კავშირის დაშლის ღია მუზეუმი. საუკეთესოა შაბათ-კვირას.",
                            "Музей распада СССР под открытым небом. Лучше посещать в выходные.",
                            "Sovyet çöküşünün açık hava müzesi. En iyi hafta sonları ziyaret edilir.",
                            "Խորհրդային փլուզման բացօթյա թանգարան. Լավագույնս այցելել հանգստյան օրերին:",
                            "מוזיאון באוויר הפתוח של ההתמוטטות הסובייטית. מומלץ לבקר בסופי שבוע.",
                            "متحف في الهواء الطلق للانهيار السوفيتي. أفضل زيارة في عطلة نهاية الأسبوع."
                        ),
                        timeLabel = "D3 16:00",
                        location = GeoPoint(41.7005, 44.8055),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-Dry-Bridge-antiques.jpg"
                    ),
                    BattleNode(
                        title = L("Mtatsminda Funicular", "მთაწმინდის ფუნიკულიორი", "Фуникулер Мтацминда", "Mtatsminda Füniküleri", "Մթացմինդա ճոպանուղի", "פוניקולר מתאצמינדה", "قطار متاتسميندا"),
                        description = L(
                            "1905 Belgian engineering. Climbs 501 meters. Essential for sunset.",
                            "1905 წლის ბელგიური ინჟინერია. ადის 501 მეტრზე. აუცილებელია მზის ჩასვლისთვის.",
                            "Бельгийская инженерия 1905 года. Подъем на 501 метр. Обязателен для заката.",
                            "1905 Belçika mühendisliği. 501 metre tırmanıyor. Gün batımı için gerekli.",
                            "1905 թվականի բելգիական ճարտարագիտություն. Բարձրանում է 501 մետր։",
                            "הנדסה בלגית משנת 1905. מטפס 501 מטר.",
                            "هندسة بلجيكية عام 1905. يتسلق 501 مترا. ضروري لغروب الشمس."
                        ),
                        timeLabel = "D3 18:30",
                        location = GeoPoint(41.6948, 44.7845),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/10/Emily-Lush-Mtatsminda-Cable-Car-Lower-Station-building-restored.jpg"
                    ),
                    BattleNode(
                        title = L("Funicular Restaurant", "რესტორანი ფუნიკულიორი", "Ресторан Фуникулер", "Füniküler Restoran", "Ֆունիկուլյոր ռեստորան", "מסעדת פוניקולר", "مطعم القطار الجبلي المائل"),
                        description = L(
                            "The Ritual: Ponchiki (donuts) and Lagidze water. A classic Tbilisi tradition.",
                            "რიტუალი: ფუნჩულა და ლაღიძის წყლები. კლასიკური თბილისური ტრადიცია.",
                            "Ритуал: Пончики и воды Лагидзе. Классическая тбилисская традиция.",
                            "Ritüel: Ponchiki (çörek) ve Lagidze suyu. Klasik bir Tiflis geleneği.",
                            "Ծես. Պոնչիկի (пончики) և Լագիձե ջուր: Թբիլիսիի դասական ավանդույթ:",
                            "הריטואל: פונצ'יקי (סופגניות) ומים לג'ידזה. מסורת טביליסית קלאסית.",
                            "الطقوس: بونشيكي (الكعك) ومياه لاغيدزي. تقليد تبليسي كلاسيكي."
                        ),
                        timeLabel = "D3 20:00",
                        location = GeoPoint(41.6948, 44.7800),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/10/Emily-Lush-Mtatsminda-Cafe-Funicular-ponchiki-pumpkin-cream.jpg"
                    )
                )
            )

            // ===================================================================================
            // THEME 2: CAPITAL (TBILISI) - BOHEMIAN & BRUTALIST
            // ===================================================================================
            tripsToInsert += TripEntity(
                id = "tbilisi_brutalist_3d",
                title = L(
                    "Tbilisi: Bohemian & Brutalist", "თბილისი: ბოჰემური და ბრუტალისტური", "Тбилиси: Богемный и Бруталистский", "Tiflis: Bohem ve Brütalist", "Թբիլիսի. բոհեմական և բրուտալիստական", "טביליסי: בוהמי וברוטליסטי", "تبليسي: البوهيمية والوحشية"
                ),
                description = L(
                    "Soviet Space City architecture. LOGISTICS: Nutsubidze elevator requires 20 tetri coins. Bassiani door opens 23:59.",
                    "საბჭოთა 'კოსმოსური ქალაქის' არქიტექტურა. ლოგისტიკა: ნუცუბიძის ლიფტს სჭირდება 20 თეთრიანი. ბასიანი იხსნება 23:59-ზე.",
                    "Архитектура советского космического города. ЛОГИСТИКА: Лифт Нуцубидзе требует монеты 20 тетри. Бассиани открывается в 23:59.",
                    "Sovyet Uzay Şehri mimarisi. LOJİSTİK: Nutsubidze asansörü 20 tetri madeni para gerektirir. Bassiani kapısı 23:59'da açılır.",
                    "Խորհրդային տիեզերական քաղաքի ճարտարապետություն. ԼՈԳԻՍՏԻԿԱ. Նուցուբիձեի վերելակի համար պահանջվում է 20 թետրի մետաղադրամ:",
                    "אדריכלות עיר החלל הסובייטית. לוגיסטיקה: מעלית נוצובידזה דורשת מטבעות של 20 טטרי. דלת בסיאני נפתחת ב-23:59.",
                    "عمارة مدينة الفضاء السوفيتية. الخدمات اللوجستية: مصعد نوتسوبيدزي يتطلب عملات 20 تيتري."
                ),
                imageUrl = "https://images.pexels.com/photos/35563480/pexels-photo-35563480.jpeg",
                category = "URBAN_EXPLORER",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 90,
                durationDays = 3,
                route = listOf(GeoPoint(41.7355, 44.7708), GeoPoint(41.7095, 44.8028)),
                itinerary = listOf(
                    // DAY 1
                    BattleNode(
                        title = L("Bank of Georgia HQ", "საქართველოს ბანკის სათაო ოფისი", "Штаб-квартира Банка Грузии", "Gürcistan Bankası Genel Merkezi", "Վրաստանի բանկի կենտրոնակայանը", "מטה בנק גאורגיה", "مقر بنك جورجيا"),
                        description = L(
                            "The 'Space City' (1975). Soviet Brutalist icon.",
                            "კოსმოსური ქალაქი (1975). საბჭოთა ბრუტალიზმის ხატი.",
                            "'Космический город' (1975). Икона советского брутализма.",
                            "'Uzay Şehri' (1975). Sovyet Brütalist ikonu.",
                            "«Տիեզերական քաղաք» (1975). Խորհրդային բրուտալիստական ​​պատկերակ:",
                            "'עיר החלל' (1975). אייקון ברוטליסטי סובייטי.",
                            "'مدينة الفضاء' (1975). أيقونة وحشية سوفيتية."
                        ),
                        timeLabel = "D1 10:00",
                        location = GeoPoint(41.7355, 44.7708),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-Former-Ministry-of-Roads.jpg"
                    ),
                    BattleNode(
                        title = L("Skybridge Plato", "ნუცუბიძის პლატოს საჰაერო ხიდები", "Воздушные мосты Нуцубидзе", "Nutsubidze Gökyüzü Köprüsü", "Նուցուբիձեի օդային կամուրջներ", "גשרי נուצובידזה", "جسر نوتسوبيدزي"),
                        description = L(
                            "Carry 20 Tetri coin for elevator.",
                            "გააყოლეთ 20 თეთრიანი ლიფტისთვის.",
                            "Возьмите 20 тетри для лифта.",
                            "Asansör için 20 Tetri bozuk para bulundurun.",
                            "Վերելակի համար վերցրեք 20 թեթրի մետաղադրամ:",
                            "קחו מטבע של 20 תטრი למעלית.",
                            "احمل عملة 20 تيتري للمصعد."
                        ),
                        timeLabel = "D1 11:30",
                        alertType = "COIN_REQUIRED_20T",
                        location = GeoPoint(41.7270, 44.7350),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Emily-Lush-Nutsubidze-Skybridge-Tbilisi-new-bridges.jpg"
                    ),
                    BattleNode(
                        title = L("Mapshalia", "მაფშალია", "Мапшалия", "Mapshalia", "Մափշալիա", "מפשליה", "مابشاليا"),
                        description = L(
                            "Soviet-style workers' canteen. Cheap, spicy Megrelian food.",
                            "საბჭოთა სტილის სასადილო. იაფი, ცხარე მეგრული კერძები.",
                            "Рабочая столовая в советском стиле. Дешевая, острая мегрельская еда.",
                            "Sovyet tarzı işçi kantini. Ucuz, baharatlı Megrel yemekleri.",
                            "Խորհրդային ոճի բանվորական ճաշարան. Էժան, կծու մեգրելական սնունդ:",
                            "מזנון עובדים בסגנון סובייטי. אוכל מגרלי זול וחריף.",
                            "مقصف العمال على الطراز السوفيتي. طعام ميغريلي رخيص وحار."
                        ),
                        timeLabel = "D1 13:30",
                        location = GeoPoint(41.7085, 44.7965),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/05/Emily-Lush-Tbilisi-restaurant-Mapshalia.jpg"
                    ),
                    BattleNode(
                        title = L("Archaeology Museum", "არქეოლოგიის მუზეუმი", "Музей археологии", "Arkeoloji Müzesi", "Հնագիտության թանգարան", "המוזיאון לארכיאולוגיה", "متحف الآثار"),
                        description = L(
                            "Abandoned brutalist giant. Great for photography.",
                            "მიტოვებული ბრუტალისტური გიგანტი. კარგია ფოტოგრაფიისთვის.",
                            "Заброшенный гигант брутализма. Отлично подходит для фотосъемки.",
                            "Terk edilmiş brütalist dev. Fotoğrafçılık için harika.",
                            "Լքված բրուտալիստական ​​հսկա. Հիանալի է լուսանկարչության համար:",
                            "ענק ברוטליסטי נטוש. נהדר לצילום.",
                            "عملاق وحشي مهجور. رائع للتصوير الفوتوغرافي."
                        ),
                        timeLabel = "D1 15:00",
                        location = GeoPoint(41.7750, 44.7670),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-Former-Archaeology-Museum.jpg"
                    ),
                    BattleNode(
                        title = L("Chronicles of Georgia", "საქართველოს მატიანე", "Хроники Грузии", "Gürcistan Günlükleri", "Վրաստանի տարեգրություն", "כרוניקות של גאורגיה", "سجلات جورجيا"),
                        description = L(
                            "The 'Georgian Stonehenge'. Very cold in January.",
                            "'საქართველოს სტოუნჰენჯი'. იანვარში ძალიან ცივა.",
                            "'Грузинский Стоунхендж'. В январе очень холодно.",
                            "'Gürcü Stonehenge'i. Ocak ayında çok soğuk.",
                            "«Վրացական Սթոունհենջը». Հունվարին շատ ցուրտ է:",
                            "'סטונהנג' הגאורגי'. קר מאוד בינואר.",
                            "'ستونهنج الجورجي'. بارد جدا في يناير."
                        ),
                        timeLabel = "D1 17:00",
                        alertType = "WIND_CHILL",
                        location = GeoPoint(41.7705, 44.8105),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-Chronicles-of-Georgia.jpg"
                    ),
                    BattleNode(
                        title = L("Stamba Hotel", "სტამბა", "Отель Стамба", "Stamba Otel", "Հյուրանոց Ստամբա", "מלון סטמבה", "فندق ستامبا"),
                        description = L(
                            "Converted 1930s publishing house. High-end dining node.",
                            "გადაკეთებული 1930-იანი წლების გამომცემლობა. მაღალი კლასის ვახშამი.",
                            "Переоборудованное издательство 1930-х годов. Ужин высокого класса.",
                            "Dönüştürülmüş 1930'lar yayınevi. Üst düzey yemek noktası.",
                            "Վերափոխված 1930-ականների հրատարակչություն. Բարձրակարգ ճաշի հանգույց:",
                            "בית הוצאה לאור משנות ה-30 שהוסב. צומת אוכל יוקרתי.",
                            "دار نشر تم تحويلها في ثلاثينيات القرن العشرين. عقدة طعام راقية."
                        ),
                        timeLabel = "D1 20:00",
                        location = GeoPoint(41.7060, 44.7865),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Tbilisi-restaurant-Cafe-Stamba-Space-Farms.jpg"
                    ),

                    // DAY 2
                    BattleNode(
                        title = L("Fabrika", "ფაბრიკა", "Фабрика", "Fabrika", "Ֆաբրիկա", "מפעל פבריקה", "فابريكا"),
                        description = L(
                            "Old sewing factory turned creative hub.",
                            "ძველი სამკერვალო ფაბრიკა.",
                            "Старая швейная фабрика, ставшая креативным хабом.",
                            "Eski dikiş fabrikası yaratıcı merkeze dönüştü.",
                            "Հին կարի ֆաբրիկան վերածվել է ստեղծագործական կենտրոնի:",
                            "מפעל תפירה ישן שהפך למרכז יצירתי.",
                            "مصنع خياطة قديم تحول إلى مركز إبداعي."
                        ),
                        timeLabel = "D2 11:00",
                        location = GeoPoint(41.7095, 44.8028),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2017/09/EmilyLushTbilisiGeorgia-24.jpg"
                    ),
                    BattleNode(
                        title = L("Shavi Lomi", "შავი ლომი", "Шави Ломи", "Shavi Lomi", "Շավի Լոմի", "שאבי לומי", "شافي لومي"),
                        description = L(
                            "Birthplace of modern Georgian fusion. Hidden location.",
                            "თანამედროვე ქართული ფიუჟენის დაბადების ადგილი.",
                            "Место рождения современного грузинского фьюжн.",
                            "Modern Gürcü füzyonunun doğum yeri. Gizli konum.",
                            "Ժամանակակից վրացական ֆյուժնի ծննդավայրը. Թաքնված գտնվելու վայրը:",
                            "מקום הולדתו של הפיוז'ן הגאורגי המודרני. מיקום נסתר.",
                            "مسقط رأس الاندماج الجورجي الحديث. موقع مخفي."
                        ),
                        timeLabel = "D2 13:30",
                        location = GeoPoint(41.7115, 44.8040),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/07/Emily-Lush-Shavi-Lomi-Gobi-sharing-bowl.jpg"
                    ),
                    BattleNode(
                        title = L("Vodkast Records", "Vodkast Records", "Vodkast Records", "Vodkast Records", "Vodkast Records", "Vodkast Records", "فودكاست للتسجيلات"),
                        description = L(
                            "Vinyl digging and electronic culture.",
                            "ვინილები და ელექტრონული კულტურა.",
                            "Винил и электронная культура.",
                            "Plak kazma ve elektronik kültür.",
                            "Վինիլային և էլեկտրոնային մշակույթ:",
                            "חפירות תקליטים ותרבות אלקטרונית.",
                            "حفر الفينيل والثقافة الإلكترونية."
                        ),
                        timeLabel = "D2 15:30",
                        location = GeoPoint(41.7045, 44.7900),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/08/Emily-Lush-bars-in-Tbilisi-Craft-Wine-Bar-pink.jpg"
                    ),
                    BattleNode(
                        title = L("Wine Factory N1", "ღვინის ქარხანა N1", "Винзавод N1", "Şarap Fabrikası N1", "Գինու գործարան N1", "מפעל יין N1", "مصنع النبيذ N1"),
                        description = L(
                            "Historic 1896 factory. Great for pre-drinks.",
                            "1896 წლის ისტორიული ქარხანა. კარგია სასმელებისთვის.",
                            "Исторический завод 1896 года. Отлично подходит для аперитива.",
                            "Tarihi 1896 fabrikası. Ön içkiler için harika.",
                            "Պատմական 1896 գործարան. Հիանալի է նախնական խմիչքների համար:",
                            "מפעל היסטורי משנת 1896. נהדר למשקאות לפני יציאה.",
                            "مصنع تاريخي عام 1896. عظيم للمشروبات المسبقة."
                        ),
                        timeLabel = "D2 18:00",
                        location = GeoPoint(41.7065, 44.7830),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/04/Emily-Lush-52-things-to-do-in-tbilisi-georgia-wine-factory-n1-terrace.jpg"
                    ),
                    BattleNode(
                        title = L("Bassiani / Khidi", "ბასიანი / ხიდი", "Бассиани / Хиди", "Bassiani / Khidi", "Բասիանի / Խիդի", "בסיאני / חידי", "باسياني / خيدي"),
                        description = L(
                            "The Techno Cathedral. Door opens 23:59. Strict Face Control.",
                            "ტექნოს ტაძარი. კარი იღება 23:59-ზე. მკაცრი ფეისკონტროლი.",
                            "Собор техно. Двери открываются в 23:59. Строгий фейс-контроль.",
                            "Tekno Katedrali. Kapı 23:59'da açılıyor. Sıkı Yüz Kontrolü.",
                            "Տեխնո տաճար. Դուռը բացվում է 23:59-ին։ Խիստ դեմքի վերահսկում:",
                            "קתדרלת הטכנו. דלת נפתחת ב-23:59. בקרת פנים קפדנית.",
                            "كاتدرائية التكنو. يفتح الباب 23:59. تحكم صارم في الوجه."
                        ),
                        timeLabel = "D2 23:55",
                        alertType = "FACE_CONTROL",
                        location = GeoPoint(41.7230, 44.7897),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-metro-station-artwork.jpg"
                    ),

                    // DAY 3
                    BattleNode(
                        title = L("Palace of Rituals", "რიტუალების სასახლე", "Дворец торжественных обрядов", "Ritüeller Sarayı", "Ծեսերի պալատ", "ארמון הטקסים", "قصر الطقوس"),
                        description = L(
                            "1984 'Wedding Palace'. Phallic/Medieval fusion.",
                            "1984 წლის ქორწინების სახლი. ფალიკური/შუასაუკუნეების ფიუჟენი.",
                            "Дворец бракосочетания 1984 года. Фаллический/средневековый фьюжн.",
                            "1984 'Düğün Sarayı'. Fallik/Ortaçağ füzyonu.",
                            "1984 «Հարսանյաց պալատ». Ֆալլիկ/միջնադարյան միաձուլում:",
                            "1984 'ארמון החתונה'. פיוז'ן פאלי/ימי הביניים.",
                            "1984 'قصر الزفاف'. الانصهار القضيبي / القرون الوسطى."
                        ),
                        timeLabel = "D3 12:00",
                        location = GeoPoint(41.6780, 44.8320),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-Palace-of-Rituals-view.jpg"
                    ),
                    BattleNode(
                        title = L("Expo Georgia", "ექსპო ჯორჯია", "Экспо Джорджия", "Expo Georgia", "Էքսպո Ջորջիա", "אקספו גאורגיה", "إكسبو جورجيا"),
                        description = L(
                            "Soviet Modernist pavilions. Hunt for the 1963 Cosmonaut mosaic.",
                            "საბჭოთა მოდერნისტული პავილიონები. იპოვეთ 1963 წლის კოსმონავტების მოზაიკა.",
                            "Павильоны советского модернизма. Найдите мозаику Космонавта 1963 года.",
                            "Sovyet Modernist pavyonları. 1963 Kozmonot mozaiğini avlayın.",
                            "Խորհրդային մոդեռնիստական ​​տաղավարներ. Որս 1963 թվականի Տիեզերագնացների խճանկարի համար:",
                            "ביתנים מודרניסטיים סובייטיים. צוד את פסיפס הקוסמונאוט משנת 1963.",
                            "أجنحة الحداثة السوفيتية. ابحث عن فسيفساء رائد الفضاء عام 1963."
                        ),
                        timeLabel = "D3 14:00",
                        location = GeoPoint(41.7380, 44.7810),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/03/Soviet-architecture-Tbilisi-Georgia-Expo-Georgia-mosaic.jpg"
                    ),
                    BattleNode(
                        title = L("Dezerter Bazaar", "დეზერტირების ბაზარი", "Дезертирский рынок", "Dezerter Pazarı", "Դեզերտեր շուկա", "שוק דזרטר", "بازار ديزيرتر"),
                        description = L(
                            "Central market. Chaotic and raw. Best place for photography.",
                            "ცენტრალური ბაზარი. ქაოტური და ნამდვილი. საუკეთესოა ფოტოებისთვის.",
                            "Центральный рынок. Хаотичный и сырой. Лучшее место для фотографии.",
                            "Merkez pazar. Kaotik ve ham. Fotoğrafçılık için en iyi yer.",
                            "Կենտրոնական շուկա. Քաոսային և հում: Լավագույն վայրը լուսանկարչության համար:",
                            "שוק מרכזי. כאוטי וגולמי. המקום הטוב ביותר לצילום.",
                            "السوق المركزي. فوضوية وخام. أفضل مكان للتصوير الفوتوغرافي."
                        ),
                        timeLabel = "D3 16:00",
                        location = GeoPoint(41.7210, 44.7930),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2019/07/Emily-Lush-Tbilisi-Dezerter-Bazaar-2.jpg"
                    ),
                    BattleNode(
                        title = L("Stalin's Printing Press", "სტალინის სტამბა", "Типография Сталина", "Stalin'in Matbaası", "Ստալինի տպարան", "בית הדפוס של סטלין", "مطبعة ستالين"),
                        description = L(
                            "Clandestine bunker (1903-1906). Descent via well shaft.",
                            "იატაკქვეშა ბუნკერი (1903-1906). ჩასასვლელი ჭიდან.",
                            "Подпольный бункер (1903-1906). Спуск через шахту колодца.",
                            "Gizli sığınak (1903-1906). Kuyu şaftından iniş.",
                            "Գաղտնի բունկեր (1903-1906). Իջնել ջրհորի հորանով:",
                            "בונקר חשאי (1903-1906). ירידה דרך פיר באר.",
                            "مخبأ سري (1903-1906). النسب عبر رمح البئر."
                        ),
                        timeLabel = "D3 17:30",
                        location = GeoPoint(41.6850, 44.8250),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/01/Emily-Lush-Stalin-Printing-Press-Museum-Avlabari-Tbilisi-machinery.jpg"
                    ),
                    BattleNode(
                        title = L("Bina N37", "ბინა N37", "Квартира N37", "Bina N37", "Բնակարան N37", "בינה N37", "بينا N37"),
                        description = L(
                            "Rooftop winery. Qvevris embedded in balcony. Slow pace essential.",
                            "მარანი სახურავზე. ქვევრები აივანზე. ნელი ტემპი აუცილებელია.",
                            "Винодельня на крыше. Квеври встроены в балкон. Медленный темп обязателен.",
                            "Çatı katı şaraphanesi. Balkona gömülü Qvevris. Yavaş tempo şart.",
                            "Տանիքի գինեգործարան. Պատշգամբում ներկառուցված կարասներ: Դանդաղ տեմպը կարևոր է:",
                            "יקב גג. כדים משובצים במרפסת. קצב איטי חיוני.",
                            "مخزن نبيذ على السطح. الجرار جزءا لا يتجزأ من الشرفة. الوتيرة البطيئة ضرورية."
                        ),
                        timeLabel = "D3 20:00",
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
                title = L(
                    "Kakheti: The 8000 Vintage", "კახეთი: 8000 წლოვანი ვინტაჟი", "Кахетия: Винтаж 8000", "Kakheti: 8000 Yıllık Bağbozumu", "Կախեթ. 8000 բերքահավաք", "קאחתי: בציר ה-8000", "كاختي: 8000 خمر"
                ),
                description = L(
                    "Cradle of Wine. LOGISTICAL AUDIT: Use private transfer over marshrutka to enable stops at Badiauri. Cold War tunnel is 12°C - bring layers.",
                    "ღვინის აკვანი. ლოგისტიკა: გამოიყენეთ კერძო ტრანსფერი ბადიანურში გასაჩერებლად. გვირაბში 12°C-ია - ჩაიცვით თბილად.",
                    "Колыбель вина. ЛОГИСТИКА: Используйте частный трансфер, чтобы остановиться в Бадиаури. В туннеле 12°C — одевайтесь теплее.",
                    "Şarabın Beşiği. LOJİSTİK DENETİM: Badiauri'de durabilmek için marshrutka yerine özel transfer kullanın. Soğuk Savaş tüneli 12°C - kat kat giyinin.",
                    "Գինու օրրան. ԼՈԳԻՍՏԻԿԱԿԱՆ ԱՈՒԴԻՏ. Բադիաուրիում կանգառներ թույլ տալու համար օգտագործեք մասնավոր փոխանցում երթուղայինի փոխարեն: Սառը պատերազմի թունելը 12°C է, բերեք տաք հագուստ:",
                    "עריסת היין. ביקורת לוגיסטית: השתמשו בהסעה פרטית ולא במרשוטקה כדי לאפשר עצירות בבדיאורי. מנהרת המלחמה הקרה היא 12 מעלות צלזיוס - הביאו שכבות.",
                    "مهد النبيذ. التدقيق اللوجستي: استخدم النقل الخاص لتمكين التوقف في باديوري. نفق الحرب الباردة 12 درجة مئوية - أحضر طبقات."
                ),
                imageUrl = "https://wander-lush.org/wp-content/uploads/2017/07/GeorgiaKakheti-28.jpg",
                category = "WINE_REGION",
                difficulty = Difficulty.RELAXED.name,
                totalRideTimeMinutes = 240,
                durationDays = 2,
                route = listOf(GeoPoint(41.6205, 45.9255), GeoPoint(41.8965, 45.5680)),
                itinerary = listOf(
                    // DAY 1
                    BattleNode(
                        title = L("Badiauri", "ბადიაური", "Бадиаури", "Badiauri", "Բադիաուրի", "בדיאורי", "باديوري"),
                        description = L(
                            "Roadside bread stop. Eat hot Shoti bread with Guda cheese.",
                            "პურის გაჩერება გზატკეცილზე. მიირთვით ცხელი შოთი გუდის ყველით.",
                            "Остановка за хлебом у дороги. Ешьте горячий хлеб шоти с сыром гуда.",
                            "Yol kenarında ekmek molası. Guda peyniri ile sıcak Shoti ekmeği yiyin.",
                            "Ճանապարհային հացի կանգառ. Կերեք տաք Շոթի հաց Գուդա պանրով:",
                            "עצירת לחם בצד הדרך. תאכלו לחם שוטי חם עם גבינת גודה.",
                            "توقف الخبز على جانب الطريق. تناول خبز شوتي الساخن مع جبنة جودا."
                        ),
                        timeLabel = "D1 09:30",
                        location = GeoPoint(41.6660, 45.4330),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/10/Emily-Lush-Tbilisi-Georgia-guide-shotis-bread.jpg"
                    ),
                    BattleNode(
                        title = L("Bodbe Monastery", "ბოდბის მონასტერი", "Монастырь Бодбе", "Bodbe Manastırı", "Բոդբե վանք", "מנזר בודבה", "دير بودبي"),
                        description = L(
                            "Tomb of St. Nino. 667 steps to the spring. Dress code enforced.",
                            "წმინდა ნინოს საფლავი. 667 საფეხური წყარომდე. დრეს-კოდი მკაცრია.",
                            "Могила св. Нино. 667 ступеней к источнику. Строгий дресс-код.",
                            "Azize Nino'nun mezarı. Kaynağa 667 basamak. Kıyafet kuralı uygulanır.",
                            "Սուրբ Նինոյի գերեզմանը. 667 քայլ դեպի աղբյուրը. Հագուստի կոդը պարտադիր է:",
                            "קבר נינו הקדושה. 667 מדרגות למעיין. קוד לבוש נאכף.",
                            "قبر القديسة نينو. 667 خطوة إلى الربيع. قواعد اللباس مفروضة."
                        ),
                        timeLabel = "D1 11:00",
                        alertType = "DRESS_CODE",
                        location = GeoPoint(41.6068, 45.9328),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/04/Emily-Lush-Kakheti-itinerary-Bodbe-Monastery-view.jpg"
                    ),
                    BattleNode(
                        title = L("Pheasant’s Tears", "ხოხბის ცრემლები", "Слезы фазана", "Sülün Gözyaşları", "Փասիանի արցունքները", "דמעות הפסיון", "دموع الدراج"),
                        description = L(
                            "Natural wine HQ. 'Polyphonic' cuisine takes time.",
                            "ბუნებრივი ღვინის შტაბი. 'პოლიფონიური' სამზარეულო დროს მოითხოვს.",
                            "Штаб-квартира натурального вина. 'Полифоническая' кухня требует времени.",
                            "Doğal şarap merkezi. 'Polifonik' mutfak zaman alır.",
                            "Բնական գինու շտաբ. «Պոլիֆոնիկ» խոհանոցը ժամանակ է պահանջում:",
                            "מטה יין טבעי. מטבח 'פוליפוני' לוקח זמן.",
                            "مقر النبيذ الطبيعي. المطبخ 'متعدد الأصوات' يستغرق وقتا."
                        ),
                        timeLabel = "D1 13:00",
                        location = GeoPoint(41.6195, 45.9220),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/06/Emily-Lush-Sighnaghi-restaurant-Pheasants-Tears-cellar.jpg"
                    ),
                    BattleNode(
                        title = L("Signagi Walls", "სიღნაღის გალავანი", "Стены Сигнахи", "Signagi Surları", "Սիղնաղի պարիսպները", "חומות סיגנאגי", "جدران سيغناغي"),
                        description = L(
                            "1762 Fortifications. Fog can obscure views in Jan.",
                            "1762 წლის სიმაგრეები. იანვარში ნისლმა შეიძლება დაფაროს ხედი.",
                            "Укрепления 1762 года. В январе туман может скрыть вид.",
                            "1762 Surları. Ocak ayında sis manzarayı kapatabilir.",
                            "1762 ամրություններ. Հունվարին մառախուղը կարող է թաքցնել տեսարանները:",
                            "ביצורים משנת 1762. ערפל יכול להסתיר את הנוף בינואר.",
                            "تحصينات 1762. يمكن للضباب أن يحجب الرؤية في يناير."
                        ),
                        timeLabel = "D1 15:30",
                        alertType = "FOG_RISK",
                        location = GeoPoint(41.6210, 45.9250),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Emily-Lush-visit-Kakheti-wine-region-Georgia-Sighnagh-Walls.jpg"
                    ),
                    BattleNode(
                        title = L("Vakho’s Winery", "ვახოს მარანი", "Винодельня Вахо", "Vakho'nun Şaraphanesi", "Վախոյի գինեգործարան", "יקב ואחו", "مصنع نبيذ فاخو"),
                        description = L(
                            "Intimate family Marani. Taste from the Qvevri with an Orshimo.",
                            "ოჯახური მარანი. გასინჯეთ ღვინო ქვევრიდან ორშიმოთი.",
                            "Уютный семейный марани. Пробуйте из квеври с помощью оршимо.",
                            "Samimi aile Marani'si. Orshimo ile Qvevri'den tadın.",
                            "Ինտիմ ընտանիք Մարանի. Ճաշակեք կարասից Օրշիմոյով:",
                            "מרני משפחתי אינטימי. טעמו מהכד עם אורשימו.",
                            "عائلة ماراني الحميمة. تذوق من الجرة مع أورشيمو."
                        ),
                        timeLabel = "D1 17:30",
                        location = GeoPoint(41.6180, 45.9240),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2019/07/Emily-Lush-Tbilisi-to-Sighnaghi-10.jpg"
                    ),
                    BattleNode(
                        title = L("The Terrace Signagi", "ტერასა სიღნაღი", "Терраса Сигнахи", "Teras Signagi", "Սիղնաղի տեռաս", "הטרסה סיגנאגי", "تراس سيغناغي"),
                        description = L(
                            "Golden Hour views of the Caucasus.",
                            "ოქროს საათი კავკასიონის ხედით.",
                            "Виды Кавказа в 'золотой час'.",
                            "Kafkasya'nın Altın Saat manzaraları.",
                            "Կովկասի ոսկե ժամի տեսարաններ:",
                            "נופי שעת הזהב של הקווקז.",
                            "مناظر الساعة الذهبية للقوقاز."
                        ),
                        timeLabel = "D1 20:00",
                        location = GeoPoint(41.6200, 45.9210),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2025/06/Emily-Lush-Sighnaghi-Kakheti-Georgia-city-golden-hour.jpg"
                    ),

                    // DAY 2
                    BattleNode(
                        title = L("Tsinandali Estate", "წინანდლის მამული", "Усадьба Цинандали", "Tsinandali Malikanesi", "Ծինանդալի կալվածք", "אחוזת ציננדלי", "عقارات تسيناندالي"),
                        description = L(
                            "Prince Chavchavadze's palace. Open 10:00-18:00 daily.",
                            "თავად ჭავჭავაძის სასახლე. ღიაა 10:00-18:00 ყოველდღე.",
                            "Дворец князя Чавчавадзе. Открыто 10:00-18:00.",
                            "Prens Chavchavadze'nin sarayı. Her gün 10:00-18:00 arası açık.",
                            "Իշխան Ճավճավաձեի պալատը. Բաց է ամեն օր 10:00-18:00:",
                            "ארמונו של הנסיך צ'בצ'בדזה. פתוח מדי יום 10:00-18:00.",
                            "قصر الأمير تشافشافادزه. مفتوح 10:00-18:00 يوميا."
                        ),
                        timeLabel = "D2 10:30",
                        location = GeoPoint(41.8965, 45.5680),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Emily-Lush-visit-Kakheti-wine-region-Georgia-Tsinandali.jpg"
                    ),
                    BattleNode(
                        title = L("Vazisubani Estate", "ვაზისუბნის მამული", "Усадьба Вазисубани", "Vazisubani Malikanesi", "Վազիսուբանի կալվածք", "אחוזת וזיסובאני", "عقارات فازيسوباني"),
                        description = L(
                            "Restored noble mansion. Excellent for a relaxed lunch.",
                            "აღდგენილი თავადური სასახლე. შესანიშნავია მშვიდი სადილისთვის.",
                            "Отреставрированный дворянский особняк. Отлично подходит для обеда.",
                            "Restore edilmiş soylu konağı. Rahat bir öğle yemeği için mükemmel.",
                            "Վերականգնված ազնվական առանձնատուն. Հիանալի է հանգիստ ճաշի համար:",
                            "אחוזה אצילית משוחזרת. מצוין לארוחת צהריים רגועה.",
                            "قصر نبيل تم تجديده. ممتاز لتناول غداء مريح."
                        ),
                        timeLabel = "D2 13:00",
                        location = GeoPoint(41.8380, 45.7100),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/04/Emily-Lush-Kakheti-accommodation-Vazisubani-Estate-heritage.jpg"
                    ),
                    BattleNode(
                        title = L("Alaverdi Monastery", "ალავერდის მონასტერი", "Монастырь Алаверди", "Alaverdi Manastırı", "Ալավերդու վանք", "מנזר אלברדי", "دير ألافيردي"),
                        description = L(
                            "11th-century Cathedral. Monastic wine 'Since 1011'.",
                            "მე-11 საუკუნის ტაძარი. სამონასტრო ღვინო '1011 წლიდან'.",
                            "Собор 11 века. Монастырское вино 'С 1011 года'.",
                            "11. yüzyıl Katedrali. '1011'den beri' manastır şarabı.",
                            "11-րդ դարի տաճար. Վանական գինի «1011 թվականից»:",
                            "קתדרלה מהמאה ה-11. יין נזירים 'מאז 1011'.",
                            "كاتدرائية القرن الحادي عشر. النبيذ الرهباني 'منذ 1011'."
                        ),
                        timeLabel = "D2 15:00",
                        location = GeoPoint(42.0325, 45.3770),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/02/Emily-Lush-Alaverdi-Monastery-Kakheti-closeup.jpg"
                    ),
                    BattleNode(
                        title = L("Khareba Tunnel", "ხარებას გვირაბი", "Тоннель Хареба", "Khareba Tüneli", "Խարեբա թունել", "מנהרת חרבה", "نفق خريبة"),
                        description = L(
                            "Cold War bunker turned wine cellar. Internal temp is 12-14°C.",
                            "ცივი ომის ბუნკერი, რომელიც ღვინის მარნად იქცა. ტემპერატურა 12-14°C.",
                            "Бункер времен холодной войны, ставший винным погребом. Температура 12-14°C.",
                            "Soğuk Savaş sığınağı şarap mahzenine dönüştü. İç sıcaklık 12-14°C.",
                            "Սառը պատերազմի բունկերը վերածվել է գինու մառանի: Ներքին ջերմաստիճանը 12-14°C է:",
                            "בונקר המלחמה הקרה הפך למרתף יין. הטמפרטורה הפנימית היא 12-14 מעלות צלזיוס.",
                            "تحول مخبأ الحرب الباردة إلى قبو نبيذ. درجة الحرارة الداخلية 12-14 درجة مئوية."
                        ),
                        timeLabel = "D2 16:30",
                        alertType = "COLD_INSIDE",
                        location = GeoPoint(41.9485, 45.8340),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/04/Emily-Lush-Winery-Khareba-Wine-Tunnel-Kakheti-bottles.jpg"
                    ),
                    BattleNode(
                        title = L("Kindzmarauli Corp", "ქინძმარაულის კორპორაცია", "Корпорация Киндзмараули", "Kindzmarauli Şirketi", "Կինձմարաուլի կորպորացիա", "תאגיד קינדזמרולי", "شركة كينبزمارولي"),
                        description = L(
                            "Industrial wine tour in Kvareli center.",
                            "ინდუსტრიული ღვინის ტური ყვარლის ცენტრში.",
                            "Промышленный винный тур в центре Кварели.",
                            "Kvareli merkezinde endüstriyel şarap turu.",
                            "Արդյունաբերական գինու շրջագայություն Ղվարելիի կենտրոնում:",
                            "סיור יין תעשייתי במרכז קווארלי.",
                            "جولة النبيذ الصناعية في مركز كفاريلي."
                        ),
                        timeLabel = "D2 18:00",
                        location = GeoPoint(41.9470, 45.8130),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/06/Emily-Lush-best-wineries-in-Kakheti-Georgia-Kindzmarauli-factory.jpg"
                    ),
                    BattleNode(
                        title = L("Kapiloni", "კაპილონი", "Капилони", "Kapiloni", "Կապիլոնի", "קפילוני", "كابيلوني"),
                        description = L(
                            "Best Mtsvadi in Telavi. BBQ on vine clippings. 2-hour engagement.",
                            "საუკეთესო მწვადი თელავში. იწვება ვაზის ნასხლავებზე.",
                            "Лучший мцвади в Телави. Жарят на виноградной лозе.",
                            "Telavi'deki en iyi Mtsvadi. Asma kırpıntıları üzerinde barbekü.",
                            "Լավագույն Մծվադին Թելավիում. Խորոված խաղողի ճյուղերի վրա:",
                            "השיפודים הטובים ביותר בתלאווי. מנגל על גזם גפנים.",
                            "أفضل مشاوي في تيلافي. شواء على قصاصات الكرمة."
                        ),
                        timeLabel = "D2 20:00",
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
                title = L(
                    "Kazbegi: The Sky Piercer", "ყაზბეგი: ცისმჭრელი", "Казбеги: Пронзающий небо", "Kazbegi: Gökyüzü Delici", "Կազբեկ. Երկինք ծակող", "קזבגי: פירסר השמיים", "كازبيجي: ثاقب السماء"
                ),
                description = L(
                    "Military Highway. LOGISTICAL ALERT: Jvari Pass subject to avalanche closure. Juta is INACCESSIBLE in Jan. 4x4 required for Gergeti.",
                    "სამხედრო გზა. გაფრთხილება: ჯვრის უღელტეხილი ზვავსაშიშროების გამო შეიძლება დაიკეტოს. ჯუთა იანვარში მიუწვდომელია. გერგეტისთვის საჭიროა 4x4.",
                    "Военная дорога. ВНИМАНИЕ: Перевал Джвари может быть закрыт из-за лавин. Джута НЕДОСТУПНА в январе. Для Гергети нужен 4x4.",
                    "Askeri Otoyol. LOJİSTİK UYARI: Jvari Geçidi çığ nedeniyle kapanabilir. Juta Ocak ayında ERİŞİLEMEZ. Gergeti için 4x4 gereklidir.",
                    "Ռազմական մայրուղի. ԼՈԳԻՍՏԻԿԱԿԱՆ ԶԳՈՒՇԱՑՈՒՄ. Ջվարի լեռնանցքը ենթակա է ձնահյուսի փակման: Ջուտան անհասանելի է հունվարին. Գերգետիի համար անհրաժեշտ է 4x4:",
                    "הכביש הצבאי. התראה לוגיסטית: מעבר ג'ווארי נתון לסגירת מפולות שלגים. ג'וטה אינה נגישה בינואר. דרוש 4x4 לגרגטי.",
                    "الطريق السريع العسكري. تنبيه لوجستي: ممر جفاري يخضع للإغلاق بسبب الانهيار الجليدي. جوتا لا يمكن الوصول إليها في يناير. 4x4 مطلوب لغيرغيتي."
                ),
                imageUrl = "https://wander-lush.org/wp-content/uploads/2019/11/Emily-Lush-Kazbegi-Gergeti-Trinity-Georgia-11.jpg",
                category = "MOUNTAIN",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 200,
                durationDays = 2,
                hasSnowWarning = true,
                route = listOf(GeoPoint(42.6605, 44.6430)),
                itinerary = listOf(
                    // DAY 1
                    BattleNode(
                        title = L("Ananuri Fortress", "ანანურის ციხე", "Крепость Ананури", "Ananuri Kalesi", "Անանուրի ամրոց", "מבצר אנאנורי", "قلعة أنانوري"),
                        description = L(
                            "17th-century fortress. Finest stone carvings in Georgia.",
                            "მე-17 საუკუნის ციხესიმაგრე. საუკეთესო ქვის ჩუქურთმები საქართველოში.",
                            "Крепость 17 века. Лучшая резьба по камню в Грузии.",
                            "17. yüzyıl kalesi. Gürcistan'daki en iyi taş oymaları.",
                            "17-րդ դարի ամրոց. Լավագույն քարե փորագրությունները Վրաստանում:",
                            "מבצר מהמאה ה-17. גילופי האבן המשובחים ביותר בגאורגיה.",
                            "قلعة القرن السابع عشر. أرقى المنحوتات الحجرية في جورجيا."
                        ),
                        timeLabel = "D1 10:00",
                        location = GeoPoint(42.1636, 44.7030),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/04/Emily-Lush-Kazbegi-Gergeti-Trinity-Georgia-10.jpg"
                    ),
                    BattleNode(
                        title = L("Friendship Monument", "მეგობრობის მონუმენტი", "Арка Дружбы", "Dostluk Anıtı", "Բարեկամության հուշարձան", "אנדרטת הידידות", "نصب الصداقة"),
                        description = L(
                            "Soviet mosaic over Devil's Valley. Snow can be deep.",
                            "საბჭოთა მოზაიკა ეშმაკის ხეობაზე. თოვლი შეიძლება ღრმა იყოს.",
                            "Советская мозаика над Чертовой долиной. Может быть глубокий снег.",
                            "Şeytan Vadisi üzerindeki Sovyet mozaiği. Kar derin olabilir.",
                            "Խորհրդային խճանկար Սատանայի հովտի վրա: Ձյունը կարող է խորը լինել:",
                            "פסיפס סובייטי מעל עמק השטן. השלג יכול להיות עמוק.",
                            "فسيفساء سوفيتية فوق وادي الشيطان. يمكن أن يكون الثلج عميقا."
                        ),
                        timeLabel = "D1 12:00",
                        alertType = "DEEP_SNOW",
                        location = GeoPoint(42.4862, 44.4542),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Valley-mountains.jpg"
                    ),
                    BattleNode(
                        title = L("Rooms Hotel", "სასტუმრო რუმსი", "Отель Rooms", "Rooms Otel", "Rooms հյուրանոց", "מלון רומס", "فندق رومز"),
                        description = L(
                            "Former Soviet sanatorium turned luxury hotel.",
                            "ყოფილ საბჭოთა სანატორიუმში გახსნილი სასტუმრო.",
                            "Бывший советский санаторий, ставший роскошным отелем.",
                            "Eski Sovyet sanatoryumu lüks otele dönüştü.",
                            "Նախկին խորհրդային առողջարանը վերածվել է շքեղ հյուրանոցի:",
                            "סנטוריום סובייטי לשעבר הפך למלון יוקרה.",
                            "مصحة سوفيتية سابقة تحولت إلى فندق فخم."
                        ),
                        timeLabel = "D1 13:30",
                        location = GeoPoint(42.6600, 44.6430),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/04/Best-hotels-in-Kazbegi-Georgia-Rooms-Kazbegi-small.jpg"
                    ),
                    BattleNode(
                        title = L("Gergeti Trinity", "გერგეტის სამება", "Гергетская Троица", "Gergeti Teslis Kilisesi", "Գերգետի Երրորդություն", "גרגטי טריניטי", "كنيسة جيرجيتي"),
                        description = L(
                            "Iconic 14th-century church. Winter hike is dangerous. Use 4x4.",
                            "მე-14 საუკუნის ტაძარი. ფეხით ასვლა ზამთარში სახიფათოა. გამოიყენეთ 4x4.",
                            "Церковь 14 века. Зимний поход опасен. Используйте 4x4.",
                            "İkonik 14. yüzyıl kilisesi. Kış yürüyüşü tehlikelidir. 4x4 kullanın.",
                            "14-րդ դարի խորհրդանշական եկեղեցի. Ձմեռային արշավը վտանգավոր է. Օգտագործեք 4x4:",
                            "כנסייה אייקונית מהמאה ה-14. טיול חורף מסוכן. השתמשו ב-4x4.",
                            "كنيسة أيقونية من القرن الرابع عشر. المشي لمسافات طويلة في فصل الشتاء أمر خطير. استخدم 4x4."
                        ),
                        timeLabel = "D1 15:30",
                        alertType = "4X4_ONLY",
                        location = GeoPoint(42.6625, 44.6200),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/06/Emily-Lush-Kazbegi-Georgia-Gergeti-Trinity-Church-hike-viewpoint-shrine-cross.jpg"
                    ),
                    BattleNode(
                        title = L("Gveleti Waterfall", "გველეთის ჩანჩქერი", "Гвелетский водопад", "Gveleti Şelalesi", "Գվելեթի ջրվեժ", "מפל גוולטי", "شلال جفيليتي"),
                        description = L(
                            "Short hike to waterfall near border.",
                            "მოკლე ლაშქრობა ჩანჩქერამდე საზღვართან ახლოს.",
                            "Короткий поход к водопаду у границы.",
                            "Sınıra yakın şelaleye kısa yürüyüş.",
                            "Կարճ արշավ դեպի ջրվեժ սահմանի մոտ:",
                            "טיול קצר למפל ליד הגבול.",
                            "نزهة قصيرة إلى الشلال بالقرب من الحدود."
                        ),
                        timeLabel = "D1 17:30",
                        location = GeoPoint(42.7050, 44.6150),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Chaukhi-river.jpg"
                    ),
                    BattleNode(
                        title = L("Shorena's", "შორენასთან", "У Шорены", "Shorena'nın Yeri", "Շորենայի մոտ", "אצל שורנה", "شورنا"),
                        description = L(
                            "Hearty mountain food. Khinkali and warming stews.",
                            "მთის ნოყიერი საჭმელი. ხინკალი და ცხელი კერძები.",
                            "Сытная горная еда. Хинкали и согревающие рагу.",
                            "Doyurucu dağ yemeği. Khinkali ve ısıtıcı yahniler.",
                            "Սրտանց լեռնային սնունդ. Խինկալի և տաքացնող շոգեխաշածներ:",
                            "אוכל הרים דשן. חינקלי ותבשילים מחממים.",
                            "طعام الجبل الشهي. خينكالي واليخنات الدافئة."
                        ),
                        timeLabel = "D1 20:00",
                        location = GeoPoint(42.6570, 44.6410),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2023/06/Emily-Lush-Kazbegi-restaurants-Maisi.jpg"
                    ),

                    // DAY 2
                    BattleNode(
                        title = L("Sno Heads", "სნოს თავები", "Головы Сно", "Sno Kafaları", "Սնոյի գլուխները", "ראשי סנו", "رؤوس سنو"),
                        description = L(
                            "Giant granite heads of poets. Primary AM activity since Juta is closed.",
                            "პოეტების გიგანტური გრანიტის თავები. მთავარი აქტივობა, რადგან ჯუთა დაკეტილია.",
                            "Гигантские гранитные головы поэтов. Основная утренняя активность, так как Джута закрыта.",
                            "Şairlerin dev granit kafaları. Juta kapalı olduğu için birincil sabah aktivitesi.",
                            "Պոետների հսկա գրանիտե գլուխներ. Հիմնական առավոտյան գործունեություն, քանի որ Ջուտան փակ է:",
                            "ראשי גרניט ענקיים של משוררים. פעילות בוקר עיקרית מכיוון שג'וטה סגורה.",
                            "رؤوس الجرانيت العملاقة للشعراء. النشاط الصباحي الأساسي منذ إغلاق جوتا."
                        ),
                        timeLabel = "D2 09:30",
                        location = GeoPoint(42.6050, 44.6350),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Sno-Giant-Stone-Heads.jpg"
                    ),
                    BattleNode(
                        title = L("Juta Valley", "ჯუთას ხეობა", "Долина Джута", "Juta Vadisi", "Ջուտայի հովիտ", "עמק ג'וטה", "وادي جوتا"),
                        description = L(
                            "Georgian Dolomites. INACCESSIBLE IN JANUARY. Road closed. High avalanche risk.",
                            "ქართული დოლომიტები. იანვარში მიუწვდომელია. გზა დაკეტილია. ზვავსაშიშროება.",
                            "Грузинские Доломиты. НЕДОСТУПНО В ЯНВАРЕ. Дорога закрыта. Риск лавин.",
                            "Gürcü Dolomitleri. OCAK AYINDA ERİŞİLEMEZ. Yol kapalı. Yüksek çığ riski.",
                            "Վրացական Դոլոմիտներ. ԱՆՀԱՍԱՆԵԼԻ ՀՈՒՆՎԱՐԻՆ. Ճանապարհը փակ է. Ձնահյուսի բարձր ռիսկ:",
                            "הדולומיטים הגאורגיים. לא נגיש בינואר. כביש סגור. סיכון גבוה למפולות.",
                            "الدولوميت الجورجي. לא נגיש בינואר. כביש סגור. סיכון גבוה למפולות."
                        ),
                        timeLabel = "D2 11:00",
                        alertType = "SEASONAL_CLOSURE",
                        location = GeoPoint(42.5790, 44.7450),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Valley-Hike-Fifth-Season-HERO.jpg"
                    ),
                    BattleNode(
                        title = L("Fifth Season Hut", "მეხუთე სეზონი", "Пятое время года", "Beşinci Mevsim Kulübesi", "Հինգերորդ սեզոնի խրճիթ", "בקתת העונה החמישית", "كوخ الموسم الخامس"),
                        description = L(
                            "Lunch spot in Juta. LIKELY CLOSED IN JAN.",
                            "სადილის ადგილი ჯუთაში. იანვარში სავარაუდოდ დაკეტილია.",
                            "Место для обеда в Джуте. В ЯНВАРЕ ВЕРОЯТНО ЗАКРЫТО.",
                            "Juta'da öğle yemeği noktası. OCAK AYINDA MUHTEMELEN KAPALI.",
                            "Ճաշի տեղ Ջուտայում. ՀԱՎԱՆԱԲԱՐ ՓԱԿ Է ՀՈՒՆՎԱՐԻՆ:",
                            "מקום ארוחת צהריים בג'וטה. ככל הנראה סגור בינואר.",
                            "مكان الغداء في جوتا. من المحتمل أن يغلق في يناير."
                        ),
                        timeLabel = "D2 13:00",
                        location = GeoPoint(42.5720, 44.7500),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-Fifth-Season-Cabin.jpg"
                    ),
                    BattleNode(
                        title = L("Dariali Monastery", "დარიალის მონასტერი", "Дарьяльский монастырь", "Dariali Manastırı", "Դարիալի վանք", "מנזר דריאלי", "دير داريالي"),
                        description = L(
                            "Massive complex on Russian border. Symbolic sentinel.",
                            "მასიური კომპლექსი რუსეთის საზღვარზე. სიმბოლური გუშაგი.",
                            "Массивный комплекс на российской границе. Символический страж.",
                            "Rusya sınırında devasa kompleks. Sembolik nöbetçi.",
                            "Զանգվածային համալիր Ռուսաստանի սահմանին. Խորհրդանշական պահակ:",
                            "מתחם עצום על הגבול הרוסי. זקיף סמלי.",
                            "مجمع ضخم على الحدود الروسية. حارس رمزي."
                        ),
                        timeLabel = "D2 16:00",
                        location = GeoPoint(42.7420, 44.6290),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Emily-Lush-visit-Kakheti-wine-region-Georgia-Ninotsminda-Cathedral.jpg"
                    ),
                    BattleNode(
                        title = L("Tsdo Village", "სოფელი ცდო", "Деревня Цдо", "Tsdo Köyü", "Ցդո գյուղ", "כפר צדו", "قرية تسدو"),
                        description = L(
                            "Animist 'ghost village'. Pre-christian shrines.",
                            "ანიმისტური 'მოჩვენებების სოფელი'. წინაქრისტიანული სალოცავები.",
                            "Анимистская 'деревня-призрак'. Дохристианские святилища.",
                            "Animist 'hayalet köy'. Hıristiyanlık öncesi tapınaklar.",
                            "Անիմիստական ​​«ուրվականների գյուղ». Նախաքրիստոնեական սրբավայրեր:",
                            "כפר רפאים אנימיסטי. מקדשים טרום-נוצריים.",
                            "'قرية الأشباح' الروحانية. أضرحة ما قبل المسيحية."
                        ),
                        timeLabel = "D2 17:00",
                        location = GeoPoint(42.7010, 44.6300),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/07/Emily-Lush-Juta-road-walking.jpg"
                    ),
                    BattleNode(
                        title = L("Pasanauri", "ფასანაური", "Пасанаури", "Pasanauri", "Պասանաուրի", "פסנאורי", "باسانوري"),
                        description = L(
                            "Birthplace of Khinkali. Meat is chopped with dagger, not ground.",
                            "ხინკლის სამშობლო. ხორცი დაკეპილია ხანჯლით.",
                            "Родина хинкали. Мясо рубят кинжалом, а не перемалывают.",
                            "Khinkali'nin doğum yeri. Et kıyma değil, hançerle doğranır.",
                            "Խինկալիի ծննդավայրը. Միսը կտրատում են դաշույնով, ոչ թե աղացած:",
                            "מקום הולדתו של חינקלי. הבשר קצוץ בפגיון, לא טחון.",
                            "مسقط رأس خينكالي. يتم تقطيع اللحم بالخنجر وليس الأرض."
                        ),
                        timeLabel = "D2 19:30",
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
                title = L(
                    "West: Canyons & Ruins", "დასავლეთი: კანიონები და ნანგრევები", "Запад: Каньоны и Руины", "Batı: Kanyonlar ve Harabeler", "Արևմուտք. կիրճեր և ավերակներ", "מערב: קניונים והריסות", "الغرب: الأخاديد والآثار"
                ),
                description = L(
                    "Golden Fleece & Soviet Decay. LOGISTICS: Tskaltubo takes 4-5 hours. Martvili boats suspended in rain. Prometheus Cave closed Mondays.",
                    "ოქროს საწმისი და საბჭოთა ნანგრევები. ლოგისტიკა: წყალტუბო 4-5 საათს მოითხოვს. მარტვილის ნავები წვიმაში ჩერდება. პრომეთე ორშაბათს დაკეტილია.",
                    "Золотое руно и советский распад. ЛОГИСТИКА: Цхалтубо занимает 4-5 часов. Лодки в Мартвили не работают в дождь. Пещера Прометея закрыта в понедельник.",
                    "Altın Post ve Sovyet Çürümesi. LOJİSTİK: Tskaltubo 4-5 saat sürer. Martvili tekneleri yağmurda askıya alındı. Prometheus Mağarası Pazartesi günleri kapalı.",
                    "Ոսկե գեղմ և խորհրդային քայքայում: ԼՈԳԻՍՏԻԿԱ. Ծխալտուբոն տևում է 4-5 ժամ: Մարտվիլի նավակները կասեցվել են անձրևի տակ: Պրոմեթևսի քարանձավը փակ է երկուշաբթի օրերին:",
                    "גיזת הזהב וריקבון סובייטי. לוגיסטיקה: צקלטובו לוקח 4-5 שעות. סירות מרטווילי מושעות בגשם. מערת פרומתאוס סגורה בימי שני.",
                    "الصوف الذهبي والاضمحلال السوفيتي. الخدمات اللوجستية: يستغرق تسكالتوبو 4-5 ساعات. القوارب مارتفيلي معلقة في المطر. كهف بروميثيوس مغلق أيام الاثنين."
                ),
                imageUrl = "https://wander-lush.org/wp-content/uploads/2021/03/Ultimate-Georgia-itinerary-Stalins-bathhouse-Tskaltubo-Imereti-replacement.jpg",
                category = "NATURE",
                difficulty = Difficulty.NORMAL.name,
                totalRideTimeMinutes = 180,
                durationDays = 3,
                route = listOf(GeoPoint(42.2770, 42.7040), GeoPoint(42.4570, 42.3770)),
                itinerary = listOf(
                    // DAY 1
                    BattleNode(
                        title = L("Sanatorium Medea", "სანატორიუმი მედეა", "Санаторий Медея", "Sanatoryum Medea", "Սանատորիա Մեդեա", "סנטוריום מדיאה", "مصح ميديا"),
                        description = L(
                            "Romanesque decay in Tskaltubo. Part of a 4-5 hour urban exploration.",
                            "რომანული ნანგრევები წყალტუბოში. ურბანული კვლევის ნაწილი.",
                            "Романский упадок в Цхалтубо. Часть 4-5 часового маршрута.",
                            "Tskaltubo'da Romanesk çürüme. 4-5 saatlik kentsel keşfin bir parçası.",
                            "Ռոմանական քայքայում Ծխալտուբոյում: 4-5 ժամ տևողությամբ քաղաքային հետազոտության մաս:",
                            "ריקבון רומנסקי בצקלטובו. חלק מחקירה עירונית של 4-5 שעות.",
                            "الاضمحلال الرومانسكي في تسكالتوبو. جزء من استكشاف حضري لمدة 4-5 ساعات."
                        ),
                        timeLabel = "D1 10:00",
                        location = GeoPoint(42.3250, 42.6000),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/11/Emily-Lush-Tskaltubo-guide-Sanatorium-Medea-entryway-HERO.jpg"
                    ),
                    BattleNode(
                        title = L("Stalin's Bath", "სტალინის აბანო", "Баня Сталина", "Stalin'in Hamamı", "Ստալինի բաղնիք", "המרחץ של סטלין", "حمام ستالين"),
                        description = L(
                            "Bathhouse No. 6. Restored. See the dictator's mosaic pool.",
                            "აბანო N6. აღდგენილი. ნახეთ დიქტატორის მოზაიკური აუზი.",
                            "Баня № 6. Отреставрирована. Посмотрите мозаичный бассейн диктатора.",
                            "6 Nolu Hamam. Restore edildi. Diktatörün mozaik havuzunu görün.",
                            "Բաղնիք No 6. Վերականգնված. Տեսեք դիկտատորի խճանկարային լողավազանը:",
                            "בית מרחץ מס' 6. משוחזר. ראו את בריכת הפסיפס של הדיקטטור.",
                            "الحمام رقم 6. استعادة. شاهد بركة فسيفساء الديكتاتور."
                        ),
                        timeLabel = "D1 11:30",
                        location = GeoPoint(42.3280, 42.5970),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/11/Emily-Lush-Tskaltubo-guide-Bathhouse-6-entry.jpg"
                    ),
                    BattleNode(
                        title = L("Magnolia", "მაგნოლია", "Магнолия", "Manolya", "Մագնոլիա", "מגנוליה", "ماغنوليا"),
                        description = L(
                            "Reliable dining in Tskaltubo park. Imeretian Khachapuri.",
                            "სანდო კვება წყალტუბოს პარკში. იმერული ხაჭაპური.",
                            "Надежный ресторан в парке Цхалтубо. Имеретинский хачапури.",
                            "Tskaltubo parkında güvenilir yemek. Imeretian Khachapuri.",
                            "Վստահելի ճաշ Ծխալտուբո այգում. Իմերեթական Խաչապուրի:",
                            "אוכל אמין בפארק צקלטובו. חצ'אפורי אימרולי.",
                            "تطعام موثوق به في حديقة تسكالتوبو. خاشابوري إيميريتي."
                        ),
                        timeLabel = "D1 13:00",
                        location = GeoPoint(42.3260, 42.5980),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/11/Emily-Lush-Tskaltubo-guide-Magnolia-restaurant-exterior.jpg"
                    ),
                    BattleNode(
                        title = L("Prometheus Cave", "პრომეთეს მღვიმე", "Пещера Прометея", "Prometheus Mağarası", "Պրոմեթևսի քարանձավ", "מערת פרומתאוס", "كهف بروميثيوس"),
                        description = L(
                            "1.4km underground route. Closed Mondays. Boat ride included.",
                            "1.4 კმ მიწისქვეშა მარშრუტი. დაკეტილია ორშაბათობით. ნავით გასეირნება.",
                            "1,4 км подземного маршрута. Закрыто по понедельникам. Прогулка на лодке.",
                            "1.4km yeraltı rotası. Pazartesi günleri kapalıdır. Tekne turu dahildir.",
                            "1.4 կմ ստորգետնյա երթուղի. Երկուշաբթի օրը փակ է: Նավով զբոսանքը ներառված է:",
                            "מסלול תת קרקעי של 1.4 ק'מ. סגור בימי שני. שייט בסירה כלול.",
                            "طريق تحت الأرض بطول 1.4 كم. مغلق الاثنين. ركوب القارب مشمول."
                        ),
                        timeLabel = "D1 15:00",
                        alertType = "CLOSED_MONDAYS",
                        location = GeoPoint(42.3765, 42.6005),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2024/08/Emily-Lush-Tskaltubo-Tetra-Cave.jpg"
                    ),
                    BattleNode(
                        title = L("Sisters", "დები", "Сестры", "Kız Kardeşler", "Քույրեր", "אחיות", "أخوات"),
                        description = L(
                            "Soulful Kutaisi dining. Live piano. Relaxed atmosphere.",
                            "ქუთაისური გარემო. ცოცხალი ფორტეპიანო. მშვიდი ატმოსფერო.",
                            "Душевный ужин в Кутаиси. Живое фортепиано. Расслабленная атмосфера.",
                            "Duygulu Kutaisi yemeği. Canlı piyano. Rahat atmosfer.",
                            "Հոգևոր Քութայիսի ճաշատեսակ. Կենդանի դաշնամուր. Հանգիստ մթնոլորտ:",
                            "ארוחת קוטאיסי מלאת נשמה. פסנתר חי. אווירה רגועה.",
                            "طعام كوتايسي العاطفي. بيانو حي. جو مريح."
                        ),
                        timeLabel = "D1 20:00",
                        location = GeoPoint(42.2710, 42.7030),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/09/Emily-Lush-restaurants-in-Kutaisi-new-Sisters-interior.jpg"
                    ),

                    // DAY 2
                    BattleNode(
                        title = L("Martvili Canyon", "მარტვილის კანიონი", "Каньон Мартвили", "Martvili Kanyonu", "Մարտվիլի կիրճ", "קניון מרטווילי", "وادي مارتفيلي"),
                        description = L(
                            "Dadiani bathing pool. Boats suspended if water level high.",
                            "დადიანების საბანაო ადგილი. ნავები ჩერდება წყლის დონის მატებისას.",
                            "Купальня Дадиани. Лодки не ходят при высоком уровне воды.",
                            "Dadiani yüzme havuzu. Su seviyesi yüksekse tekneler askıya alınır.",
                            "Դադիանի լողավազան. Նավակները կասեցվում են, եթե ջրի մակարդակը բարձր է:",
                            "בריכת רחצה דדיאני. סירות מושעות אם מפלס המים גבוה.",
                            "حمام سباحة دادياني. يتم تعليق القوارب إذا كان مستوى الماء مرتفعا."
                        ),
                        timeLabel = "D2 10:00",
                        alertType = "RAIN_DEPENDENT",
                        location = GeoPoint(42.4570, 42.3770),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/06/Emily-Lush-Batumi-Botanical-Garden-H-10.jpg"
                    ),
                    BattleNode(
                        title = L("Oda Family Marani", "ოჯახური მარანი 'ოდა'", "Семейный марани 'Ода'", "Oda Aile Marani", "Օդա ընտանիք Մարանի", "מרני משפחת אודה", "عائلة أودا ماراني"),
                        description = L(
                            "Gastro-tourism benchmark. Spicy Megrelian cuisine. Rare Ojaleshi wine.",
                            "გასტრო-ტურიზმის ეტალონი. ცხარე მეგრული სამზარეულო. იშვიათი ოჯალეში.",
                            "Эталон гастротуризма. Острая мегрельская кухня. Редкое вино Оджалеши.",
                            "Gastro-turizm kriteri. Baharatlı Megrel mutfağı. Nadir Ojaleshi şarabı.",
                            "Գաստրո-զբոսաշրջության նշաձող. Կծու մեգրելական խոհանոց. Հազվագյուտ Օջալեշի գինի:",
                            "אמת מידה לגסטרו-תיירות. מטבח מגרלי חריף. יין אוז'לשי נדיר.",
                            "معيار السياحة المعدية. المطبخ الميغريلي الحار. نبيذ أوجاليشي نادر."
                        ),
                        timeLabel = "D2 13:00",
                        location = GeoPoint(42.4100, 42.3700),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/07/Emily-Lush-Imereti-Wine-Route-Baias-Wine-qvevri.jpg"
                    ),
                    BattleNode(
                        title = L("Okatse Canyon", "ოკაცეს კანიონი", "Каньон Окаце", "Okatse Kanyonu", "Օկացե կիրճ", "קניון אוקאטסה", "وادي أوكاتسي"),
                        description = L(
                            "Hanging walkway. 10:00-18:00.",
                            "კიდული ბილიკი. 10:00-18:00.",
                            "Подвесная дорожка. 10:00-18:00.",
                            "Asma yürüyüş yolu. 10:00-18:00.",
                            "Կախովի միջանցք. 10:00-18:00:",
                            "שביל תלוי. 10:00-18:00.",
                            "ممر معلق. 10:00-18:00."
                        ),
                        timeLabel = "D2 15:30",
                        location = GeoPoint(42.4550, 42.5500),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/05/Emily-Lush-day-trips-from-Batumi-Waterfall.jpg"
                    ),
                    BattleNode(
                        title = L("Kinchkha Waterfall", "კინჩხას ჩანჩქერი", "Водопад Кинчха", "Kinchkha Şelalesi", "Կինչխա ջրվեժ", "מפל קינצ'קה", "شلال كينشخا"),
                        description = L(
                            "Massive waterfall nearby.",
                            "მასიური ჩანჩქერი.",
                            "Массивный водопад поблизости.",
                            "Yakındaki devasa şelale.",
                            "Զանգվածային ջրվեժ մոտակայքում:",
                            "מפל עצום בקרבת מקום.",
                            "شلال ضخم قريب."
                        ),
                        timeLabel = "D2 17:00",
                        location = GeoPoint(42.4950, 42.5530),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2021/05/Emily-Lush-day-trips-from-Batumi-Waterfall.jpg"
                    ),
                    BattleNode(
                        title = L("Bikentia's Kebabery", "ბიკენტიას საქაბაბე", "Кебабная у Бикентии", "Bikentia'nın Kebapçısı", "Բիկենտիայի Քյաբաբանոց", "הקבבייה של ביקנטיה", "كباب بيكنتيا"),
                        description = L(
                            "Cult spot. Standing only. Beef kebabs and lemonade.",
                            "საკულტო ადგილი. მხოლოდ ფეხზე დგომით. საქონლის ქაბაბი და ლიმონათი.",
                            "Культовое место. Только стоя. Говяжий кебаб и лимонад.",
                            "Kült noktası. Sadece ayakta. Dana kebap ve limonata.",
                            "Պաշտամունքային տեղ. Միայն կանգնած: Տավարի քյաբաբ և լիմոնադ:",
                            "מקום פולחן. עמידה בלבד. קבב בקר ולימונדה.",
                            "بقعة عبادة. واقفا فقط. كباب بقر وعصير ليمون."
                        ),
                        timeLabel = "D2 20:00",
                        alertType = "FAST_EAT",
                        location = GeoPoint(42.2720, 42.7050),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2019/07/Emily-Lush-Kutaisi-Restaurants-Bikentinas.jpg"
                    ),

                    // DAY 3
                    BattleNode(
                        title = L("Bagrati Cathedral", "ბაგრატის ტაძარი", "Храм Баграта", "Bagrati Katedrali", "Բագրատի տաճար", "קתדרלת בגרטי", "كاتدرائية باغراتي"),
                        description = L(
                            "11th-century icon. Rebuilt with controversial glass elevator.",
                            "მე-11 საუკუნის სიმბოლო. აღდგენილია მინის ლიფტით.",
                            "Икона 11 века. Восстановлен со спорным стеклянным лифтом.",
                            "11. yüzyıl ikonu. Tartışmalı cam asansörle yeniden inşa edildi.",
                            "11-րդ դարի պատկերակ. Վերակառուցվել է վիճելի ապակե վերելակով:",
                            "אייקון מהמאה ה-11. נבנה מחדש עם מעלית זכוכית שנויה במחלוקת.",
                            "أيقونة القرن الحادي عشر. أعيد بناؤها بمصعد زجاجي مثير للجدل."
                        ),
                        timeLabel = "D3 09:00",
                        location = GeoPoint(42.2770, 42.7040),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2022/04/Emily-Lush-things-to-do-in-Batumi-Georgia-Cathedral.jpg"
                    ),
                    BattleNode(
                        title = L("Botanical Garden", "ბოტანიკური ბაღი", "Ботанический сад", "Botanika Bahçesi", "Բուսաբանական այգի", "גן בוטני", "حديقة نباتية"),
                        description = L(
                            "Mtsvane Kontskhi. Vertical climate zones.",
                            "მწვანე კონცხი. ვერტიკალური კლიმატური ზონები.",
                            "Мцване Концхи (Зеленый мыс). Вертикальные климатические зоны.",
                            "Mtsvane Kontskhi. Dikey iklim bölgeleri.",
                            "Մծվանե Կոնցխի. Ուղղահայաց կլիմայական գոտիներ:",
                            "מצבאנה קונצחי. אזורי אקלים אנכיים.",
                            "متسفاني كونتسخي. المناطق المناخية العمودية."
                        ),
                        timeLabel = "D3 14:00",
                        location = GeoPoint(41.6930, 41.7070),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/06/Emily-Lush-Batumi-Botanical-Garden-H-10.jpg"
                    ),
                    BattleNode(
                        title = L("Fish Market", "თევზის ბაზარი", "Рыбный рынок", "Balık Pazarı", "Ձկան շուկա", "שוק דגים", "سوق السمك"),
                        description = L(
                            "The 'Blue Market'. Buy fresh fish, fry it next door.",
                            "'ლურჯი ბაზარი'. იყიდეთ ახალი თევზი და შეწვით იქვე.",
                            "'Голубой рынок'. Купите свежую рыбу и пожарьте по соседству.",
                            "'Mavi Pazar'. Taze balık alın, yan tarafta kızartın.",
                            "«Կապույտ շուկա». Գնեք թարմ ձուկ, տապակեք այն կողքի դռան մոտ:",
                            "'השוק הכחול'. קנו דג טרי, טגנו אותו בדלת ליד.",
                            "'السوق الأزرق'. اشتر السمك الطازج واقليه في المنزل المجاور."
                        ),
                        timeLabel = "D3 17:00",
                        location = GeoPoint(41.6490, 41.6620),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/07/Emily-Lush-things-to-do-in-Batumi-Georgia-Fish-Market-3.jpg"
                    ),
                    BattleNode(
                        title = L("Ali & Nino", "ალი და ნინო", "Али и Нино", "Ali ve Nino", "Ալի և Նինո", "עלי ונינו", "علي ونينو"),
                        description = L(
                            "Kinetic sculpture. Symbol of eternal meeting and separation.",
                            "კინეტიკური სკულპტურა. მარადიული შეხვედრისა და განშორების სიმბოლო.",
                            "Кинетическая скульптура. Символ вечной встречи и расставания.",
                            "Kinetik heykel. Sonsuz buluşma ve ayrılığın sembolü.",
                            "Կինետիկ քանդակ. Հավերժական հանդիպման և բաժանման խորհրդանիշ:",
                            "פסל קינטי. סמל למפגש ופרידה נצחיים.",
                            "النحت الحركي. رمز اللقاء والانفصال الأبدي."
                        ),
                        timeLabel = "D3 19:00",
                        location = GeoPoint(41.6555, 41.6430),
                        imageUrl = "https://wander-lush.org/wp-content/uploads/2020/07/Emily-Lush-things-to-do-in-Batumi-Georgia-Ali-and-Nino-statue.jpg"
                    ),
                    BattleNode(
                        title = L("Retro", "რეტრო", "Ретро", "Retro", "Ռետրո", "רטרו", "ريترو"),
                        description = L(
                            "Home of the 'Titanic' Adjaruli Khachapuri. Mixing egg/butter is mandatory.",
                            "'ტიტანიკის' ზომის აჭარული ხაჭაპური. კვერცხისა და კარაქის არევა სავალდებულოა.",
                            "Дом аджарского хачапури размера 'Титаник'. Перемешивание яйца и масла обязательно.",
                            "'Titanik' Adjaruli Khachapuri'nin evi. Yumurta/tereyağı karıştırmak zorunludur.",
                            "«Տիտանիկ» Աջարուլի Խաչապուրիի տունը։ Ձու/կարագ խառնելը պարտադիր է:",
                            "הבית של חצ'אפורי אג'רולי 'טיטאניק'. ערבוב ביצה/חמאה הוא חובה.",
                            "منزل 'تيتانيك' أجارولي خاشابوري. خلط البيض / الزبدة إلزامي."
                        ),
                        timeLabel = "D3 20:30",
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
