package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.R;
import com.github.jannop.buildingrecognition.tasks.DetectBuildingTask;
import com.github.jannop.buildingrecognition.tasks.DetectLocationTask;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class ShowLocationActivity extends Activity {
    //private final GeoPoint testLocation = new GeoPoint(59.402474, 24.69505);
    //private final GeoPoint testLocation = new GeoPoint(59.40298, 24.69381);
    //private final GeoPoint testLocation = new GeoPoint(59.40364, 24.69097);

    private TextView txtLocation;
    private MapView mapView;
    private ProgressDialog progressDialog;
    private Button btnReady;

    private GeoPoint currentLocation;
    private BuildingDetails currentBuilding;

    private GeoPoint selectedLocation;
    private BuildingDetails selectedBuilding;

    private boolean selectionModeEnabled = false;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_show_location);

        txtLocation = (TextView)findViewById(R.id.txtLocation);

        mapView = (MapView)findViewById(R.id.mapView);
        mapView.getController().setZoom(16);
        mapView.setMultiTouchControls(true);

        btnReady = (Button)findViewById(R.id.btnReady);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        if (state == null) {
            progressDialog.setMessage("Detecting location");
            progressDialog.show();
            new DetectLocationTask(this).execute();
        } else {
            currentLocation = (GeoPoint)state.getSerializable("currentLocation");
            currentBuilding = (BuildingDetails)state.getSerializable("currentBuilding");
            selectedLocation = (GeoPoint)state.getSerializable("selectedLocation");
            selectedBuilding = (BuildingDetails)state.getSerializable("selectedBuilding");
            selectionModeEnabled = state.getBoolean("selectionModeEnabled");
            refreshView();
            moveToActiveLocation();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("currentLocation", currentLocation);
        state.putSerializable("currentBuilding", currentBuilding);
        state.putSerializable("selectedLocation", selectedLocation);
        state.putSerializable("selectedBuilding", selectedBuilding);
        state.putSerializable("selectionModeEnabled", selectionModeEnabled);
    }

    public void showMenu(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (selectionModeEnabled) {
            createSelectionMenu(builder);
        } else {
            createBuildingMenu(builder);
        }
        builder.setInverseBackgroundForced(true);
        builder.create();
        builder.show();
    }

    private void createBuildingMenu(AlertDialog.Builder builder) {
        builder.setTitle("Selected Building");
        builder.setItems(R.array.show_location_menu_items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0 && currentBuilding != null) {
                    Intent intent = new Intent(getApplicationContext(), EditLocationActivity.class);
                    intent.putExtra("building", currentBuilding);
                    startActivity(intent);
                }
                if (which == 2) {
                    selectionModeEnabled = true;
                    refreshView();
                }
            }
        });
    }

    private void createSelectionMenu(AlertDialog.Builder builder) {
        builder.setTitle("Selecting Building");
        builder.setItems(R.array.select_location_menu_items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (selectionModeEnabled) {
            selectionModeEnabled = false;
            refreshView();
            moveToActiveLocation();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                showMenu(null);
                return true;
        }

        return super.onKeyDown(keycode, e);
    }

    public void completeLocationDetection(GeoPoint location) {
        if (selectionModeEnabled) {
            selectedLocation = location;
        } else {
            currentLocation = location;
        }

        if (location != null) {
            identifyBuilding(location);
        } else {
            completeBuildingDetection(null);
        }
    }

    public void completeBuildingDetection(BuildingDetails building) {
        progressDialog.dismiss();

        if (selectionModeEnabled) {
            selectedBuilding = building;
        } else {
            currentBuilding = building;
        }

        refreshView();
        moveToActiveLocation();
    }

    private void moveToActiveLocation() {
        GeoPoint activeLocation = selectionModeEnabled ? selectedLocation : currentLocation;
        if (activeLocation != null) {
            mapView.getController().animateTo(activeLocation);
        }
    }

    private void refreshView() {
        GeoPoint location = selectionModeEnabled ? selectedLocation : currentLocation;
        BuildingDetails building = selectionModeEnabled ? selectedBuilding : currentBuilding;

        mapView.getOverlays().clear();

        setAddress(building);
        setBuildingMarker(building);
        setLocationMarker(location != null ? location : currentLocation);

        if (selectionModeEnabled) {
            addGestureOverlay();
        }

        mapView.invalidate();

        btnReady.setVisibility(selectionModeEnabled ? View.VISIBLE : View.INVISIBLE);
        btnReady.setEnabled(selectionModeEnabled && selectedBuilding != null);
    }

    private void setAddress(BuildingDetails building) {
        String address = "Not inside a building";
        if (building != null && building.address != null && !building.address.isEmpty()) {
            address = building.address;
        }

        String label = selectionModeEnabled ? "Selected" : "Current";
        txtLocation.setText(label + " Location: " + address);
    }

    private void setLocationMarker(GeoPoint location) {
        if (location == null) {
            return;
        }

        OverlayItem touchOverlayItem = new OverlayItem("Here", "Current Position", location);
        Drawable touchLocationMarker = this.getResources().getDrawable(R.drawable.map_marker);
        touchOverlayItem.setMarker(touchLocationMarker);
        touchOverlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);

        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(touchOverlayItem);

        ItemizedIconOverlay<OverlayItem> touchLocationOverlay = new ItemizedIconOverlay<OverlayItem>(
                this,
                items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                });

        mapView.getOverlays().add(touchLocationOverlay);
    }

    private void setBuildingMarker(BuildingDetails building) {
        if (building != null && building.polygon != null && building.polygon.size() > 0) {
            Polygon path = new Polygon(this);
            path.setFillColor(Color.argb(164, 0, 255, 0));
            path.setStrokeColor(Color.argb(255, 0, 128, 0));
            path.setStrokeWidth(2);

            GeoPoint first = building.polygon.get(0);
            for (GeoPoint point : building.polygon) {
                path.addPoint(point.getLatitudeE6(), point.getLongitudeE6());
            }
            path.addPoint(first.getLatitudeE6(), first.getLongitudeE6());

            mapView.getOverlays().add(path);
        }
    }

    private void addGestureOverlay() {
        MapEventsOverlay eventsOverlay = new MapEventsOverlay(this, new MyMapEventsReceiver());
        mapView.getOverlays().add(eventsOverlay);
        mapView.invalidate();
    }

    private class MyMapEventsReceiver implements MapEventsReceiver {
        @Override
        public boolean singleTapUpHelper(IGeoPoint location) {
            return false;
        }

        @Override
        public boolean longPressHelper(IGeoPoint location) {
            selectedLocation = (GeoPoint)location;
            identifyBuilding(selectedLocation);
            refreshView();
            return false;
        }
    }

    private void identifyBuilding(GeoPoint location) {
        progressDialog.setMessage("Identifying current building");
        progressDialog.show();
        new DetectBuildingTask(this).execute(location);
    }
}
