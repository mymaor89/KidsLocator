package il.ac.sce.ac.maorya.kidslocator;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnPath;
    private Button btnClear;
    private ArrayList<LatLng> pathlist;
    private ArrayList<LatLng> polylist;
    private boolean pathFlag = false;
    private boolean polyFlag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        pathlist = new ArrayList<>();
        mapFragment.getMapAsync(this);
        btnPath = (Button) findViewById(R.id.btn_polygon);
        btnClear = (Button) findViewById(R.id.btn_clear);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final GoogleMap gmap = googleMap;
        LatLng sydney = new LatLng(-33.852, 151.211);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(pathFlag){
                    pathlist.add(latLng);
                }
                if(polyFlag){
                    pathlist.add(latLng);
                }
                gmap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("New marker"));
            }
        });
        path(gmap);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathlist.clear();
                gmap.clear();
                LatLng latLng = new LatLng(-33.852, 151.211);
                String title = "Sydney";
                MarkerModel marker = new MarkerModel(latLng,title);
                saveMarker(marker);
            }
        });
    }

    private void saveMarker(MarkerModel markerModel) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new user with a first and last name
        Map<String, Object> marker = new HashMap<>();
        marker.put("Lat", markerModel.getLatLng().latitude);
        marker.put("Lang", markerModel.getLatLng().longitude);
        marker.put("title", markerModel.getTitle());

        // Add a new document with a generated ID
        db.collection("markers")
                .add(marker)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FIREBASE", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FIREBASE", "Error adding document", e);
                    }
                });
    }

    private void path(final GoogleMap gmap) {
        btnPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pathFlag) {
                    pathFlag = true;
                    btnPath.setText("Done");
                    new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("Create path")
                            .setMessage("Select points, press button again when finished")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                } else {
                    pathFlag = false;
                    PolylineOptions polylineOptions = new PolylineOptions();
                    btnPath.setText("Path");
                    // Create polyline options with existing LatLng ArrayList
                    polylineOptions.addAll(pathlist);
                    polylineOptions
                            .width(5)
                            .color(Color.RED);

                    // Adding multiple points in map using polyline and arraylist
                    gmap.addPolyline(polylineOptions);

                }
            }
        });
    }
}
