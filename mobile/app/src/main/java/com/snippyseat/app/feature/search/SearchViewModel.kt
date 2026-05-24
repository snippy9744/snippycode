package com.snippyseat.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.snippyseat.app.data.salon.GenderFilter
import com.snippyseat.app.data.salon.Salon
import com.snippyseat.app.data.salon.SalonRepository
import com.snippyseat.app.data.salon.SalonSearchFilters
import com.snippyseat.app.data.salon.ServiceCategoryFilter
import com.snippyseat.app.data.salon.SortOption
import com.snippyseat.app.data.search.RecentSearchStore
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val results: List<Salon> = emptyList(),
    val filters: SalonSearchFilters = SalonSearchFilters(),
    val filterSheetOpen: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val salonRepository: SalonRepository,
    recentSearchStore: RecentSearchStore,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = mutableUiState.asStateFlow()
    val recentSearches: StateFlow<List<String>> = recentSearchStore.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val searchStore = recentSearchStore
    private var searchJob: Job? = null

    init {
        searchNow()
    }

    fun updateQuery(value: String) {
        mutableUiState.value = mutableUiState.value.copy(query = value, error = null)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            searchNow()
        }
    }

    fun submitSearch(query: String = mutableUiState.value.query) {
        updateQuery(query)
        viewModelScope.launch { searchStore.addSearch(query) }
        searchNow()
    }

    fun setFilterSheet(open: Boolean) {
        mutableUiState.value = mutableUiState.value.copy(filterSheetOpen = open)
    }

    fun updateGender(gender: GenderFilter) {
        mutableUiState.value = mutableUiState.value.copy(filters = mutableUiState.value.filters.copy(gender = gender))
    }

    fun updateDistance(distance: Float) {
        mutableUiState.value = mutableUiState.value.copy(
            filters = mutableUiState.value.filters.copy(distanceKm = distance),
        )
    }

    fun updateRating(rating: Double?) {
        mutableUiState.value = mutableUiState.value.copy(filters = mutableUiState.value.filters.copy(rating = rating))
    }

    fun toggleService(service: ServiceCategoryFilter) {
        val current = mutableUiState.value.filters.services
        val next = if (service in current) current - service else current + service
        mutableUiState.value = mutableUiState.value.copy(filters = mutableUiState.value.filters.copy(services = next))
    }

    fun updateHomeServiceOnly(enabled: Boolean) {
        mutableUiState.value = mutableUiState.value.copy(
            filters = mutableUiState.value.filters.copy(homeServiceOnly = enabled),
        )
    }

    fun updateSort(sort: SortOption) {
        mutableUiState.value = mutableUiState.value.copy(filters = mutableUiState.value.filters.copy(sort = sort))
    }

    fun applyFilters() {
        mutableUiState.value = mutableUiState.value.copy(filterSheetOpen = false)
        searchNow()
    }

    private fun searchNow() {
        viewModelScope.launch {
            val state = mutableUiState.value
            mutableUiState.value = state.copy(loading = true, error = null)
            runCatching {
                salonRepository.searchSalons(query = state.query, filters = state.filters)
            }.onSuccess { results ->
                mutableUiState.value = mutableUiState.value.copy(loading = false, results = results)
            }.onFailure { error ->
                mutableUiState.value = mutableUiState.value.copy(
                    loading = false,
                    error = error.message ?: "Could not search salons.",
                )
            }
        }
    }
}
