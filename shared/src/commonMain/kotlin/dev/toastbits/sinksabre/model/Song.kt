package dev.toatsbits.sinksabre.model

interface Song {
    val id: String
    val name: String?
    val subname: String?
    val artist_name: String?
    val mapper_name: String?
    val bpm: Float?
}
