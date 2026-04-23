package com.example.transporttracker.domain.usecase

sealed class TripEvent {

    data object TripStarted : TripEvent()

    data object TripEnded : TripEvent()
}