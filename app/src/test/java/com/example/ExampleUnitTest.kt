package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun waterIntake_progressAndVolume_isCorrect() {
    val targetGlasses = 8
    val consumedGlasses = 6
    val fraction = (consumedGlasses.toFloat() / targetGlasses.toFloat()).coerceIn(0f, 1f)
    val percentage = ((consumedGlasses.toFloat() / targetGlasses.toFloat()) * 100).toInt()
    val consumedMl = consumedGlasses * 250
    val targetMl = targetGlasses * 250

    assertEquals(0.75f, fraction, 0.001f)
    assertEquals(75, percentage)
    assertEquals(1500, consumedMl)
    assertEquals(2000, targetMl)
  }
}
