package opticnav.persistence.web;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Map {
    private LinkedList<Marker> markers;
    private LinkedList<Anchor> anchors;
    
    public Map() {
        this.markers = new LinkedList<Marker>();
        this.anchors = new LinkedList<Anchor>();
    }
    
    public void addMarker(Marker marker){
        this.markers.add(marker);
    }
    public void addAnchor(Anchor anchor){
        this.anchors.add(anchor);
    }

    public List<Marker> getMarkers() {
        return Collections.unmodifiableList(markers);
    }

    public List<Anchor> getAnchors() {
        return Collections.unmodifiableList(anchors);
    }
    
    
}
