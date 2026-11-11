package com.laterbox.data.demo

import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.richnote.ListType
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.data.model.richnote.RichDocument
import com.laterbox.data.model.richnote.SpanType
import com.laterbox.data.model.richnote.TextStyleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DemoDataLabTest {

    @Test
    fun testGeneralDatasetExactCountAndDistribution() {
        val items = DemoGeneralFixtures.items
        assertEquals(100, items.size)

        // 1. Saved webpages & articles: 20 items (15 articles + 5 web links)
        val articlesAndWebpages = items.filter {
            (it.contentType == ContentType.ARTICLE || (it.contentType == ContentType.LINK && it.sourceDomain == "example.com")) &&
                    it.sourceApp != "Twitter/X" && it.sourceApp != "Reddit" && it.sourceApp != "LinkedIn" &&
                    it.sourceApp != "Mastodon" && it.sourceApp != "GitHub"
        }
        assertEquals(20, articlesAndWebpages.size)

        // 2. Images & screenshots: 15 items
        val images = items.filter { it.contentType == ContentType.IMAGE }
        assertEquals(15, images.size)

        // 3. Quick text captures: 15 items
        val quickText = items.filter { it.captureMethod == "QUICK_NOTE" }
        assertEquals(15, quickText.size)

        // 4. Voice/Audio records: 10 items
        val audio = items.filter { it.contentType == ContentType.AUDIO }
        assertEquals(10, audio.size)
        audio.forEach {
            assertNotNull(it.transcript)
            assertTrue(it.transcript!!.isNotBlank())
        }

        // 5. Tasks and reminders: 10 items
        val tasks = items.filter { it.reminderTemplate != null && it.reminderTemplate.eventType == "REMINDER" }
        assertEquals(10, tasks.size)

        // 6. Birthday & Anniversary events: 10 items
        val smartEvents = items.filter {
            it.reminderTemplate != null && (it.reminderTemplate.eventType == "BIRTHDAY" || it.reminderTemplate.eventType == "ANNIVERSARY")
        }
        assertEquals(10, smartEvents.size)

        // 7. Videos and media references: 5 items
        val videos = items.filter { it.contentType == ContentType.VIDEO }
        assertEquals(5, videos.size)

        // 8. Research and reference memories: 5 items
        val research = items.filter { it.contentType == ContentType.DOCUMENT }
        assertEquals(5, research.size)

        // 9. Mixed social / shared content: 10 items
        val social = items.filter {
            it.contentType == ContentType.LINK &&
                    (it.sourceApp == "Twitter/X" || it.sourceApp == "Reddit" || it.sourceApp == "LinkedIn" ||
                            it.sourceApp == "Mastodon" || it.sourceApp == "GitHub")
        }
        assertEquals(10, social.size)

        // Total check
        assertEquals(100, articlesAndWebpages.size + images.size + quickText.size + audio.size + tasks.size + smartEvents.size + videos.size + research.size + social.size)
    }

    @Test
    fun testRichNotesDatasetExactCountAndDistribution() {
        val templates = DemoRichNotesFixtures.templates
        assertEquals(100, templates.size)

        val categoryCounts = templates.groupBy { it.noteTypeCategory }.mapValues { it.value.size }

        assertEquals(10, categoryCounts["Plain Formatted"])
        assertEquals(10, categoryCounts["Heading-Rich"])
        assertEquals(10, categoryCounts["Mixed Spans"])
        assertEquals(10, categoryCounts["Bulleted Lists"])
        assertEquals(10, categoryCounts["Numbered Lists"])
        assertEquals(15, categoryCounts["Interactive Checklists"])
        assertEquals(15, categoryCounts["Image-Rich"])
        assertEquals(5, categoryCounts["Multiple Images"])
        assertEquals(5, categoryCounts["Hyperlink-Rich"])
        assertEquals(5, categoryCounts["Long-Form"])
        assertEquals(5, categoryCounts["Complex Mixed"])

        val total = categoryCounts.values.sum()
        assertEquals(100, total)
    }

    @Test
    fun testCombinedDatasetGeneratesExact200Records() {
        val generalCount = DemoGeneralFixtures.items.size
        val richNotesCount = DemoRichNotesFixtures.templates.size
        assertEquals(100, generalCount)
        assertEquals(100, richNotesCount)
        assertEquals(200, generalCount + richNotesCount)
    }

    @Test
    fun testRichDocumentSerializationAndStructure() {
        val dummyImageMap = mapOf(
            "demo_img_architecture.jpg" to "/app/media/demo_img_architecture.jpg",
            "demo_img_coffee_recipe.jpg" to "/app/media/demo_img_coffee_recipe.jpg",
            "demo_img_workspace_setup.jpg" to "/app/media/demo_img_workspace_setup.jpg",
            "demo_img_kyoto_travel.jpg" to "/app/media/demo_img_kyoto_travel.jpg",
            "demo_img_sourdough_bake.jpg" to "/app/media/demo_img_sourdough_bake.jpg"
        )

        for (template in DemoRichNotesFixtures.templates) {
            val doc = template.buildDocument(dummyImageMap)
            assertTrue(doc.title.isNotBlank())
            assertTrue(doc.blocks.isNotEmpty())

            val json = doc.toJson()
            assertTrue(json.startsWith("{") && json.endsWith("}"))

            // Deserialization verification
            val restored = RichDocument.fromJson(json, File("/app/media"))
            assertEquals(doc.title, restored.title)
            assertEquals(doc.blocks.size, restored.blocks.size)
        }
    }

    @Test
    fun testChecklistStatePreservationInRichNotes() {
        val checklistTemplates = DemoRichNotesFixtures.templates.filter { it.noteTypeCategory == "Interactive Checklists" }
        assertEquals(15, checklistTemplates.size)

        for (t in checklistTemplates) {
            val doc = t.buildDocument(emptyMap())
            val checklistBlocks = doc.blocks.filterIsInstance<RichBlock.Text>().filter { it.listType == ListType.CHECKLIST }
            assertTrue("Checklist document must have checklist items", checklistBlocks.isNotEmpty())

            val checkedCount = checklistBlocks.count { it.isChecked }
            val uncheckedCount = checklistBlocks.count { !it.isChecked }
            assertTrue("Checklist document should have both completed and pending items", checkedCount > 0 && uncheckedCount > 0)
        }
    }

    @Test
    fun testInlineImageBlocksInRichNotes() {
        val imageTemplates = DemoRichNotesFixtures.templates.filter {
            it.noteTypeCategory == "Image-Rich" || it.noteTypeCategory == "Multiple Images" || it.noteTypeCategory == "Complex Mixed"
        }
        assertEquals(25, imageTemplates.size) // 15 + 5 + 5

        for (t in imageTemplates) {
            val doc = t.buildDocument(mapOf("demo_img_architecture.jpg" to "/media/arch.jpg"))
            val imageBlocks = doc.blocks.filterIsInstance<RichBlock.Image>()
            assertTrue(imageBlocks.isNotEmpty())
            imageBlocks.forEach { img ->
                assertTrue(img.fileName.isNotBlank())
                assertNotNull(img.caption)
            }
        }
    }

    @Test
    fun testHyperlinksInRichNotes() {
        val linkTemplates = DemoRichNotesFixtures.templates.filter {
            it.noteTypeCategory == "Hyperlink-Rich" || it.noteTypeCategory == "Complex Mixed"
        }
        assertEquals(10, linkTemplates.size) // 5 + 5

        for (t in linkTemplates) {
            val doc = t.buildDocument(emptyMap())
            val textBlocksWithLinks = doc.blocks.filterIsInstance<RichBlock.Text>().filter { block ->
                block.spans.any { it.type == SpanType.LINK }
            }
            assertTrue("Document must have blocks with LINK spans", textBlocksWithLinks.isNotEmpty())
            textBlocksWithLinks.forEach { block ->
                val linkSpans = block.spans.filter { it.type == SpanType.LINK }
                linkSpans.forEach { span ->
                    assertNotNull(span.linkUrl)
                    assertTrue(span.linkUrl!!.startsWith("http://") || span.linkUrl!!.startsWith("https://"))
                }
            }
        }
    }

    @Test
    fun testProvenanceFlagsAndDefaultValues() {
        val realItem = SavedItemEntity(
            url = "https://mywebsite.com/article",
            title = "Real User Article"
        )
        assertFalse("Real user items must have isDemo = false by default", realItem.isDemo)
        assertEquals(null, realItem.demoRecordType)

        val demoGeneralItem = SavedItemEntity(
            url = "https://example.com/demo",
            title = "Synthetic Demo Item",
            isDemo = true,
            demoRecordType = "GENERAL"
        )
        assertTrue(demoGeneralItem.isDemo)
        assertEquals("GENERAL", demoGeneralItem.demoRecordType)

        val demoRichNote = SavedItemEntity(
            url = "note://demo/1",
            title = "Synthetic Rich Note",
            isDemo = true,
            demoRecordType = "RICH_NOTE"
        )
        assertTrue(demoRichNote.isDemo)
        assertEquals("RICH_NOTE", demoRichNote.demoRecordType)
    }

    @Test
    fun testPrecomputedAiAnalysisFixturesCountAndIntegrity() {
        val aiFixtures = DemoGeneralFixtures.items.mapNotNull { it.aiAnalysisTemplate }
        assertEquals(30, aiFixtures.size) // Exactly 30 precomputed fixture set across general items

        val analyzed = aiFixtures.filter { it.status == "ANALYZED" }
        assertTrue(analyzed.isNotEmpty())
        analyzed.forEach { ai ->
            assertTrue(ai.shortDescription.isNotBlank())
            assertTrue(ai.whatItsAbout.isNotBlank())
            assertTrue(ai.category.isNotBlank())
            assertTrue(ai.keyTakeaways.isNotEmpty())
        }
    }

    @Test
    fun testDemoImageFixtureSpecs() {
        val specs = DemoImageFixtureGenerator.DEFAULT_FIXTURE_SPECS
        assertEquals(15, specs.size)

        val names = specs.map { it.fileName }
        assertEquals(15, names.distinct().size)

        specs.forEach { spec ->
            assertTrue(spec.fileName.endsWith(".jpg"))
            assertTrue(spec.width > 0 && spec.height > 0)
            assertTrue(spec.title.isNotBlank())
        }
    }
}
