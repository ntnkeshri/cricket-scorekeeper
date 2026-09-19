package com.ntnkeshri.cricketscorekeeper.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Requirement(
    val id: String,
    val description: String
)
