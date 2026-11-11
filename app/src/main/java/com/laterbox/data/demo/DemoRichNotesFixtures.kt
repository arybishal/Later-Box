package com.laterbox.data.demo

import com.laterbox.data.model.richnote.ListType
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.data.model.richnote.RichDocument
import com.laterbox.data.model.richnote.RichSpan
import com.laterbox.data.model.richnote.SpanType
import com.laterbox.data.model.richnote.TextAlignment
import com.laterbox.data.model.richnote.TextStyleType
import java.io.File
import java.util.UUID

data class DemoRichNoteTemplate(
    val title: String,
    val noteTypeCategory: String,
    val buildDocument: (imagePathMap: Map<String, String>) -> RichDocument,
    val tags: List<String> = emptyList(),
    val playlistName: String = "Reading & Research",
    val daysAgo: Long = 1,
    val isFavorite: Boolean = false
)

object DemoRichNotesFixtures {

    /**
     * Returns exactly 100 Rich Note templates covering the required distribution.
     */
    val templates: List<DemoRichNoteTemplate> = buildList {

        // ==========================================
        // 1. Plain Formatted Documents (10 items)
        // ==========================================
        val plainThemes = listOf(
            Pair("Reflections on Simplicity in Product Design", "Simplicity is not the lack of clutter, that's a consequence of simplicity. Simplicity somehow essentially describes the purpose and place of an object and product."),
            Pair("The Psychology of Waiting and Patience", "Our perception of waiting time is dramatically altered by whether the wait is occupied or unoccupied, unexplained or explained, anxious or calm."),
            Pair("Quiet Architecture: Designing Sanctuaries", "Spaces that allow human attention to recover require intentional acoustics, indirect diffused natural illumination, and tactile natural surfaces."),
            Pair("Notes on Analog Photography Grain", "Silver halide crystals distribute organically across the film emulsion. Unlike digital sensor noise, analog grain possesses continuous micro-texture."),
            Pair("Tea Ceremony Fundamentals: Wabi and Sabi", "Finding aesthetic beauty in imperfection, transience, and the unpretentious patina that natural materials develop over decades."),
            Pair("The Evolution of Writing Instruments", "From Sumerian reed styluses pressing cuneiform into wet river clay to machined titanium fountain pens with gold nibs."),
            Pair("Observations on Mountain Weather Microclimates", "Cold air drainage down alpine valleys creates localized thermal inversions where valley bottoms remain colder than mid-ridge meadows."),
            Pair("Why Slower Cooking Transforms Flavor", "Low, steady conduction breaks down collagen into gelatin over four hours, producing silkiness that high heat pressure cannot replicate."),
            Pair("Principles of Clean Physical Workspaces", "Keep only the active task instrument on the desk surface. Everything else belongs in labeled drawers or closed vertical cabinets."),
            Pair("The Geometry of Vintage Book Typographic Grids", "The Jan Tschichold golden canon of book design dictates that text block margins expand in ratio from inner, top, outer, to bottom.")
        )

        plainThemes.forEachIndexed { i, (docTitle, docBody) ->
            add(
                DemoRichNoteTemplate(
                    title = docTitle,
                    noteTypeCategory = "Plain Formatted",
                    buildDocument = {
                        RichDocument(
                            title = docTitle,
                            blocks = listOf(
                                RichBlock.Text(
                                    id = "p_${i}_1",
                                    text = docBody,
                                    style = TextStyleType.NORMAL,
                                    alignment = TextAlignment.LEFT
                                ),
                                RichBlock.Text(
                                    id = "p_${i}_2",
                                    text = "Documented as part of Later Box contemplative thinking and long-form writing practice.",
                                    style = TextStyleType.NORMAL,
                                    alignment = TextAlignment.LEFT
                                )
                            )
                        )
                    },
                    tags = listOf("plain", "reflection", "notes"),
                    daysAgo = (i * 4L + 2)
                )
            )
        }

        // ==========================================
        // 2. Heading-Rich Documents (10 items)
        // ==========================================
        val headingThemes = listOf(
            "Android Architecture Blueprint 2026",
            "Quarterly Product Strategy and Vision",
            "Home Studio Acoustic Engineering Spec",
            "Specialty Coffee Roasting Curve Analysis",
            "Wilderness Backpacking Expedition Plan",
            "Mobile Design System Governance Model",
            "Data Pipeline Schema and Lakehouse Spec",
            "Ergonomic Hardware Evaluation Guide",
            "Sustainable Building Materials Review",
            "Personal Knowledge Management Manual"
        )

        headingThemes.forEachIndexed { i, hTitle ->
            add(
                DemoRichNoteTemplate(
                    title = hTitle,
                    noteTypeCategory = "Heading-Rich",
                    buildDocument = {
                        RichDocument(
                            title = hTitle,
                            blocks = listOf(
                                RichBlock.Text(id = "h_${i}_1", text = "Executive Overview", style = TextStyleType.HEADING_1),
                                RichBlock.Text(id = "h_${i}_2", text = "This document establishes foundational requirements and key performance objectives for $hTitle."),
                                RichBlock.Text(id = "h_${i}_3", text = "Key Architecture Principles", style = TextStyleType.HEADING_2),
                                RichBlock.Text(id = "h_${i}_4", text = "1. Maintain strict modular separation between components.\n2. Ensure zero runtime external cloud telemetry.\n3. Validate every operational state against offline network boundaries."),
                                RichBlock.Text(id = "h_${i}_5", text = "Implementation Roadmap", style = TextStyleType.HEADING_2),
                                RichBlock.Text(id = "h_${i}_6", text = "Deployment follows phased validation across unit tests, regression suites, and device verification."),
                                RichBlock.Text(id = "h_${i}_7", text = "Operational Verification", style = TextStyleType.HEADING_1),
                                RichBlock.Text(id = "h_${i}_8", text = "Continuous audit metrics confirm compliance with performance budgets and reliability criteria.")
                            )
                        )
                    },
                    tags = listOf("headings", "architecture", "spec"),
                    playlistName = "Projects & Work",
                    daysAgo = (i * 3L + 1),
                    isFavorite = i % 3 == 0
                )
            )
        }

        // ==========================================
        // 3. Mixed Bold/Italic/Underline/Strikethrough Documents (10 items)
        // ==========================================
        val spanThemes = listOf(
            "Code Review Feedback and Corrections",
            "Nutritional Macros and Dietary Guidelines",
            "Typography Style Guide and Rules",
            "Trail Navigation and Safety Protocol",
            "Audio Mixing and EQ Checklist",
            "Editorial Voice and Copy Guidelines",
            "Investment Thesis and Risk Assessment",
            "Sprint Retrospective Action Items",
            "Travel Packing Rules and Restrictions",
            "Kitchen Mise en Place Essentials"
        )

        spanThemes.forEachIndexed { i, sTitle ->
            add(
                DemoRichNoteTemplate(
                    title = sTitle,
                    noteTypeCategory = "Mixed Spans",
                    buildDocument = {
                        val text1 = "CRITICAL REQUIREMENT: All functions must be pure, deterministic, and fully documented."
                        val spans1 = listOf(
                            RichSpan(type = SpanType.BOLD, start = 0, end = 21),
                            RichSpan(type = SpanType.UNDERLINE, start = 42, end = 46),
                            RichSpan(type = SpanType.ITALIC, start = 48, end = 61)
                        )

                        val text2 = "Old approach: Poll server every 5 seconds. Modern approach: Observe local Flow reactive state."
                        val spans2 = listOf(
                            RichSpan(type = SpanType.STRIKETHROUGH, start = 0, end = 42),
                            RichSpan(type = SpanType.BOLD, start = 43, end = 58),
                            RichSpan(type = SpanType.UNDERLINE, start = 68, end = 86)
                        )

                        RichDocument(
                            title = sTitle,
                            blocks = listOf(
                                RichBlock.Text(id = "ms_${i}_1", text = text1, spans = spans1),
                                RichBlock.Text(id = "ms_${i}_2", text = text2, spans = spans2),
                                RichBlock.Text(
                                    id = "ms_${i}_3",
                                    text = "Always prefer standard library functions over third-party convenience wrappers.",
                                    spans = listOf(RichSpan(type = SpanType.BOLD, start = 14, end = 39))
                                )
                            )
                        )
                    },
                    tags = listOf("formatting", "spans", "style"),
                    daysAgo = (i * 5L + 2)
                )
            )
        }

        // ==========================================
        // 4. Bulleted List Documents (10 items)
        // ==========================================
        val bulletThemes = listOf(
            Pair("Brainstorming: Later Box Feature Roadmap", listOf("Semantic embeddings for offline search", "Tag clustering and auto-categorization", "Custom playlist cover art generator", "Home widget with random inspirational memory", "Quick capture floating bubble")),
            Pair("Core Values in Software Craftsmanship", listOf("Radical simplicity over clever abstractions", "Respect user attention and device battery", "Zero dark patterns and zero telemetry", "Reliable data persistence with automatic backups")),
            Pair("Home Barista Equipment Essentials", listOf("Conical burr grinder with single-dose bellows", "Precision 0.1g digital scale with timer", "PID temperature-controlled gooseneck kettle", "Ceramic V60 dripper and unbleached paper filters")),
            Pair("Camera Bag Everyday Carry", listOf("Mirrorless full-frame camera body", "35mm f/1.8 compact prime lens", "Two spare high-capacity batteries", "Circular polarizing filter and lens pen")),
            Pair("Hiking Safety Guidelines", listOf("Check summit weather forecast before trailhead departure", "Pack 2.5 liters of water and electrolyte tablets", "Carry physical topographic map and compass", "Inform emergency contact of intended return time")),
            Pair("Key Principles of Minimalism", listOf("Possessions must serve clear function or deep joy", "Empty space is not wasted space", "One high-quality item outlasts ten cheap alternatives", "Cluttered rooms produce cluttered thoughts")),
            Pair("Sprint Planning Meeting Agenda", listOf("Review previous sprint completion metrics", "Calibrate team velocity and upcoming PTO", "Score user stories by user impact and complexity", "Commit to achievable sprint sprint backlog")),
            Pair("Reading Recommendations on Design", listOf("The Design of Everyday Things by Don Norman", "Grid Systems in Graphic Design by Josef Muller-Brockmann", "Less and More: The Design Ethos of Dieter Rams", "Universal Principles of Design by William Lidwell")),
            Pair("Indoor Air Quality Improvement Tips", listOf("Open cross-ventilation windows for 15 minutes twice daily", "Deploy HEPA filtration unit in bedroom and workspace", "Avoid synthetic fragrance sprays and paraffin candles", "Maintain indoor relative humidity between 40% and 55%")),
            Pair("Habits for Sustained Daily Focus", listOf("Complete the single highest priority task before checking email", "Keep phone in another room during morning deep work", "Take 5-minute outdoor walking breaks between sessions", "Shut down screens at least 60 minutes before bedtime"))
        )

        bulletThemes.forEachIndexed { i, (bTitle, bItems) ->
            add(
                DemoRichNoteTemplate(
                    title = bTitle,
                    noteTypeCategory = "Bulleted Lists",
                    buildDocument = {
                        val blocks = mutableListOf<RichBlock>()
                        blocks.add(RichBlock.Text(id = "b_${i}_intro", text = "Key items to consider and prioritize:"))
                        bItems.forEachIndexed { bi, bText ->
                            blocks.add(
                                RichBlock.Text(
                                    id = "b_${i}_$bi",
                                    text = bText,
                                    listType = ListType.BULLET
                                )
                            )
                        }
                        RichDocument(title = bTitle, blocks = blocks)
                    },
                    tags = listOf("bullets", "lists"),
                    playlistName = "Projects & Work",
                    daysAgo = (i * 4L + 1)
                )
            )
        }

        // ==========================================
        // 5. Numbered List Documents (10 items)
        // ==========================================
        val numberedThemes = listOf(
            Pair("V60 Pourover Coffee Protocol", listOf("Rinse paper filter with boiling water and discard rinse water", "Add 18g medium-fine coffee grounds and create shallow center well", "Pour 50g water at 94°C for 45-second bloom", "Pour steadily in outward spirals up to 200g by 1:30", "Finish pour to 300g total and draw down by 2:45")),
            Pair("Clean Android Build Environment Setup", listOf("Install latest Android Studio Arctic or Ladybug release", "Configure JDK 17 as default Gradle daemon runtime", "Enable parallel builds and Gradle build cache in gradle.properties", "Sync project with Gradle files and verify zero lint errors", "Execute testDebugUnitTest across all modules")),
            Pair("Morning Routine for Deep Work", listOf("Wake at 6:30 AM without snooze alarm", "Drink 500ml cold water with pinch of sea salt", "Engage in 10 minutes of light mobility stretching", "Prepare single-origin pour over coffee", "Commence 90-minute uninterrupted writing sprint")),
            Pair("Sourdough Bread Baking Sequence", listOf("Feed starter 1:1:1 ratio 4 hours before mixing", "Autolyse flour and 80% water for 45 minutes", "Incorporate starter and 2% fine sea salt", "Perform 4 stretch-and-folds spaced 30 minutes apart", "Cold retard shaped boule in banneton overnight at 4°C", "Bake in preheated Dutch oven at 240°C with lid on for 20 mins", "Remove lid and bake at 210°C for 22 mins until blistered mahogany")),
            Pair("Database Migration Verification Steps", listOf("Define new schema version and alter table scripts", "Implement deterministic Migration object in Room database", "Author unit test applying migration from version N-1 to N", "Verify all indices and default values match production schema", "Deploy debug build to physical test device and verify startup")),
            Pair("Emergency Roadside Tire Change", listOf("Park on flat, level surface and engage emergency parking brake", "Retrieve spare tire, lug wrench, and scissor jack from trunk", "Loosen lug nuts half-turn while tire is still on ground", "Position jack under frame jack-point and raise vehicle", "Remove lug nuts, replace wheel, hand-tighten nuts in star pattern", "Lower vehicle to ground and torque lug nuts to factory spec")),
            Pair("Podcast Episode Production Sequence", listOf("Outline conversation topics and research guest background", "Record multi-track audio with 24-bit 48kHz WAV resolution", "Perform spectral noise reduction and voice level matching", "Edit conversational pauses and cross-talk artifacts", "Master output to -16 LUFS integrated loudness for distribution")),
            Pair("Product Feature Release Checklist", listOf("Freeze feature branch and run complete regression test suite", "Update application versionCode and versionName in build.gradle.kts", "Draft comprehensive release notes with checksums and highlights", "Tag git release with signed tag and push to remote origin", "Verify automated GitHub Actions release asset upload")),
            Pair("Handmade Ceramic Mug Workflow", listOf("Weigh 450g stoneware clay and wedge thoroughly to remove air pockets", "Center clay on pottery wheel at high wheel speed", "Open and pull cylinder walls in 3 controlled lifting passes", "Shape belly curvature and compress lip with leather chamois", "Dry to leather-hard stage and trim foot ring on wheel", "Attach pulled handle with slip and score technique", "Bisque fire to cone 04, glaze, and glaze fire to cone 6")),
            Pair("End-of-Day Shutdown Ritual", listOf("Clear physical desk surface of coffee cups and scrap papers", "Review completed tasks and celebrate daily progress", "Choose exactly three critical objectives for tomorrow morning", "Close all browser tabs unrelated to tomorrow's first task", "Close laptop screen and transition into personal evening mode"))
        )

        numberedThemes.forEachIndexed { i, (nTitle, nSteps) ->
            add(
                DemoRichNoteTemplate(
                    title = nTitle,
                    noteTypeCategory = "Numbered Lists",
                    buildDocument = {
                        val blocks = mutableListOf<RichBlock>()
                        blocks.add(RichBlock.Text(id = "n_${i}_intro", text = "Follow these sequential steps in order:"))
                        nSteps.forEachIndexed { si, stepText ->
                            blocks.add(
                                RichBlock.Text(
                                    id = "n_${i}_$si",
                                    text = stepText,
                                    listType = ListType.NUMBERED,
                                    listIndex = si + 1
                                )
                            )
                        }
                        RichDocument(title = nTitle, blocks = blocks)
                    },
                    tags = listOf("numbered", "guide", "steps"),
                    daysAgo = (i * 6L + 1)
                )
            )
        }

        // ==========================================
        // 6. Interactive Checklist Documents (15 items)
        // ==========================================
        val checklistThemes = listOf(
            Pair("Pre-Flight International Travel Checklist", listOf(Pair("Valid passport with 6+ months validity", true), Pair("Universal travel adapter & 65W GaN brick", true), Pair("Download offline Google Maps of destination", true), Pair("Prescription medications & compact first-aid kit", false), Pair("Noise-cancelling headphones charged to 100%", true), Pair("Credit cards notified for international transactions", false), Pair("Print physical flight confirmation and boarding passes", false))),
            Pair("Weekly Home Cleaning and Organizing", listOf(Pair("Launder bed linens and towels at 60°C", true), Pair("Vacuum living room and bedroom hardwood floors", true), Pair("Clean kitchen counters and sanitize cutting boards", true), Pair("Empty all room wastebaskets and recycling bins", false), Pair("Water indoor houseplants and mist tropical foliage", true), Pair("Wipe down bathroom mirror and sink basin", false))),
            Pair("Camping and Wilderness Backpacking Gear", listOf(Pair("Ultralight 2-person freestanding tent", true), Pair("Down sleeping bag rated to -5°C", true), Pair("Inflatable insulated sleeping pad (R-value > 4)", true), Pair("Compact canister stove & titanium cooking pot", true), Pair("Water filter pump and chlorine dioxide tablets", false), Pair("Headlamp with spare rechargeable batteries", true), Pair("Bear canister for food storage", false))),
            Pair("Software Production Deployment Checklist", listOf(Pair("Merge approved pull request into main branch", true), Pair("Execute complete automated test suite (100% pass)", true), Pair("Verify Room database migration scripts", true), Pair("Generate signed release APK and calculate SHA-256", true), Pair("Verify release APK installation on test physical device", false), Pair("Publish GitHub Release with release notes", false))),
            Pair("Farmers Market Weekend Grocery List", listOf(Pair("Organic sourdough country loaf", true), Pair("Heirloom cherry tomatoes (2 pints)", true), Pair("Locally roasted whole bean coffee", true), Pair("Farm fresh pastured eggs (2 dozen)", false), Pair("Raw wildflower honey jar", false), Pair("Fresh bunch of Tuscan kale and cilantro", true), Pair("Artisan goat cheese medallion", false))),
            Pair("Apartment Moving Out Checklist", listOf(Pair("Patch all wall picture nail holes with spackle", true), Pair("Clean inside oven and refrigerator shelves", true), Pair("Return physical apartment and mailbox keys", false), Pair("Submit forwarding address request to postal service", true), Pair("Transfer electric and internet utilities to new address", false), Pair("Take timestamped departure photos of all rooms", false))),
            Pair("Photography Outdoor Shoot Preparation", listOf(Pair("Format high-speed SD cards in camera body", true), Pair("Clean sensor and lens front elements with blower", true), Pair("Charge primary and secondary camera batteries", true), Pair("Pack sturdy carbon-fiber travel tripod", true), Pair("Pack ND filters (6-stop and 10-stop)", false), Pair("Check golden hour weather and cloud coverage forecast", false))),
            Pair("Car Winter Preparation Inspection", listOf(Pair("Inspect winter tire tread depth (> 4mm)", true), Pair("Refill windshield washer fluid with -30°C rating", true), Pair("Test 12V car battery cold cranking voltage", true), Pair("Stow emergency snow shovel and jumper cables in trunk", false), Pair("Pack warm thermal blanket and flashlight in glove box", false), Pair("Verify engine coolant freezing point protection", true))),
            Pair("Daily Productivity and Health Habits", listOf(Pair("Drink 500ml water immediately upon waking", true), Pair("Engage in 20 minutes of aerobic exercise or brisk walk", true), Pair("Complete primary 90-minute deep work focus block", true), Pair("Take healthy lunch away from computer screens", true), Pair("Review and triage Later Box inbox items", false), Pair("Perform evening reflection and digital shutdown", false))),
            Pair("Home Office Ergonomics Audit", listOf(Pair("Top of monitor aligned with eye level", true), Pair("Elbows at 90-degree angle resting on armrests", true), Pair("Feet resting flat on the floor or footrest", true), Pair("Lower back lumbar support positioned at curvature", true), Pair("Keyboard and mouse within easy reach without stretching", false), Pair("Room illumination balanced to avoid screen glare", false))),
            Pair("Bicycle Spring Tune-Up Inspection", listOf(Pair("Inflate tires to recommended PSI pressure", true), Pair("Inspect brake pads for wear and alignment", true), Pair("Clean and degrease chain and cassette", true), Pair("Apply dry PTFE lubricant to chain links", false), Pair("Verify smooth shifting across all gear ratios", false), Pair("Check handlebar and stem bolt torque specs", true))),
            Pair("Conference Speaker Travel Prep", listOf(Pair("Test presentation slides on offline laptop", true), Pair("Export PDF backup of slide deck to thumb drive", true), Pair("Pack USB-C to HDMI adapter and presentation clicker", true), Pair("Rehearse speech timing with stopwatch", false), Pair("Pack business cards and notebook with pen", false), Pair("Confirm hotel reservation and check-in time", true))),
            Pair("Cast Iron Pan Restoration Sequence", listOf(Pair("Scrub rust spots with steel wool and vinegar", true), Pair("Wash thoroughly with warm water and dry immediately", true), Pair("Apply ultra-thin layer of grapeseed oil over entire surface", true), Pair("Place upside down in oven preheated to 230°C", false), Pair("Bake for one hour and allow to cool inside oven", false), Pair("Repeat oiling and baking cycle 3 times for slick seasoning", false))),
            Pair("Personal Finance Monthly Audit", listOf(Pair("Reconcile credit card statement against receipts", true), Pair("Verify automatic monthly retirement contributions", true), Pair("Review recurring subscriptions and cancel unused services", true), Pair("Check high-yield savings account interest payment", false), Pair("Update net worth and budget spreadsheet tracker", false))),
            Pair("Emergency Preparedness Home Kit", listOf(Pair("Gallon of drinking water per person for 3 days", true), Pair("Non-perishable canned food and manual can opener", true), Pair("Hand-crank emergency weather radio", true), Pair("First-aid kit with antiseptic and bandages", true), Pair("Waterproof matches and emergency space blankets", false), Pair("Multi-tool with pliers, knife, and screwdrivers", false)))
        )

        checklistThemes.forEachIndexed { i, (cTitle, cItems) ->
            add(
                DemoRichNoteTemplate(
                    title = cTitle,
                    noteTypeCategory = "Interactive Checklists",
                    buildDocument = {
                        val blocks = mutableListOf<RichBlock>()
                        blocks.add(RichBlock.Text(id = "c_${i}_intro", text = "Interactive checklist for $cTitle. Tap checkboxes to toggle state:"))
                        cItems.forEachIndexed { ci, (itemText, isDone) ->
                            blocks.add(
                                RichBlock.Text(
                                    id = "c_${i}_$ci",
                                    text = itemText,
                                    listType = ListType.CHECKLIST,
                                    isChecked = isDone
                                )
                            )
                        }
                        RichDocument(title = cTitle, blocks = blocks)
                    },
                    tags = listOf("checklist", "tasks", "actionable"),
                    daysAgo = (i * 3L + 1),
                    isFavorite = i % 4 == 0
                )
            )
        }

        // ==========================================
        // 7. Image-Rich Documents (15 items)
        // ==========================================
        for (i in 0 until 15) {
            val spec = DemoImageFixtureGenerator.FixtureSpec(
                fileName = when (i % 15) {
                    0 -> "demo_img_architecture.jpg"
                    1 -> "demo_img_mountain_sunset.jpg"
                    2 -> "demo_img_ui_wireframe.jpg"
                    3 -> "demo_img_audio_studio.jpg"
                    4 -> "demo_img_coffee_recipe.jpg"
                    5 -> "demo_img_workspace_setup.jpg"
                    6 -> "demo_img_data_pipeline.jpg"
                    7 -> "demo_img_typography_specimen.jpg"
                    8 -> "demo_img_kyoto_travel.jpg"
                    9 -> "demo_img_plant_care.jpg"
                    10 -> "demo_img_camera_rig.jpg"
                    11 -> "demo_img_running_shoes.jpg"
                    12 -> "demo_img_book_collection.jpg"
                    13 -> "demo_img_circuit_board.jpg"
                    else -> "demo_img_sourdough_bake.jpg"
                },
                title = "Visual Reference $i",
                subtitle = "Sub",
                width = 800,
                height = 600,
                startColor = 0,
                endColor = 0,
                accentColor = 0
            )

            val noteTitle = "Visual Study: ${spec.fileName.removePrefix("demo_img_").removeSuffix(".jpg").replace('_', ' ').replaceFirstChar { it.uppercase() }}"

            add(
                DemoRichNoteTemplate(
                    title = noteTitle,
                    noteTypeCategory = "Image-Rich",
                    buildDocument = { imageMap ->
                        val localPath = imageMap[spec.fileName] ?: "/sdcard/media/${spec.fileName}"
                        RichDocument(
                            title = noteTitle,
                            blocks = listOf(
                                RichBlock.Text(
                                    id = "img_${i}_1",
                                    text = "This study documents visual form, proportions, and light interactions for creative design work."
                                ),
                                RichBlock.Image(
                                    id = "img_${i}_media",
                                    localPath = localPath,
                                    fileName = spec.fileName,
                                    caption = "Figure 1: High-resolution visual fixture for ${spec.fileName}."
                                ),
                                RichBlock.Text(
                                    id = "img_${i}_2",
                                    text = "Notice the balance between negative space and core focal elements. Inline images in Later Box are preserved across backup and restore cycles."
                                )
                            )
                        )
                    },
                    tags = listOf("image", "visual", "gallery"),
                    playlistName = "Travel & Inspiration",
                    daysAgo = (i * 4L + 2),
                    isFavorite = i % 3 == 0
                )
            )
        }

        // ==========================================
        // 8. Mixed Text and Multiple Images (5 items)
        // ==========================================
        val multiImageThemes = listOf(
            Triple("Architectural Form & Alpine Topography", "demo_img_architecture.jpg", "demo_img_mountain_sunset.jpg"),
            Triple("Design System UI & Typography Guidelines", "demo_img_ui_wireframe.jpg", "demo_img_typography_specimen.jpg"),
            Triple("Workspace Ergonomics & Studio Acoustics", "demo_img_workspace_setup.jpg", "demo_img_audio_studio.jpg"),
            Triple("Kyoto Travel Memories & Botanical Gardens", "demo_img_kyoto_travel.jpg", "demo_img_plant_care.jpg"),
            Triple("Artisan Sourdough & Specialty Coffee Routine", "demo_img_sourdough_bake.jpg", "demo_img_coffee_recipe.jpg")
        )

        multiImageThemes.forEachIndexed { i, (mTitle, img1, img2) ->
            add(
                DemoRichNoteTemplate(
                    title = mTitle,
                    noteTypeCategory = "Multiple Images",
                    buildDocument = { imageMap ->
                        val p1 = imageMap[img1] ?: "/sdcard/media/$img1"
                        val p2 = imageMap[img2] ?: "/sdcard/media/$img2"
                        RichDocument(
                            title = mTitle,
                            blocks = listOf(
                                RichBlock.Text(id = "mi_${i}_1", text = "Comparative exploration of paired aesthetic elements.", style = TextStyleType.HEADING_2),
                                RichBlock.Text(id = "mi_${i}_2", text = "First study focuses on structural and primary geometry:"),
                                RichBlock.Image(id = "mi_${i}_img1", localPath = p1, fileName = img1, caption = "Exhibit A: Primary structural subject ($img1)"),
                                RichBlock.Text(id = "mi_${i}_3", text = "Second study contrasts this with organic textures and natural lighting:"),
                                RichBlock.Image(id = "mi_${i}_img2", localPath = p2, fileName = img2, caption = "Exhibit B: Complementary organic study ($img2)"),
                                RichBlock.Text(id = "mi_${i}_4", text = "Both fixtures render smoothly inline with asynchronous Coil caching.")
                            )
                        )
                    },
                    tags = listOf("multi-image", "comparison", "design"),
                    playlistName = "Travel & Inspiration",
                    daysAgo = (i * 7L + 3),
                    isFavorite = true
                )
            )
        }

        // ==========================================
        // 9. Hyperlink-Rich Documents (5 items)
        // ==========================================
        val linkThemes = listOf(
            "Curated Android Development References",
            "Specialty Coffee Roaster Recommendations",
            "Essential Typography and Design Resources",
            "High-Altitude Alpine Trekking Portals",
            "Recommended Open Source Software Projects"
        )

        linkThemes.forEachIndexed { i, lTitle ->
            add(
                DemoRichNoteTemplate(
                    title = lTitle,
                    noteTypeCategory = "Hyperlink-Rich",
                    buildDocument = {
                        val t1 = "Explore the official Android Developers Portal for API documentation and sample guides."
                        val s1 = listOf(RichSpan(type = SpanType.LINK, start = 21, end = 46, linkUrl = "https://developer.android.example"))

                        val t2 = "Check out Material Design 3 Guidelines for color tokens, typography scales, and shape tokens."
                        val s2 = listOf(RichSpan(type = SpanType.LINK, start = 10, end = 38, linkUrl = "https://m3.material.example"))

                        val t3 = "For source code and issue tracking, review the official Later Box GitHub Repository."
                        val s3 = listOf(RichSpan(type = SpanType.LINK, start = 56, end = 83, linkUrl = "https://github.example/arybishal/Later-Box"))

                        RichDocument(
                            title = lTitle,
                            blocks = listOf(
                                RichBlock.Text(id = "link_${i}_intro", text = "Curated bookmarks and clickable external references:"),
                                RichBlock.Text(id = "link_${i}_1", text = t1, spans = s1, listType = ListType.BULLET),
                                RichBlock.Text(id = "link_${i}_2", text = t2, spans = s2, listType = ListType.BULLET),
                                RichBlock.Text(id = "link_${i}_3", text = t3, spans = s3, listType = ListType.BULLET),
                                RichBlock.Text(id = "link_${i}_note", text = "Hyperlinks are fully interactive in reading mode and open in your default browser.")
                            )
                        )
                    },
                    tags = listOf("hyperlinks", "bookmarks", "curation"),
                    playlistName = "Reading & Research",
                    daysAgo = (i * 6L + 2)
                )
            )
        }

        // ==========================================
        // 10. Long-Form Documents (5 items)
        // ==========================================
        val longThemes = listOf(
            "Comprehensive Guide to Building Offline-First Mobile Apps",
            "The History and Evolution of Typography in Print & Digital",
            "Modern Sourdough Bread Science: From Starter to Crust",
            "The Philosophy of Mindful Attention in the Digital Era",
            "Endurance Running Physiology: Aerobic Base Building"
        )

        longThemes.forEachIndexed { i, lfTitle ->
            add(
                DemoRichNoteTemplate(
                    title = lfTitle,
                    noteTypeCategory = "Long-Form",
                    buildDocument = {
                        val blocks = mutableListOf<RichBlock>()
                        blocks.add(RichBlock.Text(id = "lf_${i}_h1", text = "1. Introduction & Theoretical Foundations", style = TextStyleType.HEADING_1))
                        blocks.add(RichBlock.Text(id = "lf_${i}_p1", text = "To understand modern systems, we must first trace how historical constraints shaped our contemporary paradigms. When resources are constrained, elegance emerges not from abundance, but from thoughtful omission."))
                        blocks.add(RichBlock.Text(id = "lf_${i}_p2", text = "Human attention, device battery life, and database I/O are all finite commodities that require disciplined stewardship."))

                        blocks.add(RichBlock.Text(id = "lf_${i}_h2", text = "2. Core Structural Principles", style = TextStyleType.HEADING_1))
                        blocks.add(RichBlock.Text(id = "lf_${i}_sub1", text = "A. Unidirectional Data Flow", style = TextStyleType.HEADING_2))
                        blocks.add(RichBlock.Text(id = "lf_${i}_p3", text = "State flows downward through immutable models. Events and user intents flow upward through well-defined handlers."))
                        blocks.add(RichBlock.Text(id = "lf_${i}_sub2", text = "B. Local Storage as Single Source of Truth", style = TextStyleType.HEADING_2))
                        blocks.add(RichBlock.Text(id = "lf_${i}_p4", text = "Network is an asynchronous synchronization transport, never the primary state store. When the app opens, UI renders instantly from local SQLite disk."))

                        blocks.add(RichBlock.Text(id = "lf_${i}_h3", text = "3. Implementation Protocols", style = TextStyleType.HEADING_1))
                        blocks.add(RichBlock.Text(id = "lf_${i}_step1", text = "Establish normalized database tables with cascade rules", listType = ListType.NUMBERED, listIndex = 1))
                        blocks.add(RichBlock.Text(id = "lf_${i}_step2", text = "Expose query flows with distinctUntilChanged()", listType = ListType.NUMBERED, listIndex = 2))
                        blocks.add(RichBlock.Text(id = "lf_${i}_step3", text = "Isolate heavy background compute on Dispatchers.IO", listType = ListType.NUMBERED, listIndex = 3))
                        blocks.add(RichBlock.Text(id = "lf_${i}_step4", text = "Implement zip-based lossless backup archives", listType = ListType.NUMBERED, listIndex = 4))

                        blocks.add(RichBlock.Text(id = "lf_${i}_h4", text = "4. Verification Criteria", style = TextStyleType.HEADING_1))
                        blocks.add(RichBlock.Text(id = "lf_${i}_chk1", text = "Cold app launch under 400 milliseconds", listType = ListType.CHECKLIST, isChecked = true))
                        blocks.add(RichBlock.Text(id = "lf_${i}_chk2", text = "Zero database crashes during rapid scrolling", listType = ListType.CHECKLIST, isChecked = true))
                        blocks.add(RichBlock.Text(id = "lf_${i}_chk3", text = "Backup export survives complete device factory reset", listType = ListType.CHECKLIST, isChecked = true))
                        blocks.add(RichBlock.Text(id = "lf_${i}_chk4", text = "Zero memory leaks in background coroutine scopes", listType = ListType.CHECKLIST, isChecked = false))

                        blocks.add(RichBlock.Text(id = "lf_${i}_concl", text = "5. Conclusion", style = TextStyleType.HEADING_1))
                        blocks.add(RichBlock.Text(id = "lf_${i}_concl_p", text = "By prioritizing craftsmanship, local-first data ownership, and clean typography, we build software that respects both the user and the craft."))

                        RichDocument(title = lfTitle, blocks = blocks)
                    },
                    tags = listOf("long-form", "guide", "deep-dive"),
                    playlistName = "Reading & Research",
                    daysAgo = (i * 8L + 4),
                    isFavorite = true
                )
            )
        }

        // ==========================================
        // 11. Complex Mixed-Format Documents (5 items)
        // ==========================================
        val complexThemes = listOf(
            Triple("Product Design Manifesto & Architectural Sprint", "demo_img_architecture.jpg", "https://laterbox.example/manifesto"),
            Triple("Specialty Pour-Over Coffee Lab Protocol", "demo_img_coffee_recipe.jpg", "https://coffee.example/v60-ratio"),
            Triple("Ergonomic Mobile Workstation Masterplan", "demo_img_workspace_setup.jpg", "https://ergonomics.example/dual-monitor"),
            Triple("Kyoto Cultural Heritage Exploration Journal", "demo_img_kyoto_travel.jpg", "https://japan.example/kyoto-trails"),
            Triple("Wild Fermentation Sourdough Baking Diary", "demo_img_sourdough_bake.jpg", "https://sourdough.example/hydration")
        )

        complexThemes.forEachIndexed { i, (cTitle, cImg, cUrl) ->
            add(
                DemoRichNoteTemplate(
                    title = cTitle,
                    noteTypeCategory = "Complex Mixed",
                    buildDocument = { imageMap ->
                        val imgPath = imageMap[cImg] ?: "/sdcard/media/$cImg"
                        val blocks = mutableListOf<RichBlock>()

                        // Title / Header
                        blocks.add(RichBlock.Text(id = "cm_${i}_h1", text = "Master Blueprint: $cTitle", style = TextStyleType.HEADING_1))

                        // Styled intro paragraph with mixed bold & italic
                        val introText = "This master document synthesizes design principles, actionable protocols, visual references, and verification criteria."
                        val introSpans = listOf(
                            RichSpan(type = SpanType.BOLD, start = 5, end = 20),
                            RichSpan(type = SpanType.ITALIC, start = 32, end = 49)
                        )
                        blocks.add(RichBlock.Text(id = "cm_${i}_p1", text = introText, spans = introSpans))

                        // Quote block centered
                        blocks.add(
                            RichBlock.Text(
                                id = "cm_${i}_quote",
                                text = "“Form follows function, but elegance endures across generations.”",
                                spans = listOf(RichSpan(type = SpanType.ITALIC, start = 0, end = 66)),
                                alignment = TextAlignment.CENTER
                            )
                        )

                        // Inline Image with caption
                        blocks.add(
                            RichBlock.Image(
                                id = "cm_${i}_media",
                                localPath = imgPath,
                                fileName = cImg,
                                caption = "Visual Specimen: Primary photographic evidence for $cTitle."
                            )
                        )

                        // Subheading
                        blocks.add(RichBlock.Text(id = "cm_${i}_h2", text = "Sequential Implementation Steps", style = TextStyleType.HEADING_2))
                        blocks.add(RichBlock.Text(id = "cm_${i}_s1", text = "Establish base parameters and calibrate equipment", listType = ListType.NUMBERED, listIndex = 1))
                        blocks.add(RichBlock.Text(id = "cm_${i}_s2", text = "Execute phase one testing under controlled conditions", listType = ListType.NUMBERED, listIndex = 2))
                        blocks.add(RichBlock.Text(id = "cm_${i}_s3", text = "Document variance and adjust tolerance bounds", listType = ListType.NUMBERED, listIndex = 3))

                        // Checklist
                        blocks.add(RichBlock.Text(id = "cm_${i}_h3", text = "Readiness & Verification Checklist", style = TextStyleType.HEADING_2))
                        blocks.add(RichBlock.Text(id = "cm_${i}_chk1", text = "Pre-flight calibration verified", listType = ListType.CHECKLIST, isChecked = true))
                        blocks.add(RichBlock.Text(id = "cm_${i}_chk2", text = "Equipment temperature stabilized", listType = ListType.CHECKLIST, isChecked = true))
                        blocks.add(RichBlock.Text(id = "cm_${i}_chk3", text = "Final audit review signed off", listType = ListType.CHECKLIST, isChecked = false))

                        // External Link
                        val linkText = "For additional reference material, consult the external technical specification."
                        val linkSpans = listOf(RichSpan(type = SpanType.LINK, start = 47, end = 79, linkUrl = cUrl))
                        blocks.add(RichBlock.Text(id = "cm_${i}_link", text = linkText, spans = linkSpans))

                        RichDocument(title = cTitle, blocks = blocks)
                    },
                    tags = listOf("complex", "master", "kitchen-sink"),
                    playlistName = "Projects & Work",
                    daysAgo = (i * 5L + 1),
                    isFavorite = true
                )
            )
        }
    }
}
