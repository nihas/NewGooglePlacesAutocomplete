package app.map.placesautocomplete.java;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import app.map.placesautocomplete.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
 * AutoCompleteAdapter.java CREATED BY NIHAS NIZAR @May 27/2019
 * */
public class AutoCompleteAdapter extends ArrayAdapter<AutocompletePrediction> implements Filterable {

    private ArrayList<AutocompletePrediction> mResultList;
    private PlacesClient placesClient;

    public AutoCompleteAdapter(Context context, PlacesClient placesClient) {
        super(context, R.layout.autocomplete_place_item,android.R.id.text1);
        this.placesClient = placesClient;
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public AutocompletePrediction getItem(int position) {
        return mResultList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
//        View view = LayoutInflater.from(mContext).inflate(R.layout.autocomplete_place_item, parent, false);

        AutocompletePrediction item = getItem(position);

        TextView textView1 = row.findViewById(android.R.id.text1);
        TextView textView2 = row.findViewById(android.R.id.text2);
        if (item != null) {
            textView1.setText(item.getPrimaryText(null));
            textView2.setText(item.getSecondaryText(null));
        }

        return row;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                FilterResults results = new FilterResults();

                // We need a separate list to store the results, since
                // this is run asynchronously.
                ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                // Skip the autocomplete query if no constraints are given.
                if (charSequence != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    filterData = getAutocomplete(charSequence);
                }

                results.values = filterData;
                if (filterData != null) {
                    results.count = filterData.size();
                } else {
                    results.count = 0;
                }

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {

                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    mResultList = (ArrayList<AutocompletePrediction>) results.values;
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                } else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint) {

        //Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(23.63936, 68.14712),
                new LatLng(28.20453, 97.34466));


        final FindAutocompletePredictionsRequest.Builder requestBuilder =
                FindAutocompletePredictionsRequest.builder()
                        .setQuery(constraint.toString())
//                        .setCountry("IN") //Use only in specific country
                        // Call either setLocationBias() OR setLocationRestriction().
                        .setLocationBias(bounds)
//                        .setLocationRestriction(bounds)
                        .setSessionToken(AutocompleteSessionToken.newInstance())
                        .setTypeFilter(TypeFilter.ADDRESS);

        Task<FindAutocompletePredictionsResponse> results =
                placesClient.findAutocompletePredictions(requestBuilder.build());


        //Wait to get results.
        try {
            Tasks.await(results, 60, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        try {
            FindAutocompletePredictionsResponse autocompletePredictions = results.getResult();

            Log.i("TAG", "Query completed. Received " + autocompletePredictions.getAutocompletePredictions().size()
                    + " predictions.");

            // Freeze the results immutable representation that can be stored safely.
            return new ArrayList<AutocompletePrediction>(autocompletePredictions.getAutocompletePredictions());
        } catch (RuntimeExecutionException e) {
            // If the query did not complete successfully return null
            Toast.makeText(getContext(), "Error contacting API: " + e.toString(),
                    Toast.LENGTH_SHORT).show();
            Log.e("TAG", "Error getting autocomplete prediction API call", e);
            return null;
        }
    }
}
