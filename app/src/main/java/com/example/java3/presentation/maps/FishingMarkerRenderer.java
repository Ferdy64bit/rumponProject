package com.example.java3.presentation.maps;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.java3.domain.model.FishingClusterItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class FishingMarkerRenderer extends DefaultClusterRenderer<FishingClusterItem> {

    public FishingMarkerRenderer(Context context, GoogleMap map, ClusterManager<FishingClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull FishingClusterItem item, @NonNull MarkerOptions markerOptions) {
        String type = item.getData().getType();
        float hue = BitmapDescriptorFactory.HUE_RED;

        if (type != null) {
            switch (type.toLowerCase()) {
                case "pantai": hue = BitmapDescriptorFactory.HUE_AZURE; break;
                case "muara": hue = BitmapDescriptorFactory.HUE_CYAN; break;
                case "dermaga": hue = BitmapDescriptorFactory.HUE_BLUE; break;
                case "sungai": hue = BitmapDescriptorFactory.HUE_GREEN; break;
                case "danau": hue = BitmapDescriptorFactory.HUE_YELLOW; break;
                case "tambak": hue = BitmapDescriptorFactory.HUE_VIOLET; break;
            }
        }

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
    }
}
