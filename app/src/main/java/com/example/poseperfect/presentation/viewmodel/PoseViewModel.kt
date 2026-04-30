package com.example.poseperfect.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poseperfect.domain.model.FeedbackItem
import com.example.poseperfect.domain.model.PoseResult
import com.example.poseperfect.domain.model.PoseTemplate
import com.example.poseperfect.domain.usecase.GetPoseFeedbackUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Full UI state for the camera screen. */
data class PoseUiState(
    val poseResult: PoseResult? = null,
    val feedbackItems: List<FeedbackItem> = emptyList(),
    val poseScore: Int = 0,
    val isDetecting: Boolean = false,
    val activeTemplate: PoseTemplate = PoseTemplate.PROFESSIONAL,
    val isFrontCamera: Boolean = true,
    val isOptimalPose: Boolean = false
)

/**
 * Manages [PoseUiState] and delegates ML work to [GetPoseFeedbackUseCase].
 */
class PoseViewModel : ViewModel() {

    private val feedbackUseCase = GetPoseFeedbackUseCase()

    private val _uiState = MutableStateFlow(PoseUiState())
    val uiState: StateFlow<PoseUiState> = _uiState.asStateFlow()

    // ── Camera events ─────────────────────────────────────────────────────────

    fun onPoseDetected(rawResult: PoseResult) {
        viewModelScope.launch(Dispatchers.Default) {
            val (smoothed, feedback, score) = feedbackUseCase(rawResult)
            val optimal = score >= OPTIMAL_SCORE_THRESHOLD

            _uiState.update {
                it.copy(
                    poseResult = smoothed,
                    feedbackItems = feedback,
                    poseScore = score,
                    isDetecting = true,
                    isOptimalPose = optimal
                )
            }
        }
    }

    fun onNoPoseDetected() {
        _uiState.update { it.copy(isDetecting = false, isOptimalPose = false) }
    }

    // ── User actions ──────────────────────────────────────────────────────────

    fun setTemplate(template: PoseTemplate) {
        feedbackUseCase.setTemplate(template)
        _uiState.update { it.copy(activeTemplate = template) }
    }

    fun flipCamera() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    companion object {
        private const val OPTIMAL_SCORE_THRESHOLD = 85
    }
}

