package com.example.arnauddemion.prochesdemoi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends AppCompatActivity
        implements
        LocationListener,
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private Circle mCircle;
    private Marker mMarker;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add entries in ActionBar
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Manage click in ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_amis:
                Intent intenta = new Intent(this, Amis.class);
                this.startActivity(intenta);
                break;
            case R.id.action_recherche:
                Intent intentb = new Intent(this, Recherche.class);
                this.startActivity(intentb);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
    }

    private void drawCircle(LatLng latLng, LatLng latLngb, String prenom) {
        for(int rad=100;rad<=500;rad+=100)
        {
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)   //set center
                    .radius(rad)   //set radius in meters
                    .fillColor(0x10000000)  //default
                    .strokeColor(0x10000000)
                    .strokeWidth(5);

            mCircle = mMap.addCircle(circleOptions);

            // use DecimalFormat
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.0");
            String numberAsString = decimalFormat.format(distanceCalculation(latLng,latLngb));

            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(prenom)
                    .snippet("À " + numberAsString + " km"));
        }
    }



    public double distanceCalculation(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return Radius * c;
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onResume() {
        super.onResume();

        //Obtention de la référence du service
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        //Si le GPS est disponible, on s'y abonne
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            abonnementGPS();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        desabonnementGPS();
    }

    /**
     * Method allowing to subscribe from GPS data
     */
    public void abonnementGPS() {
        //On s'abonne
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
    }

    /**
     * Method allowing to unsubscribe from GPS data
     */
    public void desabonnementGPS() {
        // GPS subscription if available
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(final Location location) {

        LatLng latLngb = new LatLng(location.getLatitude(), location.getLongitude());

        /**
         * Ajouter la récupération des coordonnées des amis et leur nom sur le serveur
         */

        //LatLng latLng = new LatLng(43.23, 5.43);

        ArrayList<Personne> personnes;
        personnes = getPersonnes();

        for(Personne personne : personnes) {
            LatLng latLng = new LatLng(personne.getLatitude(), personne.getLongitude());

            drawCircle(latLng, latLngb, personne.getPrenom());

        }
    }

    @Override
    public void onProviderDisabled(final String provider) {
        // GPS unsubscription if deactivated
        if("gps".equals(provider)) {
            desabonnementGPS();
        }
    }

    @Override
    public void onProviderEnabled(final String provider) {
        //Si le GPS est activé on s'abonne
        if("gps".equals(provider)) {
            abonnementGPS();
        }
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {

    }

    /**
     * Récupère une liste de personnes.
     * @return ArrayList<Personne>: ou autre type de données.
     * @author François http://www.francoiscolin.fr/
     */
    public static ArrayList<Personne> getPersonnes() {

        ArrayList<Personne> personnes = new ArrayList<Personne>();

        try {
            String myurl = "https://proches-de-moi.piment-noir.org/api/person";

            URL url = new URL(myurl+"s");

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            /*
             * InputStreamOperations est une classe complémentaire:
             * Elle contient une méthode InputStreamToString.
             */
            String result = InputStreamOperations.InputStreamToString(inputStream);

            // On récupère le JSON complet
            JSONObject jsonObject = new JSONObject(result);

            // On récupère le tableau d'objets qui nous concernent
            JSONArray array = new JSONArray(jsonObject.getString("personnes"));

            // Pour tous les objets on récupère les infos
            for (int i = 0; i < array.length(); i++) {
                // On récupère un objet JSON du tableau
                JSONObject obj = new JSONObject(array.getString(i));
                // On fait le lien Personne - Objet JSON
                Personne personne = new Personne();
                personne.setId(obj.getInt("id"));
                personne.setNom(obj.getString("nom"));
                personne.setPrenom(obj.getString("prenom"));

                URL urlcoord = new URL(myurl+"/"+obj.getInt("id")+"/localisation");
                HttpsURLConnection connectioncoord = (HttpsURLConnection) urlcoord.openConnection();
                connectioncoord.connect();
                InputStream inputStreamcoord = connectioncoord.getInputStream();
                /*
                 * InputStreamOperations est une classe complémentaire:
                 * Elle contient une méthode InputStreamToString.
                 */
                String resultcoord = InputStreamOperations.InputStreamToString(inputStreamcoord);

                // On récupère le JSON complet
                JSONObject jsonObjectcoord = new JSONObject(resultcoord);

                // On récupère le tableau d'objets qui nous concernent
                JSONArray arraycoord = new JSONArray(jsonObject.getString("personnes"));
                // On récupère un objet JSON du tableau
                JSONObject objcoord = new JSONObject(arraycoord.getString(i));

                personne.setLatitude(objcoord.getDouble("latitude"));
                personne.setLongitude(objcoord.getDouble("longitude"));

                // On ajoute la personne à la liste
                personnes.add(personne);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // On retourne la liste des personnes
        return personnes;
    }

}

