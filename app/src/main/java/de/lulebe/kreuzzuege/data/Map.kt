package de.lulebe.kreuzzuege.data


data class Map(
        val id: Int,
        val sizeX: Int,
        val sizeY: Int,
        val fields: Array<Field>
)