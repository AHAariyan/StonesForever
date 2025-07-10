package com.hady.stonesforever.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.hady.stonesforever.common.InputScanOption
import com.hady.stonesforever.data.model.BatchMovement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class InputActionViewModel @Inject constructor() : ViewModel() {

    private val _selectedScanOption = MutableStateFlow<InputScanOption>(InputScanOption.NONE)
    val selectedScanOption: StateFlow<InputScanOption> = _selectedScanOption.asStateFlow()



    fun updateScanOption(scanOption: InputScanOption) {
        _selectedScanOption.value = scanOption
    }

}