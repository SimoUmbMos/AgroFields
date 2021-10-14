package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityImportBinding;
import com.mosc.simo.ptuxiaki3741.enums.ImportAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ImportActivity extends AppCompatActivity {
    public static final String TAG = "ImportActivity";

    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final List<PolygonOptions> options = new ArrayList<>();

    private ActivityImportBinding binding;
    private GoogleMap mMap;

    private User curUser;
    private ImportAction action = ImportAction.VIEW;

    private void init(){
        initData();
        initActionBar();
        initActivity();
    }
    private void initData(){
        Intent intent = getIntent();
        if(intent != null){
            if(intent.getExtras() != null){
                if(intent.hasExtra(AppValues.actionImportActivity)){
                    action = (ImportAction) intent.getSerializableExtra(AppValues.actionImportActivity);
                }else{
                    action = ImportAction.IMPORT;
                }
                if(action == ImportAction.NONE){
                    setResult(RESULT_CANCELED);
                    finish();
                }
                AsyncTask.execute(()->{
                    long id = intent.getLongExtra(
                            AppValues.userNameImportActivity,
                            AppValues.sharedPreferenceDefaultUserViewModel
                    );
                    if(id != AppValues.sharedPreferenceDefaultUserViewModel){
                        UserViewModel userViewModel =
                                new ViewModelProvider(this).get(UserViewModel.class);
                        onUserUpdate(userViewModel.getUserByID(id));
                    }
                });
            }
        }
    }
    private void initActionBar() {
        setSupportActionBar(binding.tbImportActivity);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            switch (action){
                case IMPORT:
                    getSupportActionBar().show();
                    getSupportActionBar().setTitle(
                            getString(R.string.import_activity_import_action)
                    );
                    break;
                case ADD:
                    getSupportActionBar().show();
                    getSupportActionBar().setTitle(
                            getString(R.string.import_activity_add_action)
                    );
                    break;
                case SUBTRACT:
                    getSupportActionBar().show();
                    getSupportActionBar().setTitle(
                            getString(R.string.import_activity_subtract_action)
                    );
                    break;
                case VIEW:
                default:
                    getSupportActionBar().hide();
                    break;
            }
        }
    }
    private void initActivity(){
        lands.observe(this,this::onLandsUpdate);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapView);
        if(mapFragment != null)
            mapFragment.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setOnPolygonClickListener(this::onPolygonClick);
        AsyncTask.execute(()->
                lands.postValue(FileUtil.handleFile(getApplicationContext(),getIntent()))
        );
    }
    public void onPolygonClick(@NonNull Polygon polygon){
        if (curUser != null && (
            action == ImportAction.IMPORT ||
            action == ImportAction.ADD ||
            action == ImportAction.SUBTRACT
        )) {
            Intent returnIntent;
            returnIntent = new Intent();
            returnIntent.putExtra(
                    AppValues.resNameImportActivity,
                    new Land(new LandData(polygon.getPoints(),polygon.getHoles()))
            );
            setResult(RESULT_OK,returnIntent);
            finish();
        }
    }

    private void onLandsUpdate(List<Land> lands) {
        if(lands != null){
            if(lands.size()>0){
                int strokeColor = ContextCompat.getColor(this, R.color.polygonStroke);
                int fillColor = ContextCompat.getColor(this, R.color.polygonFill);
                AsyncTask.execute(()->{
                    final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for(Land land:lands){
                        for(LatLng point:land.getData().getBorder()){
                            builder.include(point);
                        }
                        options.add(LandUtil.getPolygonOptions(
                                land,
                                strokeColor,
                                fillColor,
                                true
                        ));
                    }
                    runOnUiThread(()->{
                        for(PolygonOptions option:options){
                            mMap.addPolygon(option);
                        }
                        if(options.size()>0)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                    builder.build(),
                                    AppValues.defaultPadding
                            ));
                        binding.tvDisplay.setVisibility(View.GONE);
                    });
                });
                return;
            }
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
    private void onUserUpdate(User user) {
        curUser = user;
    }

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityImportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}