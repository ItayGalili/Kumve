package com.example.mykumve.data.data_classes

data class MapState(
    val markers: List<Point>,
    val polyline: List<Point>,
    val distance: Double
)