package io.mehow.squashit.screenshot.capture

import android.app.Activity
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import io.mehow.squashit.screenshot.capture.TwistState.End
import io.mehow.squashit.screenshot.capture.TwistState.Start

internal class TwistDetector(
  private val onTwistDetected: () -> Unit
) : SensorEventListener {
  private var startTimestamp: Long = 0
  private var twistState = Start

  override fun onSensorChanged(event: SensorEvent) {
    val ax = event.values[0] / SensorManager.GRAVITY_EARTH
    val ay = event.values[1] / SensorManager.GRAVITY_EARTH
    val az = event.values[2] / SensorManager.GRAVITY_EARTH
    val sample = TwistSample(event.timestamp, ax, ay, az)
    if (twistState == Start) startTimestamp = sample.timestamp
    twistState = twistState.proceed(startTimestamp, sample) ?: Start
    if (twistState == End) onTwistDetected()
  }

  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

  fun attach(activity: Activity) {
    val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
    sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_FASTEST)
  }

  fun detach(activity: Activity) {
    val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
    sensorManager.unregisterListener(this, accelerometer)
  }
}
