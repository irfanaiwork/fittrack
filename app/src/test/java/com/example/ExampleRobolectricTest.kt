package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FitTrack Daily", appName)
  }

  @Test
  fun `verify default data plan`() {
    val meals = com.example.data.database.DefaultData.getDefaultMeals()
    assertEquals(4, meals.size)
    assertEquals("Breakfast", meals[0].name)
    assertEquals("Lunch", meals[1].name)
    assertEquals("Evening Snack", meals[2].name)
    assertEquals("Dinner", meals[3].name)

    val lowMeals = com.example.data.database.DefaultData.getMealsForPlan(com.example.data.database.DefaultData.PLAN_LOW)
    assertEquals(4, lowMeals.size)

    val mediumMeals = com.example.data.database.DefaultData.getMealsForPlan(com.example.data.database.DefaultData.PLAN_MEDIUM)
    assertEquals(4, mediumMeals.size)

    val highMeals = com.example.data.database.DefaultData.getMealsForPlan(com.example.data.database.DefaultData.PLAN_HIGH)
    assertEquals(4, highMeals.size)

    val exercises = com.example.data.database.DefaultData.getDefaultExercises()
    assertEquals(7, exercises.size)

    val workoutItems = com.example.data.database.DefaultData.getDefaultWorkoutItems()
    assertEquals(7, workoutItems.size)
  }
}
