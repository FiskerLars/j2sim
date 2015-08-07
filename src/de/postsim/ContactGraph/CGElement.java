package de.postsim.ContactGraph;

/**
 * Created by lars on 07/08/15.
 */
public class CGElement {
    private Integer id;
    private static Integer lastID = 0;

    public CGElement(){
        lastID++;
        this.id = lastID;
    }

    public Integer getId() {
        return id;
    }

    public String toStringID() {
        return getId().toString();
    }

}
