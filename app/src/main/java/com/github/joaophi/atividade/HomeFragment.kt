package com.github.joaophi.atividade

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.joaophi.atividade.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.utils.component1
import com.google.maps.android.ktx.utils.component2
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.cos
import kotlin.math.sin
import android.hardware.SensorEvent as SystemSensorEvent


sealed interface SensorEvent {
    data class SensorChanged(val event: SystemSensorEvent) : SensorEvent
    data class AccuracyChanged(val sensor: Sensor, val accuracy: Int) : SensorEvent
}

fun SensorManager.flowOf(sensor: Sensor, samplingPeriodUs: Int) = callbackFlow {
    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SystemSensorEvent) {
            trySend(SensorEvent.SensorChanged(event))
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            trySend(SensorEvent.AccuracyChanged(sensor, accuracy))
        }
    }
    registerListener(listener, sensor, samplingPeriodUs)
    awaitClose { unregisterListener(listener, sensor) }
}

@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.getCurrentLocation(priority: Int): Location {
    return suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()

        getCurrentLocation(priority, cts.token)
            .addOnFailureListener(cont::resumeWithException)
            .addOnSuccessListener(cont::resume)

        cont.invokeOnCancellation { cts.cancel() }
    }
}

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)
        val mapFragment: SupportMapFragment = binding.map.getFragment()

        val locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val sensorManager: SensorManager? = requireContext().getSystemService()
        val proximity = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticField = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val map = mapFragment.awaitMap()

            var marker: Marker? = null
            suspend fun markCurrentLocation() {
                val location = locationClient
                    .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY)
                val latLng = LatLng(location.latitude, location.longitude)

                marker?.remove()
                marker = map.addMarker { position(latLng) }
                map.moveCamera(
                    CameraUpdateFactory
                        .newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 10f)),
                )
            }

            if (proximity != null) {
                sensorManager.flowOf(proximity, SensorManager.SENSOR_DELAY_NORMAL)
                    .filterIsInstance<SensorEvent.SensorChanged>()
                    .map { it.event.values.first() < 10f }
                    .distinctUntilChanged()
                    .onEach { isClose -> if (isClose) markCurrentLocation() }
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                    .launchIn(viewLifecycleOwner.lifecycleScope)
            }

            markCurrentLocation()

            fun moveMarker(x: Float, y: Float, bearing: Float) {
                var (lat, lng) = marker?.position ?: return
                val rad = Math.toRadians(bearing.toDouble())
                lat -= x * sin(rad) - y * cos(rad)
                lng += x * cos(rad) - y * sin(rad)
                val newPosition = LatLng(lat, lng)
                marker?.position = newPosition
                map.moveCamera(
                    CameraUpdateFactory
                        .newCameraPosition(
                            CameraPosition.builder()
                                .bearing(bearing)
                                .target(newPosition)
                                .zoom(10f)
                                .build()
                        )
                )
            }

            if (accelerometer != null && magneticField != null) {
                combine(
                    sensorManager
                        .flowOf(accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                        .filterIsInstance<SensorEvent.SensorChanged>(),
                    sensorManager
                        .flowOf(magneticField, SensorManager.SENSOR_DELAY_NORMAL)
                        .filterIsInstance<SensorEvent.SensorChanged>(),
                ) { (accelerometer), (magneticField) ->
                    val x = -accelerometer.values[0] / 300
                    val y = -accelerometer.values[1] / 300

                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    SensorManager
                        .getRotationMatrix(R, I, accelerometer.values, magneticField.values)
                        .takeIf { it } ?: return@combine
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R, orientation)
                    val azimuthInRadians = orientation[0].toDouble()
                    val azimuthInDegress = (Math.toDegrees(azimuthInRadians) + 360).mod(360f)

                    moveMarker(x, y, azimuthInDegress.toFloat())
                }
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                    .launchIn(viewLifecycleOwner.lifecycleScope)
            }
        }
    }
}