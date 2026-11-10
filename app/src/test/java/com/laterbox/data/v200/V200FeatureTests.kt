package com.laterbox.data.v200

import com.laterbox.data.ai.SemanticSearchEngine
import com.laterbox.data.local.entity.AiAnalysisEntity
import com.laterbox.data.local.entity.ReminderEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.data.reminder.ReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class V200FeatureTests {

    @Test
    fun `test cosine similarity calculation accurately computes vector alignment`() {
        val vecA = listOf(1.0f, 0.0f, 0.0f)
        val vecB = listOf(1.0f, 0.0f, 0.0f)
        val identicalSim = SemanticSearchEngine.cosineSimilarity(vecA, vecB)
        assertEquals(1.0f, identicalSim, 0.0001f)

        val vecOrthogonal = listOf(0.0f, 1.0f, 0.0f)
        val orthogonalSim = SemanticSearchEngine.cosineSimilarity(vecA, vecOrthogonal)
        assertEquals(0.0f, orthogonalSim, 0.0001f)

        val vecOpposite = listOf(-1.0f, 0.0f, 0.0f)
        val oppositeSim = SemanticSearchEngine.cosineSimilarity(vecA, vecOpposite)
        assertEquals(-1.0f, oppositeSim, 0.0001f)

        val emptyA = emptyList<Float>()
        val emptySim = SemanticSearchEngine.cosineSimilarity(emptyA, vecB)
        assertEquals(0.0f, emptySim, 0.0001f)

        val mismatchedSim = SemanticSearchEngine.cosineSimilarity(listOf(1f, 2f), listOf(1f, 2f, 3f))
        assertEquals(0.0f, mismatchedSim, 0.0001f)
    }

    @Test
    fun `test ReminderScheduler computeNextOccurrence handles recurrence rules accurately`() {
        val baseTime = 1700000000000L // arbitrary fixed timestamp

        // NONE should return same current due time
        val noneNext = ReminderScheduler.computeNextOccurrence(baseTime, "NONE")
        assertEquals(baseTime, noneNext)

        // DAILY should add 1 day
        val cal = Calendar.getInstance().apply { timeInMillis = baseTime }
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val dailyExpected = cal.timeInMillis
        val dailyNext = ReminderScheduler.computeNextOccurrence(baseTime, "DAILY")
        assertEquals(dailyExpected, dailyNext)

        // WEEKLY should add 7 days
        val calWeekly = Calendar.getInstance().apply { timeInMillis = baseTime }
        calWeekly.add(Calendar.WEEK_OF_YEAR, 1)
        val weeklyNext = ReminderScheduler.computeNextOccurrence(baseTime, "WEEKLY")
        assertEquals(calWeekly.timeInMillis, weeklyNext)

        // MONTHLY should add 1 month
        val calMonthly = Calendar.getInstance().apply { timeInMillis = baseTime }
        calMonthly.add(Calendar.MONTH, 1)
        val monthlyNext = ReminderScheduler.computeNextOccurrence(baseTime, "MONTHLY")
        assertEquals(calMonthly.timeInMillis, monthlyNext)

        // YEARLY should add 1 year
        val calYearly = Calendar.getInstance().apply { timeInMillis = baseTime }
        calYearly.add(Calendar.YEAR, 1)
        val yearlyNext = ReminderScheduler.computeNextOccurrence(baseTime, "YEARLY")
        assertEquals(calYearly.timeInMillis, yearlyNext)
    }

    @Test
    fun `test SavedItemEntity v2_0_0 memory model fields preservation`() {
        val item = SavedItemEntity(
            id = 42L,
            url = "https://example.com/memory",
            title = "Original Title",
            description = "Original Description",
            contentType = ContentType.ARTICLE,
            isSnap = true,
            captureMethod = "SNAP_AUDIO",
            aiTitle = "Concise AI Title",
            aiDescription = "Rich AI summary description",
            extractedText = "OCR detected text from screenshot",
            transcript = "Audio transcription text from voice note",
            sourcePackage = "com.google.android.apps.messaging"
        )

        assertTrue(item.isSnap)
        assertEquals("SNAP_AUDIO", item.captureMethod)
        assertEquals("Concise AI Title", item.aiTitle)
        assertEquals("Original Title", item.title) // Original title is preserved
        assertEquals("OCR detected text from screenshot", item.extractedText)
        assertEquals("Audio transcription text from voice note", item.transcript)
        assertEquals("com.google.android.apps.messaging", item.sourcePackage)
    }

    @Test
    fun `test AiAnalysisEntity embeddingVectorJson parsing`() {
        val jsonArray = "[0.1234, -0.5678, 0.9999, 0.0]"
        val entity = AiAnalysisEntity(
            itemId = 100L,
            shortDescription = "Test description",
            whatItsAbout = "Testing embeddings",
            category = "Technology",
            modelUsed = "gemini-2.0-flash",
            embeddingVectorJson = jsonArray
        )

        val vector = entity.getEmbeddingVector()
        assertEquals(4, vector.size)
        assertEquals(0.1234f, vector[0], 0.0001f)
        assertEquals(-0.5678f, vector[1], 0.0001f)
        assertEquals(0.9999f, vector[2], 0.0001f)
        assertEquals(0.0f, vector[3], 0.0001f)

        // Empty / Null embedding test
        val emptyEntity = entity.copy(embeddingVectorJson = null)
        assertTrue(emptyEntity.getEmbeddingVector().isEmpty())

        val malformedEntity = entity.copy(embeddingVectorJson = "invalid json")
        assertTrue(malformedEntity.getEmbeddingVector().isEmpty())
    }

    @Test
    fun `test ReminderEntity event type and notification formatting`() {
        val reminder = ReminderEntity(
            id = 1L,
            contentId = 42L,
            dueAt = 1750000000000L,
            eventTitle = "Alice's 30th Birthday",
            eventType = "BIRTHDAY",
            personName = "Alice",
            recurrenceRule = "YEARLY",
            notifyBeforeMinutes = 1440 // 1 day before
        )

        assertEquals("BIRTHDAY", reminder.eventType)
        assertEquals("Alice", reminder.personName)
        assertEquals("YEARLY", reminder.recurrenceRule)
        assertEquals(1440, reminder.notifyBeforeMinutes)
        assertFalse(reminder.isCompleted)
    }
}
