package de.postsim.ContactGraph;

import com.sun.deploy.util.StringUtils;
import de.postsim.Objects.Coordinate;
import de.postsim.Objects.User;
import org.jgrapht.ext.ComponentAttributeProvider;

import java.util.*;

/**
 * Created by lars on 29/07/15.
 */
public class ContactEvent extends CGVertex {

    private List<User> users;
    private Coordinate coord;
    private long startTime;
    private long endTime;



    public ContactEvent(List<User> users, Coordinate coord,
                        long startTime, long endTime) {
        super();
        this.users = users;
        this.coord = coord;
        this.startTime = startTime;
        this.endTime = endTime;

    }

    public String toString() {
        return "Contact: " + users.toString() + " " + coord.toString() + " " + startTime + "-" + endTime;
    }
    public String toShortString() {
        String res = new String();
        Iterator<User> it = users.iterator();
        res += new Integer(it.next().getUsernumber()).toString();
        while (it.hasNext()) {
            res += ", ";
            res += new Integer(it.next().getUsernumber()).toString();
        }
        res += " " + startTime;
        return res;
    }

    public Coordinate getCoord() {
        return coord;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<User> getUsers() {
        return users;
    }


    public Map<String, String> getComponentAttributes() {
        Map<String,String> attrs = new HashMap<String, String>();
        attrs.put("start", Long.toString(this.getStartTime()));
        attrs.put("end", Long.toString(this.getEndTime()));
        attrs.put("pos", this.getCoord().toString());
        Collection<String> userIDs = new ArrayList<>();
        for (User user: this.getUsers())
            userIDs.add(Integer.toString(user.getUsernumber()));
        attrs.put("users", StringUtils.join(userIDs, ","));
        return attrs;
    }

}
