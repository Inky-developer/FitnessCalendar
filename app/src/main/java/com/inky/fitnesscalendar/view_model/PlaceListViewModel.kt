package com.inky.fitnesscalendar.view_model

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.repository.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppRepository
) : ViewModel() {
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places get() = _places.asStateFlow()

    private val _placeActivityCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val placeActivityCounts get() = _placeActivityCounts.asStateFlow()

    val snackbarHostState = SnackbarHostState()

    init {
        repository.getPlaces().onEach {
            _places.emit(it)
        }.launchIn(viewModelScope)

        repository.getActivityCountPerPlace().onEach {
            _placeActivityCounts.emit(it)
        }.launchIn(viewModelScope)
    }

    fun get(placeId: Int) = repository.getPlace(placeId)

    fun save(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        repository.savePlace(place)
    }

    fun delete(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.deletePlace(place)
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.deleted_place),
                actionLabel = context.getString(R.string.undo),
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> repository.savePlace(place)
                SnackbarResult.Dismissed -> {}
            }
        } catch (e: SQLiteConstraintException) {
            snackbarHostState.showSnackbar(message = context.getString(R.string.cannot_delete_place_because_there_are_still_activities))
        }
    }

}
