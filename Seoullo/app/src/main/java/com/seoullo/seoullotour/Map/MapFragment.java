package com.seoullo.seoullotour.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.annotations.Nullable;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//TODO : 북마크 지도에 표시하기
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static String TAG = "MapFragment";
    //naver map
    private MapView mapView;
    private NaverMap nMap;
    private String NAVER_CLIENT_ID = "";
    private String NAVER_CLIENT_SECRET_ID = "";
    //현위치
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    //got from recommend
    private Point mPoint;

    MapFragment() { }

    MapFragment(Point ref1) {
        this.mPoint = ref1;
    }

    //naver api key
    public static String getApiKeyFromManifest(Context context) {
        String apiKey = null;

        try {
            String e = context.getPackageName();
            ApplicationInfo ai = context
                    .getPackageManager()
                    .getApplicationInfo(e, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if(bundle != null) {
                apiKey = bundle.getString("com.naver.maps.map.CLIENT_ID");
            }
        } catch (Exception var6) {
            Log.d(TAG, "Caught non-fatal exception while retrieving apiKey: " + var6);
        }

        return apiKey;
    }
    public static String getApiKeyFromManifestSecret(Context context) {
        String apiKey = null;

        try {
            String e = context.getPackageName();
            ApplicationInfo ai = context
                    .getPackageManager()
                    .getApplicationInfo(e, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if(bundle != null) {
                apiKey = bundle.getString("com.naver.maps.map.CLIENT_SECRET_ID");
            }
        } catch (Exception var6) {
            Log.d(TAG, "Caught non-fatal exception while retrieving apiKey: " + var6);
        }

        return apiKey;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //API KEY init
        NAVER_CLIENT_ID = getApiKeyFromManifest(this.getContext());
        NAVER_CLIENT_SECRET_ID = getApiKeyFromManifestSecret(this.getContext());

        //네이버지도
        NaverMapSdk.getInstance(this.getContext()).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(NAVER_CLIENT_ID));

        //xml layout
        View view = inflater.inflate(R.layout.activity_map, container, false);

        //current location
        locationSource = new FusedLocationSource(getActivity(), LOCATION_PERMISSION_REQUEST_CODE);

        return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.naver_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        nMap = naverMap;
        //location
        nMap.setLocationSource(locationSource);

        //Ui setting
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);      //현위치버튼
        uiSettings.setZoomControlEnabled(true);         //줌버튼
        uiSettings.setIndoorLevelPickerEnabled(true);   //층별로 볼수있
        uiSettings.setLogoGravity(1);
        uiSettings.setLogoMargin(5,5, 450, 1000);
        uiSettings.setZoomGesturesEnabled(true);    //줌 제스처


        if(mPoint != null) {
            LatLng latLng = new LatLng(mPoint.x, mPoint.y);
            final Marker marker = new Marker();
            marker.setPosition(latLng);
            final InfoWindow mInfoWindow = new InfoWindow();
            mInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                @NonNull
                @Override
                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                   infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                       @Override
                       public boolean onClick(@NonNull Overlay overlay) {
                           try {
                               HttpConnection();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                           return false;
                       }
                   });

                    return mPoint.location;
                }
            });
            final boolean[] isInfoWindowOpen = {false};
            marker.setOnClickListener(new Overlay.OnClickListener() {
                @Override
                public boolean onClick(@NonNull Overlay overlay) {
                    if(!isInfoWindowOpen[0]) {
                        mInfoWindow.open(marker);
                        isInfoWindowOpen[0] = true;
                    } else {
                        mInfoWindow.close();
                        isInfoWindowOpen[0] = false;
                    }
                    return false;
                }
            });
            marker.setMap(nMap);
            nMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng, 16f));
        }
        else {
            LatLng latLng = new LatLng(37.5582, 127.0002);
            nMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng, 16f));
        }
    }

    //view life cycle
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        locationSource = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //location permission method
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    protected void HttpConnection() throws IOException {

        String url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving" +
                     "?start=37.5582, 127.0002" +
                     "&goal=" + mPoint.x + "," + mPoint.y
                    ;

        new RestAPITask(url, NAVER_CLIENT_ID, NAVER_CLIENT_SECRET_ID).execute();

    }
    // Rest API calling task
    public static class RestAPITask extends AsyncTask<Integer, Void, Void> {
        // Variable to store url
        protected String mURL;
        private String NAVER_CLIENT_ID;
        private String NAVER_CLIENT_SECRET_ID;

        // Constructor
        public RestAPITask(String url, String id, String secretId) {
            mURL = url;
            NAVER_CLIENT_ID = id;
            NAVER_CLIENT_SECRET_ID = secretId;
        }

        // Background work
        protected Void doInBackground(Integer... params) {
            String result = null;

            try {
                // Open the connection
                URL url = new URL(mURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID",NAVER_CLIENT_ID);
                conn.setRequestProperty("X-NCP-APIGW-API-KEY",NAVER_CLIENT_SECRET_ID);
                InputStream is = conn.getInputStream();

                System.out.println("RESPONSE CODE : " + conn.getResponseMessage() );

                // Get the stream
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Set the result
                conn.disconnect();
                result = builder.toString();
                System.out.println(result);
            }
            catch (Exception e) {
                // Error calling the rest api
                Log.e("REST_API", "GET method failed: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}
