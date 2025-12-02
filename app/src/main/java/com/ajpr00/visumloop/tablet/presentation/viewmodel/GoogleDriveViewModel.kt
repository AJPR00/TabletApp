package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.repository.GoogleDriveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoogleDriveViewModel @Inject constructor(
    private val driveRepository: GoogleDriveRepository
) : ViewModel() {

    var files by mutableStateOf<List<String>>(emptyList())
        private set
    fun loadFiles(idToken: String) {
        viewModelScope.launch {
            files = driveRepository.listDriveFiles(idToken)
            Log.d("GoogleDriveViewModel", "Files: $files")
        }
    }
}
