package com.filipkesteli.googlemaps;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    //definiramo konstante
    private static LatLng LAT_LNG_ZAGREB = new LatLng(45.817, 16);
    private static final float INIT_ZOOM_LEVEL = 17.0f;
    private static final String ADRESS = "Trg bana Josipa Jelačića, Zagreb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */

    //Tu cemo dodavati svoje stvari:
    private void setUpMap() {
        configureMap(); //konfigurirati mapu

        LAT_LNG_ZAGREB = getLatLangFromAdress(ADRESS); //metoda koja ce dati adresu

        addMarker(LAT_LNG_ZAGREB); //dodati marker -> gdje hoces marker
        setAdapter(); //adapter za poppup window
        animateCamera(LAT_LNG_ZAGREB); //animirat cemo kameru
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        setupListener(); //osluskuj mi na mapi dogadaje... stavit cemo i marker i animirat kameru
    }

    private void setupListener() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addMarker(latLng);
                animateCamera(latLng);
                String address = getAdressFromLatLng(latLng);

                Toast.makeText(MapsActivity.this, address, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getAdressFromLatLng(LatLng latLng) {
        String address = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses.size() > 0) {
                Address a = addresses.get(0); //izvuci mi prvu adresu ako imas adresu
                for (int i = 0; i < a.getMaxAddressLineIndex(); i++) {
                    address += a.getAddressLine(i) + "\n";
                }
            }
        } catch (IOException e) {
        }

        return address;
    }

    //zvat cemo metodu
    private LatLng getLatLangFromAdress(String adress) {

        LatLng latLng = LAT_LNG_ZAGREB; //Osiguravamo se da geoCoder ne pukne!
        //API od GEOCODER-a:
        Geocoder geocoder = new Geocoder(this, Locale.getDefault()); //lociranje geocodera prema nekom lokalitetu
        //idem pitati Geocoder
        try {
            //daj mi listu adresa (ili jednu adresu)
            //izvuci mi lattitude i longitude
            List<Address> addresses = geocoder.getFromLocationName(adress, 1);
            if (addresses.size() > 0) {
                Address a = addresses.get(0);
                double lat = a.getLatitude();
                double lng = a.getLongitude();
                latLng = new LatLng(lat, lng);
            }
        } catch (IOException e) {
        }

        return latLng;
    }

    private void setAdapter() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            //vraca null po defaultu
            @Override
            public View getInfoWindow(Marker marker) {
                //inflateamo view element
                View view = getLayoutInflater().inflate(R.layout.info_window, null);

                TextView markerText = (TextView) view.findViewById(R.id.marker_text);
                ImageView markerIcon = (ImageView) view.findViewById(R.id.marker_icon);

                //umecemo text i sliku
                markerText.setText(R.string.zagreb_glavni_grad);
                markerIcon.setImageResource(R.drawable.ic_location_city_black_24dp);

                return view;
            }

            //vraca nesto sto ja hocu
            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
    }

    private void configureMap() {
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setTrafficEnabled(true);
    }

    private void addMarker(LatLng latLng) {
        mMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.zagreb_glavni_grad))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_city_black_24dp))
        );
    }

    private void animateCamera(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, INIT_ZOOM_LEVEL));
    }
}
