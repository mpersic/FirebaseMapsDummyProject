package com.pers.pingme

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import java.lang.Exception
import java.util.Collections.sort

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var  databaseReference: DatabaseReference

    private lateinit var locationListener:LocationListener
    private lateinit var locationManager:LocationManager

    private final var MIN_TIME:Long=1000
    private final var MIN_DST:Long=5

    private lateinit var editTextLatitude:EditText
    private lateinit var editTextLongitude: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val permissions= arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions, PackageManager.PERMISSION_GRANTED)

        editTextLatitude=findViewById(R.id.latitude)
        editTextLongitude=findViewById(R.id.longtitude)

        databaseReference=FirebaseDatabase.getInstance().getReference("Location")
        databaseReference.addValueEventListener( object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) = try {
                var databaseLatitude: String = p0.child("latitude").value.toString()
                    .substring(1, p0.child("latitude").value.toString().length - 1)
                var databaseLongitude: String = p0.child("longitude").value.toString()
                    .substring(1, p0.child("longitude").value.toString().length - 1)

                var stringLat: List<String> = databaseLatitude.split(", ")
                stringLat.sorted()
                sort(stringLat)
                var latitude= stringLat[stringLat.size-1].split("=")[1]

                var stringLong: List<String> = databaseLongitude.split(", ")
                sort(stringLong)
                var longitude= stringLong[stringLong.size-1].split("=")[1]

                var latLng=LatLng(latitude.toDouble(),longitude.toDouble())

                mMap.addMarker(MarkerOptions().position(latLng).title(latitude + " , " + longitude))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

            }


        catch(e:Exception){
            e.printStackTrace()
        }

    })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        locationListener=object:LocationListener{
            override fun onLocationChanged(location: Location?) {
                try{
                    if (location != null) {
                        editTextLatitude.text=Editable.Factory.getInstance().newEditable(location.latitude.toString())
                    }
                    if (location != null) {
                        editTextLongitude.text=Editable.Factory.getInstance().newEditable(location.longitude.toString())
                    }
                }
                catch(e:Exception){
                    e.printStackTrace()
                }

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {
            }

        }

        locationManager=getSystemService(Context.LOCATION_SERVICE)as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME,
                MIN_DST.toFloat(),
                locationListener
            )
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME,
                MIN_DST.toFloat(),
                locationListener
            )
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }
    public fun updateButtonOnClick(view: View){
            databaseReference.child("latitude").push().setValue(editTextLatitude.text.toString())
            databaseReference.child("longitude").push().setValue(editTextLongitude.text.toString())
        }
    }

