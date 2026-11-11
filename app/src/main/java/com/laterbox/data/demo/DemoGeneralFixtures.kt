package com.laterbox.data.demo

import com.laterbox.data.model.ContentType
import org.json.JSONArray
import java.util.Calendar

data class DemoGeneralItemTemplate(
    val url: String,
    val title: String,
    val description: String?,
    val sourceDomain: String?,
    val sourceApp: String?,
    val contentType: ContentType,
    val imageFixtureIndex: Int? = null,
    val daysAgo: Long = 0,
    val isFavorite: Boolean = false,
    val readProgress: Float = 0f,
    val isSnap: Boolean = false,
    val captureMethod: String? = null,
    val transcript: String? = null,
    val extractedText: String? = null,
    val playlistNames: List<String> = emptyList(),
    val tagNames: List<String> = emptyList(),
    val reminderTemplate: DemoReminderTemplate? = null,
    val aiAnalysisTemplate: DemoAiAnalysisTemplate? = null
)

data class DemoReminderTemplate(
    val eventType: String = "REMINDER",
    val eventTitle: String? = null,
    val personName: String? = null,
    val relationship: String? = null,
    val secondPersonName: String? = null,
    val dueDaysFromNow: Long = 1,
    val originalBirthOrAnnivDate: Long? = null,
    val isCompleted: Boolean = false,
    val note: String? = null,
    val reminderOffsetsDays: List<Int> = listOf(0)
) {
    fun getReminderOffsetsDaysJson(): String {
        val arr = JSONArray()
        reminderOffsetsDays.forEach { arr.put(it) }
        return arr.toString()
    }
}

data class DemoAiAnalysisTemplate(
    val status: String = "ANALYZED",
    val shortDescription: String,
    val whatItsAbout: String,
    val category: String,
    val keyTakeaways: List<String> = emptyList(),
    val peopleAndEntities: List<String> = emptyList(),
    val locationName: String? = null,
    val errorMessage: String? = null
) {
    fun getKeyTakeawaysJson(): String = JSONArray(keyTakeaways).toString()
    fun getPeopleAndEntitiesJson(): String = JSONArray(peopleAndEntities).toString()
}

object DemoGeneralFixtures {

    val playlists = listOf(
        "Projects & Work",
        "Reading & Research",
        "Travel & Inspiration",
        "Personal & Home",
        "Code & Tech"
    )

    private fun dateOf(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 9, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // Exactly 100 General Item Templates
    val items: List<DemoGeneralItemTemplate> = buildList {

        // ==========================================
        // 1. Saved Webpages & Articles (20 items)
        // ==========================================
        add(
            DemoGeneralItemTemplate(
                url = "https://developer.android.example/compose/performance-2026",
                title = "Jetpack Compose Performance Best Practices in 2026",
                description = "Comprehensive guide on recomposition pruning, stability configuration, and layout inspector profiling in Jetpack Compose.",
                sourceDomain = "developer.android.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 1,
                isFavorite = true,
                readProgress = 0.45f,
                playlistNames = listOf("Code & Tech", "Reading & Research"),
                tagNames = listOf("android", "compose", "performance"),
                aiAnalysisTemplate = DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Optimization techniques for high-frame-rate Compose apps.",
                    whatItsAbout = "Performance tuning for Compose rendering and StateFlow subscriptions.",
                    category = "Technology",
                    keyTakeaways = listOf("Use immutable wrappers for collections", "Defer state reads to draw phase", "Avoid unnecessary modifier chaining"),
                    peopleAndEntities = listOf("Android Jetpack", "Kotlin Compiler", "Google")
                )
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://techjournal.example/local-first-software-architecture",
                title = "The Evolution of Local-First Mobile Architectures",
                description = "Why local SQLite databases with zero cloud dependencies deliver the most resilient user experience.",
                sourceDomain = "techjournal.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 3,
                isFavorite = true,
                readProgress = 0.85f,
                playlistNames = listOf("Code & Tech"),
                tagNames = listOf("architecture", "sqlite", "privacy"),
                aiAnalysisTemplate = DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Deep dive into local-first and offline-first mobile applications.",
                    whatItsAbout = "Principles of local storage, instant UI feedback, and peer-to-peer data sync.",
                    category = "Software Engineering",
                    keyTakeaways = listOf("Local databases eliminate network latency", "Offline availability ensures trust", "Device backups give user true ownership"),
                    peopleAndEntities = listOf("Martin Kleppmann", "Ink & Switch", "Room DB")
                )
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://waitbutwhy.example/intentional-focus-digital-wellbeing",
                title = "The Art of Intentional Focus and Digital Wellbeing",
                description = "Strategies to curate saved content, overcome notification fatigue, and maintain deep reading habits.",
                sourceDomain = "waitbutwhy.example",
                sourceApp = "Pocket",
                contentType = ContentType.ARTICLE,
                daysAgo = 6,
                isFavorite = false,
                readProgress = 0.20f,
                playlistNames = listOf("Reading & Research"),
                tagNames = listOf("productivity", "wellbeing", "habits")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://traveler.example/kyoto-hidden-temples-tea-houses",
                title = "Kyoto Travel Guide: Hidden Temples and Quiet Paths",
                description = "Walking routes through eastern Kyoto, historical tea ceremonies, and seasonal garden highlights.",
                sourceDomain = "traveler.example",
                sourceApp = "Browser",
                contentType = ContentType.ARTICLE,
                daysAgo = 12,
                isFavorite = true,
                readProgress = 0.60f,
                playlistNames = listOf("Travel & Inspiration"),
                tagNames = listOf("travel", "japan", "kyoto"),
                aiAnalysisTemplate = DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Curated itinerary for culturally immersive Kyoto exploration.",
                    whatItsAbout = "Lesser-known shrines, Zen stone gardens, and morning tea experiences.",
                    category = "Travel",
                    keyTakeaways = listOf("Visit early morning before crowd peaks", "Preserve temple etiquette and shoe removal protocols", "Reserve traditional machiya stays in advance"),
                    peopleAndEntities = listOf("Kyoto", "Higashiyama", "Arashiyama"),
                    locationName = "Kyoto, Japan"
                )
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://pragmatic.example/clean-architecture-in-production",
                title = "Pragmatic Clean Architecture: Avoiding Premature Abstraction",
                description = "A senior developer perspective on keeping layers minimal and letting code evolve naturally.",
                sourceDomain = "pragmatic.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 18,
                isFavorite = false,
                readProgress = 0.90f,
                playlistNames = listOf("Code & Tech"),
                tagNames = listOf("architecture", "best-practices"),
                aiAnalysisTemplate = DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Pragmatic software architecture rules and avoiding YAGNI overhead.",
                    whatItsAbout = "Senior software engineering philosophy on sensible layer abstractions.",
                    category = "Software Engineering",
                    keyTakeaways = listOf("Avoid speculative interfaces", "Keep domain entities clean", "Embrace simplicity"),
                    peopleAndEntities = listOf("Clean Architecture", "Kotlin")
                )
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://economist.example/semiconductor-supply-chain-insights",
                title = "Global Semiconductor Dynamics and Next-Gen Lithography",
                description = "High-NA EUV developments and the geographical shifts in silicon foundry capacities.",
                sourceDomain = "economist.example",
                sourceApp = "Browser",
                contentType = ContentType.ARTICLE,
                daysAgo = 25,
                isFavorite = false,
                playlistNames = listOf("Reading & Research"),
                tagNames = listOf("hardware", "business", "economics")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://designsystems.example/fluid-typography-scales",
                title = "Mathematical Foundations of Modern Fluid Typography",
                description = "Implementing modular typography scales that scale smoothly across responsive breakpoints.",
                sourceDomain = "designsystems.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 32,
                isFavorite = true,
                playlistNames = listOf("Projects & Work"),
                tagNames = listOf("design", "typography", "css"),
                aiAnalysisTemplate = DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Formulas for responsive typography and dynamic modular scales.",
                    whatItsAbout = "Design tokens and CSS clamping formulas for fluid type across screen widths.",
                    category = "Design",
                    keyTakeaways = listOf("Use CSS clamp for fluid scaling", "Choose consistent ratio progression", "Verify optical legibility"),
                    peopleAndEntities = listOf("Design Systems", "Typography")
                )
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://nordiccoffee.example/water-chemistry-extraction",
                title = "Water Mineralization and Specialty Coffee Extraction",
                description = "How calcium and magnesium ions bind volatile flavor compounds during extraction.",
                sourceDomain = "nordiccoffee.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 40,
                playlistNames = listOf("Personal & Home"),
                tagNames = listOf("coffee", "science", "recipes"),
                aiAnalysisTemplate = DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Water mineral composition impact on specialty coffee flavor extraction.",
                    whatItsAbout = "Brew water chemistry, total dissolved solids, and magnesium vs calcium ratios.",
                    category = "Science & Food",
                    keyTakeaways = listOf("Magnesium boosts fruity flavor notes", "Calcium highlights heavier body", "Target 120-150 ppm TDS"),
                    peopleAndEntities = listOf("Specialty Coffee Association", "Brewing")
                )
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://nature.example/circadian-sleep-architectures",
                title = "Circadian Rhythm Biology and Light Exposure Protocols",
                description = "Peer-reviewed insights into morning photons, melatonin regulation, and sustained cognitive focus.",
                sourceDomain = "nature.example",
                sourceApp = "Browser",
                contentType = ContentType.ARTICLE,
                daysAgo = 55,
                tagNames = listOf("health", "science", "habits"),
                aiAnalysisTemplate = DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Peer-reviewed analysis of light exposure protocols and sleep architecture.",
                    whatItsAbout = "Morning lux exposure, melanopsin stimulation, and restorative deep sleep.",
                    category = "Health & Science",
                    keyTakeaways = listOf("View natural sunlight within 60 mins of waking", "Dim ambient indoor light after sunset", "Consistent sleep-wake times"),
                    peopleAndEntities = listOf("Nature Neuroscience", "Circadian Biology")
                )
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://urbanism.example/15-minute-city-urban-planning",
                title = "The 15-Minute City: Urban Density and Human Scale",
                description = "Case studies from Paris, Barcelona, and Melbourne on walkable neighborhood clusters.",
                sourceDomain = "urbanism.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 70,
                tagNames = listOf("urbanism", "architecture")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://gardening.example/indoor-air-purifying-plants",
                title = "Top 10 Resilient Houseplants for Low-Light Workspaces",
                description = "Sansevieria, Zamioculcas, and Pothos maintenance tips for vibrant indoor green spaces.",
                sourceDomain = "gardening.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 85,
                playlistNames = listOf("Personal & Home"),
                tagNames = listOf("plants", "home")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://culinary.example/sourdough-microbiology",
                title = "Microbiology of Long Cold Fermentation Sourdough",
                description = "Lactic acid bacteria balance, gluten structure development, and blistering crust secrets.",
                sourceDomain = "culinary.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 95,
                playlistNames = listOf("Personal & Home"),
                tagNames = listOf("baking", "food"),
                aiAnalysisTemplate = DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Microbiology and gluten kinetics in artisanal sourdough baking.",
                    whatItsAbout = "Wild yeast activity, enzymatic flour breakdown, and steam oven protocols.",
                    category = "Culinary Science",
                    keyTakeaways = listOf("80%+ hydration requires autolyse", "Cold retard develops organic acids", "High steam creates blisters"),
                    peopleAndEntities = listOf("Tartine", "Sourdough Guild")
                )
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://running.example/trail-endurance-hydration-strategy",
                title = "Ultrarunning Hydration & Electrolyte Replacement Protocol",
                description = "Optimizing sodium ingestion rates and gastric emptying during sustained endurance efforts.",
                sourceDomain = "running.example",
                sourceApp = "Browser",
                contentType = ContentType.ARTICLE,
                daysAgo = 110,
                tagNames = listOf("fitness", "running")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://acoustics.example/home-studio-treatment-guide",
                title = "Bass Trapping and Diffusion for Small Listening Rooms",
                description = "Calculations for axial room modes and DIY acoustic panel placement.",
                sourceDomain = "acoustics.example",
                sourceApp = "Chrome",
                contentType = ContentType.ARTICLE,
                daysAgo = 130,
                tagNames = listOf("audio", "music", "diy")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://philosophy.example/stoic-journaling-techniques",
                title = "Marcus Aurelius: The Mechanics of Evening Reflection",
                description = "Historical examination of self-examination routines and psychological resilience.",
                sourceDomain = "philosophy.example",
                sourceApp = "Pocket",
                contentType = ContentType.ARTICLE,
                daysAgo = 160,
                tagNames = listOf("philosophy", "books", "mindset")
            )
        )
        // 5 Web Links
        add(
            DemoGeneralItemTemplate(
                url = "https://example.com/tools/color-contrast-analyzer",
                title = "WebAIM WCAG AAA Color Contrast Accessibility Checker",
                description = "Real-time contrast verification tool conforming to WCAG 2.1 compliance guidelines.",
                sourceDomain = "example.com",
                sourceApp = "Chrome",
                contentType = ContentType.LINK,
                daysAgo = 2,
                playlistNames = listOf("Projects & Work"),
                tagNames = listOf("design", "accessibility")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://example.com/resources/open-font-license-library",
                title = "Google Fonts Specimen Showcase: Geist & Inter",
                description = "Geometric sans-serif typeface pairing examples for high-density dashboard layouts.",
                sourceDomain = "example.com",
                sourceApp = "Chrome",
                contentType = ContentType.LINK,
                daysAgo = 15,
                tagNames = listOf("fonts", "typography")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://example.com/maps/dolomites-alta-via-1-route",
                title = "Alta Via 1 Trail Map & Refugio Booking Portal",
                description = "High mountain hut booking reservations and elevation profile for the classic Italian Alps trek.",
                sourceDomain = "example.com",
                sourceApp = "Browser",
                contentType = ContentType.LINK,
                daysAgo = 45,
                playlistNames = listOf("Travel & Inspiration"),
                tagNames = listOf("travel", "hiking", "italy")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://example.com/documentation/sqlite-wal-mode",
                title = "SQLite Write-Ahead Logging (WAL) Tuning Documentation",
                description = "Official technical explanation of concurrent readers and single-writer concurrency in SQLite.",
                sourceDomain = "example.com",
                sourceApp = "Chrome",
                contentType = ContentType.LINK,
                daysAgo = 60,
                playlistNames = listOf("Code & Tech"),
                tagNames = listOf("sqlite", "database")
            )
        )
        add(
            DemoGeneralItemTemplate(
                url = "https://example.com/audio/synthesizer-patch-archive",
                title = "Analog Synthesizer Preset Archive: Moog & Prophet Patches",
                description = "Community database of vintage synthesizer oscillator configurations and filter envelopes.",
                sourceDomain = "example.com",
                sourceApp = "Chrome",
                contentType = ContentType.LINK,
                daysAgo = 120,
                tagNames = listOf("audio", "synth")
            )
        )

        // ==========================================
        // 2. Images and Screenshots (15 items)
        // ==========================================
        for (idx in 0 until 15) {
            val isEven = idx % 2 == 0
            val fixtureIdx = idx % 15
            add(
                DemoGeneralItemTemplate(
                    url = "media://synthetic/image_$idx.jpg",
                    title = when (fixtureIdx) {
                        0 -> "Minimalist Architectural Facade at Dusk"
                        1 -> "Dolomites Mountain Sunrise Topography"
                        2 -> "Mobile App Design System Component Kit"
                        3 -> "Analog Synth Patch Panel & Ribbon Controller"
                        4 -> "Chemex Pour-Over Extraction Flow Diagram"
                        5 -> "Ergonomic Dual-Monitor Sit-Stand Workstation"
                        6 -> "Modern Data Lakehouse ETL Pipeline Flowchart"
                        7 -> "Editorial Typography Scale & Grid Blueprint"
                        8 -> "Kyoto Arashiyama Bamboo Forest Morning Walk"
                        9 -> "Monstera Deliciosa Indoor Plant Health Tracker"
                        10 -> "Classic 35mm Prime Lens Optics Diagram"
                        11 -> "Vibram MegaGrip Trail Lug Profile Benchmark"
                        12 -> "Hardcover Book Spine Showcase & Shelf Layout"
                        13 -> "Embedded RISC-V SoC Architecture Floorplan"
                        else -> "Artisan Sourdough Crumb Cross-Section Photo"
                    },
                    description = "Captured visual reference for creative inspiration and design exploration.",
                    sourceDomain = "local.gallery",
                    sourceApp = if (isEven) "Camera" else "Gallery",
                    contentType = ContentType.IMAGE,
                    imageFixtureIndex = fixtureIdx,
                    daysAgo = (idx * 6L + 1),
                    isFavorite = idx % 3 == 0,
                    isSnap = isEven,
                    captureMethod = if (isEven) "CAMERA" else "MANUAL_IMPORT",
                    playlistNames = if (idx % 2 == 0) listOf("Travel & Inspiration") else listOf("Personal & Home"),
                    tagNames = listOf("photography", "inspiration", "visual"),
                    aiAnalysisTemplate = if (idx < 5) DemoAiAnalysisTemplate(
                        status = "ANALYZED",
                        shortDescription = "High-contrast visual composition with clean focal hierarchy.",
                        whatItsAbout = "Visual scene capture categorized under design and photography.",
                        category = "Photography",
                        keyTakeaways = listOf("Natural gradient lighting", "Geometric balanced alignment", "High aesthetic clarity")
                    ) else null
                )
            )
        }

        // ==========================================
        // 3. Quick Text Captures and Snippets (15 items)
        // ==========================================
        val quickSnippets = listOf(
            Pair("Kotlin Coroutine Context Preservation", "val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)\nAlways supervise non-cancellable background jobs."),
            Pair("Idea: Smart Tag Aggregator", "What if Later Box automatically clusters items with similar AI themes into suggested dynamic tags?"),
            Pair("Coffee Grind Size Reference", "Espresso: 200um\nPour-over (V60): 650um\nFrench Press: 950um\nCold Brew: 1100um"),
            Pair("Book Recommendation: Thinking in Systems", "Donella Meadows classic. Key principle: Don't blame people, inspect the system feedback loops."),
            Pair("App Architecture Principle", "UI = f(State). State = f(Intent, PreviousState). Keep unidirectional data flow inviolate."),
            Pair("Packing Checklist Seed Note", "Passport, USB-C 65W GaN brick, universal plug adapter, merino base layer, noise-cancelling buds."),
            Pair("Keyboard Shortcut Idea", "Cmd+Shift+K to open Gemini Ask Question modal directly from anywhere in the app."),
            Pair("Design Token: Corner Radii", "Base: 8dp, Card: 16dp, Sheet: 24dp, Pill: 999dp. Maintain strict consistency."),
            Pair("Weekly Review Habit", "Every Sunday at 6 PM: Clean inbox, review completed reminders, verify pending goals."),
            Pair("Typography Scale Ratio", "Major Second: 1.125\nMinor Third: 1.200\nMajor Third: 1.250\nPerfect Fourth: 1.333 (Recommended for dashboards)"),
            Pair("Room Query Optimization Note", "Remember to add indices on all foreign keys and columns frequently used in ORDER BY and WHERE."),
            Pair("Hiking Route Note: Mt. Fuji", "Yoshida trail opens July 1st. Mountain huts require reservation exactly 90 days in advance."),
            Pair("Gift Ideas for Maya", "1. Ceramic pour-over dripper\n2. Noise-cancelling headband\n3. Hardcover photography book"),
            Pair("SQLite Concurrency Rule", "Enable Write-Ahead Logging (WAL) and synchronous = NORMAL for 4x faster bulk insertions."),
            Pair("Quote from Dieter Rams", "'Good design is as little design as possible. Less, but better.'")
        )

        quickSnippets.forEachIndexed { i, (snipTitle, snipBody) ->
            val aiAnalysis = when (i) {
                2 -> DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Grind particle micrometer reference guide for various brewing methods.",
                    whatItsAbout = "Micron grind measurements from espresso to cold brew.",
                    category = "Coffee & Beverage",
                    keyTakeaways = listOf("V60 requires 650um", "Espresso requires fine 200um", "Cold brew requires coarse 1100um"),
                    peopleAndEntities = listOf("Coffee Extraction")
                )
                14 -> DemoAiAnalysisTemplate(
                    status = "ANALYZED",
                    shortDescription = "Classic Dieter Rams design philosophy aphorism.",
                    whatItsAbout = "Principles of good functionalist design and aesthetic restraint.",
                    category = "Design Philosophy",
                    keyTakeaways = listOf("Less, but better", "Eliminate extraneous ornament", "Function drives form"),
                    peopleAndEntities = listOf("Dieter Rams", "Braun")
                )
                else -> null
            }
            add(
                DemoGeneralItemTemplate(
                    url = "note://snippet/$i",
                    title = snipTitle,
                    description = snipBody.take(120),
                    sourceDomain = "memo.internal",
                    sourceApp = "Later Box",
                    contentType = ContentType.NOTE,
                    daysAgo = (i * 3L),
                    isFavorite = i % 4 == 0,
                    isSnap = true,
                    captureMethod = "QUICK_NOTE",
                    extractedText = snipBody,
                    playlistNames = listOf("Projects & Work"),
                    tagNames = listOf("ideas", "notes"),
                    aiAnalysisTemplate = aiAnalysis
                )
            )
        }

        // ==========================================
        // 4. Voice/Audio Sample Records (10 items)
        // ==========================================
        val audioTranscripts = listOf(
            "Morning voice note: Remember to verify the room database migration from schema 5 to 6 before triggering release builds. Ensure SQLite cascading deletes are fully covered by unit tests.",
            "Quick reflection on product strategy: Keep the application interface minimal, spacious, and distraction-free. Do not clutter the home feed with unnecessary banners.",
            "Ideas for weekend trip: We should drive up to the mountain cabin around 8 AM on Saturday to beat morning traffic. Bring hiking boots and trail snacks.",
            "Sprint retro thoughts: The new rich notes editor is significantly faster than our previous web-based prototype. Zero lag during rapid paragraph typing.",
            "Podcast takeaway: The human brain works best with focused 90-minute ultradian rhythm cycles followed by brief walking breaks.",
            "Grocery brainstorming: Almond milk, sourdough loaf, ripe avocados, organic eggs, ground Ethiopian coffee, and olive oil.",
            "Architectural note: The reason we avoided third-party editor libraries was to maintain absolute stability across future Kotlin compiler and Compose BOM updates.",
            "Voice memo for birthday planning: Book the garden table at the Italian trattoria for six people next Friday evening.",
            "Meeting summary memo: The team agreed that local-first data ownership is our core brand value. No cloud telemetry or third-party tracking.",
            "Evening mindfulness check-in: Today's focus sessions were highly productive. Remember to keep screen brightness low after 9 PM."
        )

        audioTranscripts.forEachIndexed { i, transcriptText ->
            add(
                DemoGeneralItemTemplate(
                    url = "audio://synthetic/recording_$i.m4a",
                    title = "Voice Memo: " + transcriptText.take(45) + "…",
                    description = transcriptText,
                    sourceDomain = "voice.recorder",
                    sourceApp = "Later Box",
                    contentType = ContentType.AUDIO,
                    daysAgo = (i * 4L + 1),
                    isFavorite = i == 0 || i == 3,
                    isSnap = true,
                    captureMethod = "AUDIO_RECORDING",
                    transcript = transcriptText,
                    playlistNames = listOf("Personal & Home"),
                    tagNames = listOf("voice", "audio", "memo"),
                    aiAnalysisTemplate = DemoAiAnalysisTemplate(
                        status = "ANALYZED",
                        shortDescription = "Audio recording with automated speech-to-text transcript.",
                        whatItsAbout = "Spoken thoughts captured via voice memo.",
                        category = "Voice Memos",
                        keyTakeaways = listOf("Transcribed successfully", "Key action points documented"),
                        peopleAndEntities = listOf("Later Box Voice")
                    )
                )
            )
        }

        // ==========================================
        // 5. Tasks and Reminders (10 items)
        // ==========================================
        val tasks = listOf(
            Triple("Review Q4 Engineering Budget Proposal", "Examine infrastructure spend, device test lab budget, and contractor allocations.", 1L),
            Triple("Schedule Annual Vehicle Maintenance", "Oil change, tire rotation, brake pad inspection, and cabin air filter replacement.", 3L),
            Triple("Renew International Driving Permit", "Submit passport photos and current state driving license copy to local agency.", 7L),
            Triple("File Quarterly Estimated Business Taxes", "Verify 1099 deductions and schedule payment via tax portal.", 14L),
            Triple("Doctor Annual Health Checkup", "Blood work panel, cholesterol verification, and vitamin D check.", 21L),
            Triple("Complete Home HVAC Filter Replacement", "Swap Merv-13 air filters in hallway intake and upstairs plenum.", -2L), // Overdue
            Triple("Verify Off-Site Encrypted Backup Drive", "Run full restore test from cold storage SSD into sandbox environment.", 30L),
            Triple("Order High-Altitude Trekking Gear", "Down jacket, trekking poles, thermal gloves, and water purification tablets.", 5L),
            Triple("Finalize Presentation Slides for Tech Summit", "Export Chart.js visuals into presentation deck and rehearse timing.", 10L),
            Triple("Clean & Reseason Cast Iron Skillets", "Apply thin grape-seed oil coating and bake at 450 degrees for one hour.", -5L) // Completed
        )

        tasks.forEachIndexed { i, (taskTitle, taskDesc, dueDays) ->
            val isOverdueOrDone = dueDays < 0
            add(
                DemoGeneralItemTemplate(
                    url = "task://synthetic/task_$i",
                    title = taskTitle,
                    description = taskDesc,
                    sourceDomain = "tasks.internal",
                    sourceApp = "Later Box",
                    contentType = ContentType.NOTE,
                    daysAgo = 2L,
                    isFavorite = i % 3 == 0,
                    playlistNames = listOf("Projects & Work"),
                    tagNames = listOf("tasks", "reminders"),
                    reminderTemplate = DemoReminderTemplate(
                        eventType = "REMINDER",
                        eventTitle = taskTitle,
                        dueDaysFromNow = dueDays,
                        isCompleted = isOverdueOrDone,
                        note = taskDesc,
                        reminderOffsetsDays = listOf(0)
                    )
                )
            )
        }

        // ==========================================
        // 6. Birthday and Anniversary Memories (10 items)
        // ==========================================
        val smartEvents = listOf(
            // 1. Upcoming birthday in 5 days
            DemoReminderTemplate(
                eventType = "BIRTHDAY",
                eventTitle = "Emma Watson's Birthday",
                personName = "Emma Watson",
                relationship = "Friend",
                dueDaysFromNow = 5L,
                originalBirthOrAnnivDate = dateOf(1994, 11, 16),
                note = "Enjoys hand-poured ceramic cups and matcha.",
                reminderOffsetsDays = listOf(30, 7, 3, 1, 0)
            ),
            // 2. Birthday today
            DemoReminderTemplate(
                eventType = "BIRTHDAY",
                eventTitle = "David Miller's Birthday",
                personName = "David Miller",
                relationship = "Colleague",
                dueDaysFromNow = 0L,
                originalBirthOrAnnivDate = dateOf(1995, 11, 11),
                note = "Senior Android engineer on the partner team.",
                reminderOffsetsDays = listOf(7, 1, 0)
            ),
            // 3. Birthday passed this year
            DemoReminderTemplate(
                eventType = "BIRTHDAY",
                eventTitle = "Sarah Jenkins's Birthday",
                personName = "Sarah Jenkins",
                relationship = "Sister",
                dueDaysFromNow = 270L,
                originalBirthOrAnnivDate = dateOf(1989, 8, 14),
                note = "Sent book package earlier in the summer.",
                reminderOffsetsDays = listOf(30, 7, 1, 0)
            ),
            // 4. Anniversary approaching in 3 days
            DemoReminderTemplate(
                eventType = "ANNIVERSARY",
                eventTitle = "Michael & Jessica's Anniversary",
                personName = "Michael",
                secondPersonName = "Jessica",
                relationship = "Couple",
                dueDaysFromNow = 3L,
                originalBirthOrAnnivDate = dateOf(2014, 11, 14),
                note = "Celebrating their 10th anniversary!",
                reminderOffsetsDays = listOf(30, 7, 3, 1, 0)
            ),
            // 5. Anniversary in the future
            DemoReminderTemplate(
                eventType = "ANNIVERSARY",
                eventTitle = "Robert & Linda's Silver Anniversary",
                personName = "Robert",
                secondPersonName = "Linda",
                relationship = "Parents",
                dueDaysFromNow = 40L,
                originalBirthOrAnnivDate = dateOf(1999, 12, 21),
                note = "25th Silver Wedding Anniversary.",
                reminderOffsetsDays = listOf(30, 14, 7, 1, 0)
            ),
            // 6. Multiple on same day (1)
            DemoReminderTemplate(
                eventType = "BIRTHDAY",
                eventTitle = "Alex Rivera's Birthday",
                personName = "Alex Rivera",
                relationship = "Brother",
                dueDaysFromNow = 9L,
                originalBirthOrAnnivDate = dateOf(1998, 11, 20),
                note = "Twin celebrations with cousin Liam.",
                reminderOffsetsDays = listOf(7, 1, 0)
            ),
            // 7. Multiple on same day (2)
            DemoReminderTemplate(
                eventType = "BIRTHDAY",
                eventTitle = "Liam Chen's Birthday",
                personName = "Liam Chen",
                relationship = "Cousin",
                dueDaysFromNow = 9L,
                originalBirthOrAnnivDate = dateOf(2001, 11, 20),
                note = "Twin celebrations with brother Alex.",
                reminderOffsetsDays = listOf(7, 1, 0)
            ),
            // 8. Leap-day birthday
            DemoReminderTemplate(
                eventType = "BIRTHDAY",
                eventTitle = "Jordan Taylor's Leap-Day Birthday",
                personName = "Jordan Taylor",
                relationship = "Friend",
                dueDaysFromNow = 109L,
                originalBirthOrAnnivDate = dateOf(1996, 2, 29),
                note = "Born on leap day! Maps to Feb 28 in non-leap years.",
                reminderOffsetsDays = listOf(14, 3, 0)
            ),
            // 9. Family birthday: Mother
            DemoReminderTemplate(
                eventType = "BIRTHDAY",
                eventTitle = "Mom's Birthday",
                personName = "Mom",
                relationship = "Mother",
                dueDaysFromNow = 188L,
                originalBirthOrAnnivDate = dateOf(1965, 5, 18),
                note = "Send flowers and handmade card.",
                reminderOffsetsDays = listOf(30, 7, 3, 1, 0)
            ),
            // 10. Mentor milestone
            DemoReminderTemplate(
                eventType = "BIRTHDAY",
                eventTitle = "Dr. Arthur Pendelton's Birthday",
                personName = "Dr. Arthur Pendelton",
                relationship = "Mentor",
                dueDaysFromNow = 131L,
                originalBirthOrAnnivDate = dateOf(1958, 3, 22),
                note = "Computer Science department chair and thesis advisor.",
                reminderOffsetsDays = listOf(7, 1, 0)
            )
        )

        smartEvents.forEachIndexed { i, event ->
            add(
                DemoGeneralItemTemplate(
                    url = "event://synthetic/${event.eventType.lowercase()}_$i",
                    title = event.eventTitle ?: "Event: ${event.personName}",
                    description = event.note ?: "Synthetic annual recurrence event.",
                    sourceDomain = "events.internal",
                    sourceApp = "Later Box",
                    contentType = ContentType.NOTE,
                    daysAgo = 5L,
                    isFavorite = i < 3,
                    playlistNames = listOf("Personal & Home"),
                    tagNames = listOf(event.eventType.lowercase(), "celebration"),
                    reminderTemplate = event
                )
            )
        }

        // ==========================================
        // 7. Videos and Media References (5 items)
        // ==========================================
        val videos = listOf(
            Triple("Kotlin 2.0 Compiler Architecture & K2 Explained", "https://youtube.example.com/watch?v=k2_arch_2026", "youtube.example.com"),
            Triple("Light and Shadow: Mastering Architectural Photography", "https://vimeo.example.com/channels/photo/light_and_shadow", "vimeo.example.com"),
            Triple("The Geometry of Thought: Visual Thinking Masterclass", "https://ted.example.com/talks/geometry_of_thought", "ted.example.com"),
            Triple("Building 60FPS Fluid Gestures in Android Jetpack Compose", "https://youtube.example.com/watch?v=compose_gestures", "youtube.example.com"),
            Triple("Acoustics 101: Understanding Room Nodes and Reflections", "https://vimeo.example.com/sound_design/room_acoustics", "vimeo.example.com")
        )

        videos.forEachIndexed { i, (vTitle, vUrl, vDomain) ->
            add(
                DemoGeneralItemTemplate(
                    url = vUrl,
                    title = vTitle,
                    description = "Educational video lecture and demonstration.",
                    sourceDomain = vDomain,
                    sourceApp = "YouTube",
                    contentType = ContentType.VIDEO,
                    daysAgo = (i * 12L + 2),
                    isFavorite = i % 2 == 0,
                    readProgress = 0.35f,
                    playlistNames = listOf("Reading & Research"),
                    tagNames = listOf("video", "learning")
                )
            )
        }

        // ==========================================
        // 8. Research and Reference Memories (5 items)
        // ==========================================
        val researchPapers = listOf(
            Triple("Local LLM Quantization: 4-Bit AWQ vs GGUF Tradeoffs", "Analysis of weight-only quantization vs activation quantization on mobile NPUs.", "arxiv.example.org"),
            Triple("State of Edge Computing & SQLite In-Memory Databases", "Benchmarking memory bandwidth overheads in mobile embedded storage systems.", "ieee.example.org"),
            Triple("Ergonomics in High-Density Software Development", "Longitudinal survey on mechanical switches, mouse grip, and neck posture.", "acm.example.org"),
            Triple("Renewable Grid Energy Storage Chemistry Review", "Iron-flow batteries vs sodium-ion cells for seasonal grid balancing.", "nature.example.org"),
            Triple("Cognitive Load Theory in Modern Interface Design", "Minimizing extraneous cognitive load through progressive disclosure.", "designresearch.example.org")
        )

        researchPapers.forEachIndexed { i, (rTitle, rDesc, rDomain) ->
            add(
                DemoGeneralItemTemplate(
                    url = "https://$rDomain/papers/2026_${i + 101}.pdf",
                    title = rTitle,
                    description = rDesc,
                    sourceDomain = rDomain,
                    sourceApp = "Chrome",
                    contentType = ContentType.DOCUMENT,
                    daysAgo = (i * 15L + 5),
                    isFavorite = true,
                    readProgress = 0.50f,
                    playlistNames = listOf("Reading & Research"),
                    tagNames = listOf("research", "paper", "reference"),
                    aiAnalysisTemplate = DemoAiAnalysisTemplate(
                        status = "ANALYZED",
                        shortDescription = "Academic research paper overview.",
                        whatItsAbout = rTitle,
                        category = "Science & Research",
                        keyTakeaways = listOf("Empirical benchmark data", "Comparative architectural tradeoffs", "Published peer-reviewed methodology"),
                        peopleAndEntities = listOf(rDomain)
                    )
                )
            )
        }

        // ==========================================
        // 9. Mixed Social/Shared Content (10 items)
        // ==========================================
        val socialThreads = listOf(
            Triple("https://x.example/tech_insider/status/987123", "Thread on the future of local-first AI assistants on Android", "Twitter/X"),
            Triple("https://reddit.example/r/androiddev/comments/compose_tips", "What is your single most effective Compose performance tip?", "Reddit"),
            Triple("https://linkedin.example/posts/design_systems_lead_442", "Why our design team eliminated 80% of our custom modal dialogs", "LinkedIn"),
            Triple("https://mastodon.example/@developer/109283746", "Open source release: High-performance SQLite migration validator", "Mastodon"),
            Triple("https://github.example/arybishal/Later-Box", "Later Box: Clean, native Android memory vault with Gemini intelligence", "GitHub"),
            Triple("https://x.example/ux_critique/status/441298", "Analyzing the whitespace and breathing room in premium Scandinavian apps", "Twitter/X"),
            Triple("https://reddit.example/r/Productivity/comments/offline_notes", "Moving back to offline-only notes has cured my digital anxiety", "Reddit"),
            Triple("https://github.example/awesome/local-first-tools", "Curated catalog of offline-first mobile and desktop software", "GitHub"),
            Triple("https://linkedin.example/posts/product_leadership_881", "The case against notification badges and artificial urgency in UX", "LinkedIn"),
            Triple("https://x.example/mobile_arch/status/665511", "Why SQLite with WAL mode is still the gold standard for mobile databases", "Twitter/X")
        )

        socialThreads.forEachIndexed { i, (sUrl, sTitle, sSource) ->
            add(
                DemoGeneralItemTemplate(
                    url = sUrl,
                    title = sTitle,
                    description = "Shared post discussing technology, design, and developer experiences from $sSource.",
                    sourceDomain = sSource.lowercase().replace('/', '.'),
                    sourceApp = sSource,
                    contentType = ContentType.LINK,
                    daysAgo = (i * 7L + 2),
                    isFavorite = i % 3 == 0,
                    playlistNames = listOf("Reading & Research"),
                    tagNames = listOf("social", sSource.lowercase().take(8))
                )
            )
        }
    }
}
