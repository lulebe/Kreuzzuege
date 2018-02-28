package de.lulebe.kreuzzuege.data

import android.graphics.Point


object FieldDistances {
    val distance_1 = listOf(
            Point(1, 0),
            Point(-1, 0),
            Point(0, 1),
            Point(0, -1)
    )
    val distance_2 = listOf(
            Point(2, 0),
            Point(1, 1),
            Point(1, -1),
            Point(0, 2),
            Point(0, -2),
            Point(-1, 1),
            Point(-1, -1),
            Point(-2, 0)
    )
    val distance_3 = listOf(
            Point(3, 0),
            Point(2, 1),
            Point(2, -1),
            Point(1, 2),
            Point(1, -2),
            Point(0, 3),
            Point(0, -3),
            Point(-1, 2),
            Point(-1, -2),
            Point(-2, 1),
            Point(-2, -1),
            Point(-3, 0)
    )
    val distance_4 = listOf(
            Point(4, 0),
            Point(3, 1),
            Point(3, -1),
            Point(2, 2),
            Point(2, -2),
            Point(1, 3),
            Point(1, -3),
            Point(0, 4),
            Point(0, -4),
            Point(-1, 3),
            Point(-1, -3),
            Point(-2, 2),
            Point(-2, -2),
            Point(-3, 1),
            Point(-3, -1),
            Point(-4, 0)
    )
}