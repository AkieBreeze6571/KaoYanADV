package com.example.kaoyanadventure.data

const val GAME_REVIEW_SUBJECT_NAME = "游戏复盘"

fun isGameReviewSubjectName(name: String): Boolean {
    return name.trim() == GAME_REVIEW_SUBJECT_NAME
}
