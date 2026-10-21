package com.routefinder.model;

import com.routefinder.ui.BrowseView;
import com.routefinder.ui.BrowseView.SortOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies sorting comparators and multi-criteria ordering for the mountain catalog (Browse page).
 */
public class CatalogSortTest {

    private Mountain easyPeak;
    private Mountain moderatePeak;
    private Mountain hardPeak;
    private Mountain extremePeak;

    @BeforeEach
    void setUp() {
        easyPeak = new Mountain(40, "Guru Shikhar", "India", 1722, "easy", "Oct-Mar", "Gentle peak", null);
        moderatePeak = new Mountain(1, "Kilimanjaro", "Tanzania", 5895, "moderate", "Jun-Oct", "Volcanic peak", null);
        hardPeak = new Mountain(2, "Mont Blanc", "France", 4806, "hard", "Jun-Sep", "Alpine peak", null);
        extremePeak = new Mountain(36, "Everest", "Nepal", 8849, "extreme", "Apr-May", "Highest peak", null);
    }

    @Test
    void difficultyRankMappingMatchesExpectedHierarchy() {
        assertEquals(1, BrowseView.difficultyRank("easy"));
        assertEquals(1, BrowseView.difficultyRank("EASY"));
        assertEquals(2, BrowseView.difficultyRank("moderate"));
        assertEquals(3, BrowseView.difficultyRank("hard"));
        assertEquals(4, BrowseView.difficultyRank("extreme"));
        assertEquals(5, BrowseView.difficultyRank("unknown"));
        assertEquals(5, BrowseView.difficultyRank(null));
    }

    @Test
    void sortOptionEasyToExtremePlacesEasyFirst() {
        List<Mountain> list = new ArrayList<>(List.of(extremePeak, hardPeak, easyPeak, moderatePeak));
        list.sort(BrowseView.getComparator(SortOption.DIFFICULTY_EASY_TO_EXTREME, Set.of(1, 2)));

        assertEquals("Guru Shikhar", list.get(0).getName(), "Easy peak must appear first");
        assertEquals("Kilimanjaro", list.get(1).getName(), "Moderate peak must appear second");
        assertEquals("Mont Blanc", list.get(2).getName(), "Hard peak must appear third");
        assertEquals("Everest", list.get(3).getName(), "Extreme peak must appear last");
    }

    @Test
    void sortOptionExtremeToEasyPlacesExtremeFirst() {
        List<Mountain> list = new ArrayList<>(List.of(easyPeak, moderatePeak, hardPeak, extremePeak));
        list.sort(BrowseView.getComparator(SortOption.DIFFICULTY_EXTREME_TO_EASY, Set.of(1, 2)));

        assertEquals("Everest", list.get(0).getName(), "Extreme peak must appear first");
        assertEquals("Mont Blanc", list.get(1).getName(), "Hard peak must appear second");
        assertEquals("Kilimanjaro", list.get(2).getName(), "Moderate peak must appear third");
        assertEquals("Guru Shikhar", list.get(3).getName(), "Easy peak must appear last");
    }

    @Test
    void sortOptionElevationHighToLowPlacesHighestFirst() {
        List<Mountain> list = new ArrayList<>(List.of(easyPeak, extremePeak, moderatePeak, hardPeak));
        list.sort(BrowseView.getComparator(SortOption.ELEVATION_HIGH_TO_LOW, Set.of(1, 2)));

        assertEquals("Everest", list.get(0).getName());      // 8,849m
        assertEquals("Kilimanjaro", list.get(1).getName());  // 5,895m
        assertEquals("Mont Blanc", list.get(2).getName());   // 4,806m
        assertEquals("Guru Shikhar", list.get(3).getName()); // 1,722m
    }

    @Test
    void sortOptionNameAlphabeticalSortsAZ() {
        List<Mountain> list = new ArrayList<>(List.of(moderatePeak, easyPeak, extremePeak, hardPeak));
        list.sort(BrowseView.getComparator(SortOption.NAME_A_TO_Z, Set.of(1, 2)));

        assertEquals("Everest", list.get(0).getName());
        assertEquals("Guru Shikhar", list.get(1).getName());
        assertEquals("Kilimanjaro", list.get(2).getName());
        assertEquals("Mont Blanc", list.get(3).getName());
    }

    @Test
    void sortOptionSeededFirstPrioritizesHeroPeaks() {
        // Mountains with IDs 1 and 2 are seeded; IDs 36 and 40 are unseeded
        Set<Integer> seededIds = Set.of(1, 2);
        List<Mountain> list = new ArrayList<>(List.of(extremePeak, easyPeak, moderatePeak, hardPeak));
        list.sort(BrowseView.getComparator(SortOption.SEEDED_FIRST, seededIds));

        // The first two peaks must be seeded IDs
        assertTrue(seededIds.contains(list.get(0).getId()), "First item must be seeded");
        assertTrue(seededIds.contains(list.get(1).getId()), "Second item must be seeded");

        // The remaining two peaks must be unseeded
        assertTrue(!seededIds.contains(list.get(2).getId()), "Third item must be unseeded");
        assertTrue(!seededIds.contains(list.get(3).getId()), "Fourth item must be unseeded");
    }
}
