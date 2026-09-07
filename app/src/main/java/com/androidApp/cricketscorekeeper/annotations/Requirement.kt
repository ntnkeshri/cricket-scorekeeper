package com.androidApp.cricketscorekeeper.annotations

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Requirement(
    val id: String,
    val description: String
)
