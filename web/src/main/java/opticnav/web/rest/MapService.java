package opticnav.web.rest;

import java.util.LinkedList;
import java.util.List;

import opticnav.persistence.web.AccountBroker;
import opticnav.persistence.web.map.Anchor;
import opticnav.persistence.web.map.GetMap;
import opticnav.persistence.web.map.MapsListEntry;
import opticnav.persistence.web.map.Marker;
import opticnav.persistence.web.map.ModifyMap;
import opticnav.web.components.UserSession;
import opticnav.web.rest.pojo.map.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
public class MapService extends Controller {
    @Autowired
    private javax.sql.DataSource dbDataSource;
    
    @Autowired
    private UserSession userSession;
    
    @RequestMapping(value="/{id}", method=RequestMethod.GET)
    public GetMapPOJO getMap(@PathVariable("id") int id) throws Exception {
        try (AccountBroker broker = new AccountBroker(dbDataSource, userSession.getUser().getId())) {
            GetMap map = broker.getMap(id);
            return new GetMapPOJO(map);
        }
    }
    
    @RequestMapping(method=RequestMethod.GET)
    public List<GetMapsListEntryPOJO> getMapsList() throws Exception {
        try (AccountBroker broker = new AccountBroker(dbDataSource, userSession.getUser().getId())) {
            List<MapsListEntry> mapsList = broker.getMapsList();
            List<GetMapsListEntryPOJO> list = new LinkedList<>();
            
            for (MapsListEntry entry: mapsList) {
                GetMapsListEntryPOJO item = new GetMapsListEntryPOJO();
                item.name = entry.getName();
                item.id = entry.getId();
                list.add(item);
            }
            
            return list;
        }
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public void createMap(@RequestBody CreateMapPOJO map) throws Exception {
        try (AccountBroker broker = new AccountBroker(dbDataSource, userSession.getUser().getId())) {
            broker.createMap(map.name, map.image_resource);
        }
    }

    @RequestMapping(value="/{id}", method=RequestMethod.PUT)
    public void modifyMap(@PathVariable("id") int id,
            @RequestBody ModifyMapPOJO map) throws Exception {
        try (AccountBroker broker = new AccountBroker(dbDataSource, userSession.getUser().getId())) {
            ModifyMap pMap = new ModifyMap();
            for (MarkerPOJO m: map.markers) {
                // TODO - resource
                Marker marker = new Marker(m.lng, m.lat, m.name, 0);
                pMap.addMarker(marker);
            }
            for (AnchorPOJO a: map.anchors) {
                Anchor anchor = new Anchor(a.lng, a.lat, a.localX, a.localY);
                pMap.addAnchor(anchor);
            }
            broker.modifyMap(id, pMap);
        }
    }

    @RequestMapping(value="/{id}", method=RequestMethod.DELETE)
    public void deleteMap(@PathVariable("id") int id) throws Exception {
        try (AccountBroker broker = new AccountBroker(dbDataSource, userSession.getUser().getId())) {
            broker.deleteMap(id);
        }
    }
}
