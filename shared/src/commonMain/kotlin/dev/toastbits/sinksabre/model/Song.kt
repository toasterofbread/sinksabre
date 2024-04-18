package dev.toatsbits.sinksabre.model

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable

interface Song {
    val name: String?
    val subname: String?
    val artist_name: String?
    val mapper_name: String?
    val bpm: Float?

    @Composable
    fun Preview(modifier: Modifier)
}
