package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.github.jannop.buildingrecognition.LocationTracker;
import com.github.jannop.buildingrecognition.R;
import com.github.jannop.buildingrecognition.SelectLocationActivity;
import com.github.jannop.buildingrecognition.tasks.BuildingInfoListener;
import com.github.jannop.buildingrecognition.tasks.DetectBuildingTask;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);
        txtLocation = (TextView)findViewById(R.id.txtLocation);
        mapView = (MapView)findViewById(R.id.mapView);
        mapView.getController().setZoom(16);
        mapView.setMultiTouchControls(true);
        LocationTracker tracker = new LocationTracker(this);
        if (tracker.isLocationAcquirable()) {
            new DetectBuildingTask(this).execute(tracker.getLocation());
        } else {
            tracker.showSettingsAlert();
        }
    }

    public void showMenu(View view) {
        //Intent intent = new Intent(getApplicationContext(), SelectLocationActivity.class);
        //startActivity(intent);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selected Building");
        builder.setItems(R.array.show_location_menu_items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
            }
        });
        builder.setInverseBackgroundForced(true);
        builder.create();
        builder.show();
    }

    @Override
    public void updateAddress(String address) {
        txtLocation.setText("Current Location: " + (address == null || address.isEmpty() ? "Unknown" : address));
    }

    @Override
    public void updateMap(GeoPoint location, ArrayList<GeoPoint> building) {
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
    }
}
