package io.mehow.squashit.screenshot.capture

import android.app.Activity
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_GRAVITY
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorManager.GRAVITY_EARTH
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import io.mehow.squashit.screenshot.capture.TwistState.Finish
import io.mehow.squashit.screenshot.capture.TwistState.Start

class AccelerationDetector(
  onCapture: () -> Unit
) : CaptureDetector(onCapture) {
  private var startTimestamp = 0L
  private var twistState = Start

  private var gravity = FloatArray(3)
  private val shakeDetector = ShakeDetector()

  override fun onSensorChanged(event: SensorEvent) {
    updateSensorReadings(event)
    val sample = AccelerationSample(event.timestamp, gravity[2] / GRAVITY_EARTH)
    if (twistState == Start) {
      startTimestamp = event.timestamp
      shakeDetector.clear()
    }
    twistState = sample.proceed(startTimestamp, twistState)
    if (twistState == Finish && !shakeDetector.isShaking) detectCapture()
  }

  private fun updateSensorReadings(event: SensorEvent) {
    when (event.sensor.type) {
      TYPE_ACCELEROMETER -> {
        val alpha = 0.8f
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
      }
      TYPE_GRAVITY -> gravity = event.values
    }
  }

  override fun attach(activity: Activity) {
    val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager
    sensorManager.registerListener(this, accelerometer(sensorManager), SENSOR_DELAY_FASTEST)
    shakeDetector.start(sensorManager)
  }

  override fun detach(activity: Activity) {
    val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager
    sensorManager.unregisterListener(this, accelerometer(sensorManager))
    shakeDetector.stop()
  }

  private fun accelerometer(sensorManager: SensorManager): Sensor {
    return sensorManager.getDefaultSensor(TYPE_GRAVITY)
        ?: sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
  }
}
