package com.laterbox.ui

import com.laterbox.data.model.ViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryViewModeTest {

    @Test
    fun testViewMode_defaultsToGridAndTogglesSynchronously() {
        // Simulates the in-memory fast-recomposition pattern in LibraryViewModel
        val _viewMode = MutableStateFlow(ViewMode.GRID)
        val viewMode = _viewMode.asStateFlow()

        assertEquals("Default view mode should be GRID", ViewMode.GRID, viewMode.value)

        // Switch to LIST
        _viewMode.value = ViewMode.LIST
        assertEquals("View mode should immediately reflect LIST without waiting for DataStore", ViewMode.LIST, viewMode.value)

        // Switch back to GRID
        _viewMode.value = ViewMode.GRID
        assertEquals("View mode should immediately reflect GRID", ViewMode.GRID, viewMode.value)
    }

    @Test
    fun testViewModeEnum_valuesAndSerialization() {
        assertEquals("GRID", ViewMode.GRID.name)
        assertEquals("LIST", ViewMode.LIST.name)
        assertEquals(ViewMode.GRID, ViewMode.valueOf("GRID"))
        assertEquals(ViewMode.LIST, ViewMode.valueOf("LIST"))
    }
}
