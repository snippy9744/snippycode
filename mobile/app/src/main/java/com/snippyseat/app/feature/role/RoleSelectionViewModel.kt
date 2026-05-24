package com.snippyseat.app.feature.role

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.snippyseat.app.core.model.UserRole
import com.snippyseat.app.data.auth.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoleSelectionUiState(
    val selectedRole: UserRole? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(RoleSelectionUiState())
    val uiState: StateFlow<RoleSelectionUiState> = mutableUiState.asStateFlow()

    fun select(role: UserRole) {
        mutableUiState.value = mutableUiState.value.copy(selectedRole = role, error = null)
    }

    fun continueWithSelection(onDone: (UserRole) -> Unit) {
        val role = mutableUiState.value.selectedRole

        if (role == null) {
            mutableUiState.value = mutableUiState.value.copy(error = "Choose how you want to use Snippy Seat.")
            return
        }

        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loading = true, error = null)
            runCatching { authRepository.selectRole(role) }
                .onSuccess {
                    mutableUiState.value = mutableUiState.value.copy(loading = false)
                    onDone(role)
                }
                .onFailure { error ->
                    mutableUiState.value = mutableUiState.value.copy(
                        loading = false,
                        error = error.message ?: "Could not save role. Try again.",
                    )
                }
        }
    }
}
