package com.mosc.simo.ptuxiaki3741;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.helper.ExportFieldModel;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.enums.LandListActionState;
import com.mosc.simo.ptuxiaki3741.enums.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.enums.LandListNavigateStates;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.navigators.LandListNavigator;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.holders.LandListMenuHolder;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.holders.LandListRecycleViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileExporter.XMLOUTPUT;

public class LandListFragment  extends Fragment implements FragmentBackPress {
    public static final String TAG ="LandListFragment";

    private LandViewModel vmLands;
    private UserViewModel vmUsers;
    private User currUser;

    private LandListRecycleViewHolder viewHolder;
    private LandListMenuHolder menuHolder;
    private LandListNavigator nav;
    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_land_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
        if(menuHolder != null){
            Log.d(TAG, "onCreateOptionsMenu: menuHolder.initMenu");
            menuHolder.initMenu(menu);
            menuHolder.setupMenu(LandListMenuState.NormalState);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: "+item.getItemId());
        if(menuHolder.menuItemClick(item.getItemId())){
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onBackPressed() {
        if(menuHolder.getState() != LandListMenuState.NormalState){
            menuHolder.setState(LandListMenuState.NormalState);
            return false;
        }else{
            return true;
        }
    }

    private void init(View view) {
        MainActivity activity = (MainActivity) getActivity();
        actionBar = null;
        if (activity != null) {
            actionBar = activity.getSupportActionBar();
            activity.setOnBackPressed(this);
            changeActionBarTitle("");
            getViewModels(activity);
            initObservers();
            initHolders(view);
        }
    }
    private void changeActionBarTitle(String title) {
        if( actionBar != null ){
            actionBar.setTitle(title);
            actionBar.show();
        }
    }
    private void getViewModels(MainActivity activity) {
        vmLands = new ViewModelProvider(activity).get(LandViewModel.class);
        vmUsers = new ViewModelProvider(activity).get(UserViewModel.class);
    }
    private void initObservers() {
        if(vmLands != null){
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandListUpdate);
            vmLands.getSelectedLands().observe(getViewLifecycleOwner(),this::onSelectedLandUpdate);
        }
        if(vmUsers != null){
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
            if(vmUsers.getCurrUser().getValue() != null){
                currUser = vmUsers.getCurrUser().getValue();
                vmLands.init(currUser);
            }
        }
    }
    private void initHolders(View view) {
        nav = new LandListNavigator(NavHostFragment.findNavController(this));
        viewHolder = new LandListRecycleViewHolder(view, vmLands, this::landClick, this::landLongClick);
        menuHolder = new LandListMenuHolder(this::OnNavigate,this::OnUpdateState,this::onAction);
    }

    private void onCurrUserUpdate(User newLoginUser) {
        this.currUser = newLoginUser;
        if( currUser != null) {
            changeActionBarTitle(currUser.getUsername()+"'s Land's");
            vmLands.init(currUser);
        }
    }
    private void onSelectedLandUpdate(List<Integer> integers) {
        viewHolder.notifyItemsChanged();
    }
    private void onLandListUpdate(List<Land> lands) {
        if(lands.size() == 0){
            
        }else{

        }
        viewHolder.notifyItemsChanged();
    }

    private void OnNavigate(LandListNavigateStates state){
        if(state == LandListNavigateStates.ToCreate && getActivity() != null){
            nav.toCreateLand(getActivity());
        }
    }

    private void OnUpdateState(LandListMenuState state) {
        menuHolder.setupMenu(state);
        if(menuHolder.getState() == LandListMenuState.NormalState)
            vmLands.deselectAllLands();
    }
    private void onAction(LandListActionState action) {
        switch (action){
            case DeleteAction:
                deleteAction();
                break;
            case ExportAction:
                exportAction();
                break;
            case SelectAllAction:
                selectAllAction();
                break;
        }
        if(action != LandListActionState.SelectAllAction)
            menuHolder.setState(LandListMenuState.NormalState);
    }

    private void landClick(int position) {
        if(menuHolder.getState() != LandListMenuState.NormalState) {
            vmLands.toggleSelectOnPosition(position);
        }else{
            if(getActivity() != null){
                Land land = vmLands.getLand(position);
                if(land != null){
                    nav.toEditLand(getActivity(),land);
                }
            }
        }
    }
    private boolean landLongClick(int position) {
        if (menuHolder.getState() == LandListMenuState.NormalState){
            menuHolder.setState(LandListMenuState.MultiSelectState);
            vmLands.toggleSelectOnPosition(position);
        }else{
            landClick(position);
        }
        return true;
    }

    private void deleteAction() {
        vmLands.removeSelectedLands(currUser);
        vmLands.deselectAllLands();
        viewHolder.notifyItemsChanged();
    }
    private void exportAction() {
        List<Integer> selectedLands = vmLands.getSelectedLands().getValue();
        if(selectedLands != null){
            List<Land> lands = vmLands.returnSelectedLands();
            writeOnFile(lands);
            vmLands.deselectAllLands();
        }
    }

    private void writeOnFile(List<Land> lands) {
        if(lands.size()>0){
            String fileName = currUser.hashCode()+"_"+(System.currentTimeMillis()/1000)+"_"+lands.size();
            try{
                File path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS);
                path.mkdirs();
                String output;
                //TODO SWITCH BASED ON SELECTION + remove mock code
                output = landsToKmlString(lands);
                fileName = fileName+".kml";
                /*
                switch(action){
                    case (kml):
                        output = landsToKmlString(lands);
                        fileName = fileName+".kml";
                        break;
                    case (geoJson):
                        output = landsToGeoJsonString(lands);
                        fileName = fileName+".json";
                        break;
                    default:
                        output = "";
                        break;
                    }
                */
                File gpxfile = new File(path, fileName);
                FileWriter writer = new FileWriter(gpxfile);
                writer.append(output);
                writer.flush();
                writer.close();
                Toast.makeText(getContext(), "File created", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "writeOnFile: ", e);
            }
        }
    }

    private void landToExportModel(List<Land> lands, List<ExportFieldModel> exportFieldModels) {
        List<Double> latLng = new ArrayList<>();
        List<List<Double>> points = new ArrayList<>();
        List<List<List<Double>>> points2 = new ArrayList<>();
        for(Land land : lands){
            points.clear();
            points2.clear();
            for(LandPoint landPoint: land.getLandPoints()){
                latLng.clear();
                latLng.add(landPoint.getLatLng().longitude);
                latLng.add(landPoint.getLatLng().latitude);
                points.add(new ArrayList<>(latLng));
            }
            points2.add(new ArrayList<>(points));
            exportFieldModels.add(new ExportFieldModel(
                    land.getLandData().getTitle(),
                    String.valueOf(land.getLandData().hashCode()),
                    points2
            ));
        }
    }

    private String landsToKmlString(List<Land> lands) {
        List<ExportFieldModel> exportFieldModels = new ArrayList<>();
        landToExportModel(lands, exportFieldModels);
        Document document = KmlFileExporter.kmlFileExporter(
                String.valueOf(currUser.hashCode()),
                exportFieldModels);
        XMLOutputter xmOut = new XMLOutputter(Format.getPrettyFormat(), XMLOUTPUT);
        String output = xmOut.outputString(document);
        return xmOut.outputString(document);
    }

    private String landsToGeoJsonString(List<Land> lands) {
        List<ExportFieldModel> exportFieldModels = new ArrayList<>();
        landToExportModel(lands, exportFieldModels);
        JSONObject export = GeoJsonExporter.geoJsonExport(exportFieldModels);;
        return export.toString();
    }

    private void selectAllAction(){
        if(vmLands.isAllSelected()){
            vmLands.deselectAllLands();
        }else{
            vmLands.selectAllLands();
        }
    }
}
