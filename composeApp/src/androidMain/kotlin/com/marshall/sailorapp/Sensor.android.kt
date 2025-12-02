package com.marshall.sailorapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDeviceRotation(): State<Float> {
    val context = LocalContext.current
    val rotation = remember { mutableStateOf(0f) }
    
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { values ->
                    // values[0] is X axis, values[1] is Y axis
                    // When phone is portrait, X=0. When landscape, X is close to +/- 9.8
                    // We want rotation in degrees based on tilt
                    // Using atan2 to get angle
                    val x = values[0]
                    val y = values[1]
                    // Simple approximation: map X gravity to rotation
                    // If phone is tilted to left (landscape), x is positive
                    // If phone is tilted to right (landscape reverse), x is negative
                    // Let's normalize -10..10 to -90..90 degrees roughly
                    
                    // More precise calculation:
                    // angle = atan2(x, y) * (180/PI)
                    // When portrait upright: x=0, y=9.8 -> angle = 0
                    // When landscape left: x=9.8, y=0 -> angle = 90
                    // When landscape right: x=-9.8, y=0 -> angle = -90
                    
                    val angle = kotlin.math.atan2(x.toDouble(), y.toDouble()) * (180 / Math.PI)
                    rotation.value = angle.toFloat()
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    return rotation
}
