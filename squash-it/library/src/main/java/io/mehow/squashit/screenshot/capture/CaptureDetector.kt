package io.mehow.squashit.screenshot.capture

import android.app.Activity
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager

abstract class CaptureDetector(
  private val onCapture: () -> Unit
) : SensorEventListener {
  protected fun detectCapture() = onCapture()

  abstract fun attach(activity: Activity)

  abstract fun detach(activity: Activity)

  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

  companion object {
    fun create(context: Context, onCapture: () -> Unit): CaptureDetector {
      return if (hasMagnetometer(context)) RotationDetector(onCapture)
      else AccelerationDetector(onCapture)
    }

    private fun hasMagnetometer(context: Context): Boolean {
      val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
      return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
    }
  }
}
