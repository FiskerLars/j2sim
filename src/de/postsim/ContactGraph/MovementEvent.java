package de.postsim.ContactGraph;

import de.postsim.Objects.User;
import org.jgrapht.ext.ComponentAttributeProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 29/07/15.
 */
public class MovementEvent extends CGVertex {

    private final User user;
    private final ContactEvent src;
    private final ContactEvent dst;

    public MovementEvent(User user, ContactEvent src, ContactEvent dst) {
        super();
        this.user = user;
        this.src = src;
        this.dst = dst;
    }

    public Map<String, String> getComponentAttributes() {
        Map<String,String> attrs = new HashMap<String, String>();
        attrs.put("user",Integer.toString(this.user.getUsernumber()));
        attrs.put("src",src.toStringID());
        attrs.put("dst",dst.toStringID());
        return attrs;
    }


}
