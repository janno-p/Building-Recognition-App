package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.github.jannop.buildingrecognition.LocationTracker;
import com.github.jannop.buildingrecognition.R;
import com.github.jannop.buildingrecognition.tasks.BuildingInfoListener;
import com.github.jannop.buildingrecognition.tasks.DetectBuildingTask;
import com.github.jannop.buildingrecognition.tasks.DetectLocationTask;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class ShowLocationActivity extends Activity implements BuildingInfoListener {
    //private final GeoPoint testLocation = new GeoPoint(59.402474, 24.69505);
    //private final GeoPoint testLocation = new GeoPoint(59.40298, 24.69381);
    //private final GeoPoint testLocation = new GeoPoint(59.40364, 24.69097);

    private TextView txtLocation;
    private MapView mapView;
    private ProgressDialog progressDialog;
    private Location location;

    private boolean selectionModeEnabled = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);

        txtLocation = (TextView)findViewById(R.id.txtLocation);

        mapView = (MapView)findViewById(R.id.mapView);
        mapView.getController().setZoom(16);
        mapView.setMultiTouchControls(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Detecting location");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        new DetectLocationTask(this).execute();
    }

    public void showMenu(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selected Building");
        builder.setItems(R.array.show_location_menu_items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(getApplicationContext(), EditLocationActivity.class);
                    startActivity(intent);
                }
                if (which == 2) {
                    selectionModeEnabled = true;
                    // TODO : Enable location selection
                }
            }
        });
        builder.setInverseBackgroundForced(true);
        builder.create();
        builder.show();
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

    @Override
    public void updateAddress(String address) {
        Log.d("#########UPDATEADDRESS", "#############UPDATEADDRESS");
        txtLocation.setText("Current Location: " + (address == null || address.isEmpty() ? "Unknown" : address));
    }

    @Override
    public void updateMap(GeoPoint location, ArrayList<GeoPoint> building) {
        Log.d("#########UPDATEMAP", "#############UPDATEMAP");
        mapView.getOverlays().clear();

        if (building != null && building.size() > 0) {
            Polygon path = new Polygon(this);
            path.setFillColor(Color.argb(164, 0, 255, 0));
            path.setStrokeColor(Color.argb(255, 0, 128, 0));
            path.setStrokeWidth(2);

            for (GeoPoint point : building) {
                path.addPoint(point.getLatitudeE6(), point.getLongitudeE6());
            }

            path.addPoint(building.get(0).getLatitudeE6(), building.get(0).getLongitudeE6());

            mapView.getOverlays().add(path);
        }
        Log.d("#########ANIMATETO", "#############ANIMATETO");
        if (location != null) {
            mapView.getController().animateTo(location);

            OverlayItem myLocationOverlayItem = new OverlayItem("Here", "Current Position", location);
            Drawable myCurrentLocationMarker = this.getResources().getDrawable(R.drawable.map_marker);
            myLocationOverlayItem.setMarker(myCurrentLocationMarker);
            myLocationOverlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);

            final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
            items.add(myLocationOverlayItem);

            ItemizedIconOverlay<OverlayItem> currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(
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
            this.mapView.getOverlays().add(currentLocationOverlay);
        }

        Log.d("#########DISMISS", "#############DISMISS");
        progressDialog.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

    public void setLocation(Location location) {
        this.location = location;
        progressDialog.setMessage("Identifying current building");
        new DetectBuildingTask(this).execute(new GeoPoint(location));
    }
}
