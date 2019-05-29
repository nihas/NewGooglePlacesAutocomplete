package app.map.placesautocomplete.kotlin

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import app.map.placesautocomplete.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.ArrayList
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


/*
* AutoCompleteAdapter.kt CREATED BY NIHAS NIZAR @May 27/2019
* */
class AutoCompleteAdapter(context: Context, var placesClient: PlacesClient) : ArrayAdapter<AutocompletePrediction>(context, R.layout.autocomplete_place_item, android.R.id.text1), Filterable {
    private var mResultList: ArrayList<AutocompletePrediction>? = null


    override fun getCount(): Int {
        return mResultList?.size!!
    }

    override fun getItem(position: Int): AutocompletePrediction? {
        return mResultList?.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)
        //        View view = LayoutInflater.from(mContext).inflate(R.layout.autocomplete_place_item, parent, false);

        val item = getItem(position)

        val textView1 = row.findViewById<TextView>(android.R.id.text1)
        val textView2 = row.findViewById<TextView>(android.R.id.text2)
        if (item != null) {
            textView1.text = item.getPrimaryText(null)
            textView2.text = item.getSecondaryText(null)
        }

        return row
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {

                val results = Filter.FilterResults()

                // We need a separate list to store the results, since
                // this is run asynchronously.
                var filterData: ArrayList<AutocompletePrediction>? = ArrayList()

                // Skip the autocomplete query if no constraints are given.
                if (charSequence != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    filterData = getAutocomplete(charSequence)
                }

                results.values = filterData
                if (filterData != null) {
                    results.count = filterData.size
                } else {
                    results.count = 0
                }

                return results
            }

            override fun publishResults(charSequence: CharSequence, results: Filter.FilterResults?) {

                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    mResultList = results.values as ArrayList<AutocompletePrediction>
                    notifyDataSetChanged()
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                return (resultValue as? AutocompletePrediction)?.getFullText(null)
                        ?: super.convertResultToString(resultValue)
            }
        }
    }


    private fun getAutocomplete(constraint: CharSequence): ArrayList<AutocompletePrediction>? {

        //Create a RectangularBounds object.
        val INDIA_BOUNDS = RectangularBounds.newInstance(
                LatLng(23.63936, 68.14712),
                LatLng(28.20453, 97.34466))


        val requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setQuery(constraint.toString())
                //.setCountry("IN") //Use only in specific country
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(INDIA_BOUNDS)
                //.setLocationRestriction(bounds)
                .setSessionToken(AutocompleteSessionToken.newInstance())
                .setTypeFilter(TypeFilter.ADDRESS)

        val results = placesClient.findAutocompletePredictions(requestBuilder.build())


        //Wait to get results.
        try {
            Tasks.await(results, 60, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }

        try {
            val autocompletePredictions = results?.result

            Log.i("TAG", "Query completed. Received " + autocompletePredictions!!.autocompletePredictions.size
                    + " predictions.")

            // Freeze the results immutable representation that can be stored safely.
            return ArrayList(autocompletePredictions.autocompletePredictions)
        } catch (e: RuntimeExecutionException) {
            // If the query did not complete successfully return null
            Toast.makeText(context, "Error contacting API: $e",
                    Toast.LENGTH_SHORT).show()
            Log.e("TAG", "Error getting autocomplete prediction API call", e)
            return null
        }

    }

}