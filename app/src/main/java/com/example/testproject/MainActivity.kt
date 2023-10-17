package com.example.testproject

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.LOCATION_HARDWARE
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.testproject.ui.theme.TestProjectTheme
import java.io.Console

class MainActivity : ComponentActivity() {

    var requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted: Boolean ->
        if(isGranted){
            recreate()
        }else{
            //Explain to the user that feature is unavailable because the feature requires a permission
            //that the user has denied.
            recreate()
            Toast.makeText(
                applicationContext,
                "permission is unfortunate not granted",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    private var longitude: Double = 0.0;
    private var latitude: Double = 0.0;



    val gpsLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationByGps= location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    //------------------------------------------------------//
    val networkLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationByNetwork= location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Put the login statement here ???
        when{
            ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {


                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)



                if (hasGps) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        0F,
                        gpsLocationListener
                    )
                }
                if (hasNetwork) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000,
                        0F,
                        networkLocationListener
                    )
                }

                val lastKnownLocationByGps =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastKnownLocationByGps?.let {
                    locationByGps = lastKnownLocationByGps
                }

                val lastKnownLocationByNetwork =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                lastKnownLocationByNetwork?.let {
                    locationByNetwork = lastKnownLocationByNetwork
                }

                if (locationByGps != null && locationByNetwork != null) {
                    if (locationByGps!!.accuracy > locationByNetwork!!.accuracy) {
                        currentLocation = locationByGps
                        latitude = currentLocation!!.latitude
                        longitude = currentLocation!!.longitude
                        // use latitude and longitude as per your need
                    } else {
                        currentLocation = locationByNetwork
                        latitude = currentLocation!!.latitude
                        longitude = currentLocation!!.longitude
                        // use latitude and longitude as per your need
                    }
                }

                Log.d("Location", "Longitude: $longitude, Latitude: $latitude")

                setContent {
                    Main()
                }
            }
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                /// ...
            }
            else -> {
                // Invoke permission launcher
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }
}
@Composable
fun Main(){
    TestProjectTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Conversation(SampleData.conversationSample)
        }
    }
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource(R.drawable.sonic),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if(isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
                ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn{
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun PreviewConversation() {
    TestProjectTheme {
        Conversation(SampleData.conversationSample)
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun RenderImage(path: Int){
    Row {
        Image(
            painter = painterResource(path) ,
            contentDescription = "Sonic"
        )
    }
}

@Composable
fun ImagePreview(path: Int){
    TestProjectTheme {
        RenderImage(path)
    }
}

@Composable
fun GreetingPreview() {
    TestProjectTheme {
        Greeting("Android")
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
    TestProjectTheme {
        Surface (modifier = Modifier.fillMaxSize()) {
            MessageCard(
                msg = Message("Lexi", "Take a look at Jetpack Compose, it's great!")
            )
        }
    }
}
