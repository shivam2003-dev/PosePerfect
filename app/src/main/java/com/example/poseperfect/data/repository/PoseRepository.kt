package com.example.poseperfect.data.repository

import com.example.poseperfect.domain.model.PoseResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Holds the live stream of [PoseResult]s emitted by the ML analyser.
 * Acts as the single source of truth in the data layer.
 */
class PoseRepository {

    private val _poseResults = MutableSharedFlow<PoseResult>(extraBufferCapacity = 1)
    val poseResults: SharedFlow<PoseResult> = _poseResults.asSharedFlow()

    fun emit(result: PoseResult) {
        _poseResults.tryEmit(result)
    }
}

