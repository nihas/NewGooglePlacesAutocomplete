package app.map.placesautocomplete

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import app.map.placesautocomplete.kotlin.AutoCompleteAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder
import java.util.*

/*
* CREATED BY NIHAS NIZAR @May 27/2019
* */
class MainActivity : AppCompatActivity() {
    lateinit var placesClient: PlacesClient
    lateinit var mAdapter : AutoCompleteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Initialize Places.
        var apiKey = getString(R.string.api_key)
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }
        placesClient = Places.createClient(this)

        setUpAutoCompleteTextView()
    }

    private fun setUpAutoCompleteTextView() {
        autocomplete.setThreshold(1)
        autocomplete.onItemClickListener = autocompleteClickListener
        mAdapter = AutoCompleteAdapter(this, placesClient)
        autocomplete.setAdapter(mAdapter)
    }



    var autocompleteClickListener: AdapterView.OnItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        var item: AutocompletePrediction? = mAdapter.getItem(position)
        var placeID = item?.placeId
        var placeFields:  List<Place.Field> = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        var request: FetchPlaceRequest? = null
        if(placeID!=null){
            request = FetchPlaceRequest.builder(placeID, placeFields).build()
        }

        if(request!=null){
            placesClient.fetchPlace(request).addOnSuccessListener {
                //                responseView.setText(task.getPlace().getName() + "\n" + task.getPlace().getAddress());
                val place = it.place
                var stringBuilder = StringBuilder()
                stringBuilder.append("Name: ${place.name}\n")
                var queriedLocation: LatLng? = place.latLng
                stringBuilder.append("Latitude: ${queriedLocation?.latitude}\n")
                stringBuilder.append("Longitude: ${queriedLocation?.longitude}\n")
                stringBuilder.append("Address: ${place.address}\n")
                response_view.text = stringBuilder
                Log.i("TAG", "Called getPlaceById to get Place details for $placeID")

            }.addOnFailureListener {
                it.printStackTrace()
                response_view.text = it.message
            }
        }
        hideKeyboard()
    }


    fun Activity.hideKeyboard() {
        hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
