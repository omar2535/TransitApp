package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import android.graphics.Canvas;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.*;
import java.util.regex.Pattern;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /** overlay used to display bus route legend text on a layer above the map */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /** overlays used to plot bus routes */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     * @param context   the application context
     * @param mapView   the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {

        updateVisibleArea();
        busRouteLegendOverlay.clear();
        busRouteOverlays.clear();

        Stop stop = StopManager.getInstance().getSelected();
        float width = getLineWidth(zoomLevel);

        if(stop != null) {
            Set<Route> routeSet = stop.getRoutes();

            Iterator routeIterator = routeSet.iterator();
            while (routeIterator.hasNext()) {

                Route currentRoute = (Route) routeIterator.next();
                busRouteLegendOverlay.add(currentRoute.getNumber());
                int color = busRouteLegendOverlay.getColor(currentRoute.getNumber());
                List<RoutePattern> patterns = currentRoute.getPatterns();


                for(RoutePattern p: patterns){


                    List<LatLon> paths = p.getPath();


                    for(int i=0; i<paths.size(); i++) {
                        List<GeoPoint> points = new LinkedList<>();

                        LatLon latLon = paths.get(i);
                        LatLon previousLatLon = null;
                        if(i>=1) {
                            previousLatLon = paths.get(i - 1);
                        }

                        if (Geometry.rectangleContainsPoint(northWest, southEast, latLon)) {


                            if (!points.contains(Geometry.gpFromLL(latLon))) {
                                if(previousLatLon != null && Geometry.rectangleIntersectsLine(northWest, southEast, previousLatLon, latLon)) {
                                    points.add(Geometry.gpFromLL(latLon));
                                    points.add(Geometry.gpFromLL(previousLatLon));
                                }

                            }
                            Polyline line = new Polyline(context);
                            line.setColor(color);
                            line.setWidth(width);
                            line.setPoints(points);
                            line.setVisible(true);
                            busRouteOverlays.add(line);
                        }
                    }

                }

            }
        }



    }

    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 7.0f * BusesAreUs.dpiFactor();
        else if(zoomLevel > 10)
            return 5.0f * BusesAreUs.dpiFactor();
        else
            return 2.0f * BusesAreUs.dpiFactor();
    }
}
