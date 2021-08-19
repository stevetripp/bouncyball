package com.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class SongData(
    val lines: List<LinesItem>
) {
    companion object {

        private val json = """{
                "lines": [
                    {
                        "start": 22.277282999999998,
                        "syllables": [
                            {
                                "ball_range": "{0,1}",
                                "range": "{0,1}",
                                "start": 22.277282999999998
                            },
                            {
                                "ball_range": "{2,2}",
                                "range": "{2,2}",
                                "start": 22.803282999999998
                            },
                            {
                                "ball_range": "{5,1}",
                                "range": "{5,1}",
                                "start": 23.110283
                            },
                            {
                                "ball_range": "{7,5}",
                                "range": "{7,5}",
                                "start": 23.522282999999999
                            },
                            {
                                "ball_range": "{13,2}",
                                "range": "{13,2}",
                                "start": 24.098283
                            },
                            {
                                "ball_range": "{16,3}",
                                "range": "{16,4}",
                                "start": 24.659283
                            }
                        ],
                        "text": "I am a child of God,"
                    },
                    {
                        "start": 26.449282999999999,
                        "syllables": [
                            {
                                "ball_range": "{0,3}",
                                "range": "{0,3}",
                                "start": 26.449282999999999
                            },
                            {
                                "ball_range": "{4,2}",
                                "range": "{4,2}",
                                "start": 27.153283
                            },
                            {
                                "ball_range": "{7,3}",
                                "range": "{7,3}",
                                "start": 28.082282999999998
                            },
                            {
                                "ball_range": "{11,4}",
                                "range": "{11,4}",
                                "start": 28.447283
                            },
                            {
                                "ball_range": "{16,2}",
                                "range": "{16,2}",
                                "start": 29.043283
                            },
                            {
                                "ball_range": "{19,4}",
                                "range": "{19,5}",
                                "start": 29.616283
                            }
                        ],
                        "text": "And he has sent me here."
                    },
                    {
                        "image": null,
                        "start": 31.616283,
                        "syllables": [
                            {
                                "ball_range": "{0,3}",
                                "range": "{0,3}",
                                "start": 31.616283
                            },
                            {
                                "ball_range": "{4,3}",
                                "range": "{4,3}",
                                "start": 32.226283
                            },
                            {
                                "ball_range": "{7,2}",
                                "range": "{7,2}",
                                "start": 32.863283
                            },
                            {
                                "ball_range": "{10,2}",
                                "range": "{10,2}",
                                "start": 33.486283
                            },
                            {
                                "ball_range": "{13,2}",
                                "range": "{13,2}",
                                "start": 34.044283
                            },
                            {
                                "ball_range": "{16,5}",
                                "range": "{16,5}",
                                "start": 34.606283000000008
                            },
                            {
                                "ball_range": "{21,2}",
                                "range": "{21,2}",
                                "start": 35.200283
                            },
                            {
                                "ball_range": "{24,4}",
                                "range": "{24,4}",
                                "start": 35.854283
                            }
                        ],
                        "text": "Has given me an earthly home"
                    }
                ]
            }"""

        val defaultData = Json.decodeFromString<SongData>(json)
    }
}

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

