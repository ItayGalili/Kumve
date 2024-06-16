package com.example.mykumve.ui.trip

/**
 * Manages trip creation and editing logic.
 * Handles data operations for trips.
 *
 * TODO: Implement methods for creating, updating, and deleting trips.
 */
class TravelManager(private val context: Context) {

    private val tripRepository: TripRepository by lazy {
        RepositoryProvider.getTripRepository(context)
    }

    // TODO: Implement methods for trip creation, update, and deletion.
}
