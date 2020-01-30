package io.mehow.squashit.screenshot.capture

import android.app.Activity
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_GRAVITY
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_UI
import io.mehow.squashit.screenshot.capture.TwistState.Finish
import io.mehow.squashit.screenshot.capture.TwistState.Start
import kotlin.math.absoluteValue

internal class CaptureDetector(
  private val onCapture: () -> Unit
) : SensorEventListener {
  private var startTimestamp = 0L
  private var twistState = Start

  private var gravity = FloatArray(3)
  private var magneticField = FloatArray(3)

  private val rotation = FloatArray(9)
  private val orientation = FloatArray(3)

  override fun onSensorChanged(event: SensorEvent) {
    updateSensorReadings(event)
    SensorManager.getRotationMatrix(rotation, null, gravity, magneticField)
    SensorManager.getOrientation(rotation, orientation)
    val planeOrientation = Math.toDegrees(orientation[2].absoluteValue.toDouble())

    val sample = TwistSample(event.timestamp, planeOrientation)
    if (twistState == Start) startTimestamp = sample.timestamp
    twistState = twistState.proceed(startTimestamp, sample) ?: Start
    if (twistState == Finish) onCapture()
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
      TYPE_MAGNETIC_FIELD -> magneticField = event.values
    }
  }

  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

  fun attach(activity: Activity) {
    val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager
    sensorManager.registerListener(this, accelerometer(sensorManager), SENSOR_DELAY_UI)
    sensorManager.registerListener(this, magnetometer(sensorManager), SENSOR_DELAY_UI)
  }

  fun detach(activity: Activity) {
    val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager
    sensorManager.unregisterListener(this, accelerometer(sensorManager))
    sensorManager.unregisterListener(this, magnetometer(sensorManager))
  }

  private fun accelerometer(sensorManager: SensorManager): Sensor {
    return sensorManager.getDefaultSensor(TYPE_GRAVITY)
        ?: sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
  }

  private fun magnetometer(sensorManager: SensorManager): Sensor {
    return sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)
  }
}
