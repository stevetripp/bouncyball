package com.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongData(
    val inOutBallAnimationDuration: Int = 1,
    val lyricsAnimationDuration: Double = 0.2,
    val lines: List<LinesItem>,
//    @SerialName("image_transitions")
//	val imageTransitions: List<ImageTransitionsItem?>? = null,
//    @SerialName("cross_fade_image_animation_duration")
//    val crossFadeImageAnimationDuration: Double? = null,
    @SerialName("title_transition_start")
    val titleTransitionStart: Double,
//    val verses: List<VersesItem?>? = null,
//    val tintColor: Int? = null
)

//data class ImageTransitionsItem(
//	val image: Image? = null,
//	val effect: Int? = null,
//	val start: Int? = null
//)

//data class Image(
//	val color: Int? = null,
//	val regionOfInterest: Any? = null,
//	val renditions: String? = null,
//	val crop: String? = null
//)

//data class VersesItem(
//    val start: Double? = null
//)

@Serializable
data class LinesItem(
    val image: String? = null,
    val start: Double,
    val text: String,
    val syllables: List<SyllablesItem>
)

@Serializable
data class SyllablesItem(
    @SerialName("ball_range")
    val ballRange: String,
    val start: Double,
    val range: String
)