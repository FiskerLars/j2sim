package de.postsim.ContactGraph;

import de.postsim.Objects.Coordinate;
import de.postsim.Objects.User;

import java.util.Iterator;
import java.util.List;

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


}
