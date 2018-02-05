package de.lulebe.kreuzzuege.data

data class Player(
        val faction: Int,
        var money: Int,
        val units: MutableList<Unit>,
        val buildings: MutableList<Int>
)