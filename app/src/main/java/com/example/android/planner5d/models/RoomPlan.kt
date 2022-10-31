package com.example.android.planner5d.models

data class RoomPlan (
    val ProjectName: String  // для отладки
) {
    companion object {
        fun fillEmpty() = RoomPlan("")
    }
}
