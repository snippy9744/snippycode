package com.snippyseat.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.snippyseat.app.data.salon.GenderFilter
import com.snippyseat.app.data.salon.SalonFeed
import com.snippyseat.app.data.salon.SalonRepository
import com.snippyseat.app.data.salon.ServiceCategoryFilter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val feed: SalonFeed = SalonFeed(featured = emptyList(), nearby = emptyList()),
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val salonRepository: SalonRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()

    private val mutableSelectedGender = MutableStateFlow(GenderFilter.ALL)
    val selectedGender: StateFlow<GenderFilter> = mutableSelectedGender.asStateFlow()

    private val mutableSelectedCategory = MutableStateFlow<ServiceCategoryFilter?>(null)
    val selectedCategory: StateFlow<ServiceCategoryFilter?> = mutableSelectedCategory.asStateFlow()

    init {
        refresh()
    }

    fun selectGender(gender: GenderFilter) {
        mutableSelectedGender.value = gender
        refresh()
    }

    fun selectCategory(category: ServiceCategoryFilter?) {
        mutableSelectedCategory.value = category
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val wasLoaded = !mutableUiState.value.loading
            mutableUiState.value = mutableUiState.value.copy(
                loading = !wasLoaded,
                refreshing = wasLoaded,
                error = null,
            )
            runCatching {
                salonRepository.loadHomeFeed(
                    gender = mutableSelectedGender.value.apiValue,
                    category = mutableSelectedCategory.value?.apiValue,
                )
            }.onSuccess { feed ->
                mutableUiState.value = HomeUiState(loading = false, refreshing = false, feed = feed)
            }.onFailure { error ->
                mutableUiState.value = mutableUiState.value.copy(
                    loading = false,
                    refreshing = false,
                    error = error.message ?: "Could not load salons.",
                )
            }
        }
    }
}
