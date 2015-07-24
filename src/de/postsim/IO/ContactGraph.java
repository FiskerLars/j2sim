package de.postsim.IO;

import de.postsim.Objects.Coordinate;
import de.postsim.Objects.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.ext.GraphMLExporter;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
//import org.jgrapht.traverse.*;



/**
 * Created by lars on 24/07/15.
 * Model the Contact Graph
 */
public class ContactGraph implements Serializable  {

    private List<ContactEvent> contactEvents = new ArrayList<ContactEvent>();
    private List<MovementEdge> movement = new ArrayList<MovementEdge>();

    private Graph<ContactEvent,MovementEdge> graph;


    public ContactGraph(){
        graph = new DirectedMultigraph<ContactEvent, MovementEdge>(MovementEdge.class);
    };



    private void writeGraphML(FileWriter file)
            throws IOException
    {
        GraphMLExporter<ContactEvent,MovementEdge> export = new GraphMLExporter<ContactEvent,MovementEdge>();
        try {
            export.export(file, graph);
        } catch (SAXException e) {
            throw new IOException(e);
        } catch (TransformerConfigurationException e) {
            throw new IOException(e);
        }
    }

    public ContactEvent newContact(List<User> users, Coordinate coord,
                            long startTime, long endTime) {
        ContactEvent contact = new ContactEvent(users, coord, startTime, endTime);
        contactEvents.add(contact);
        graph.addVertex(contact);
        return contact;
    }

    public MovementEdge newMovement(User user, ContactEvent src, ContactEvent dst) {
        MovementEdge move = new MovementEdge(user, src, dst);
        movement.add(move);
        graph.addEdge(src, dst, move);
        return move;
    }

    /**
     * Insert the contact between two users either to a fitting existing contact event or create a new event.
     * @param u
     * @param u2
     * @param time
     */
    public void addMutualUserContact(User u, User u2, long time) {
        Coordinate coord = u.getPosition();
        List<User> users = new ArrayList<User>();
        users.add(u);
        users.add(u2);
        ContactEvent contact = newContact(users, coord, time, time + 1);

        ContactEvent prev_u = getPreviousContact(u, time);
        ContactEvent prev_u2 = getPreviousContact(u2, time);

        this.contactEvents.add(contact);
        // Todo: make ordered contact event list

        MovementEdge mov_u = newMovement(u, prev_u, contact);
        MovementEdge mov_u2 = newMovement(u2, prev_u2, contact);
        this.movement.add(mov_u);
        this.movement.add(mov_u2);

        graph.addVertex(contact);
        graph.addEdge(prev_u, contact, mov_u);
        graph.addEdge(prev_u2, contact, mov_u2);
        //Todo: get interval time
        //Todo: merge with other meetings
    }


        private ContactEvent getPreviousContact(User user, long time)
        {
            //FIXME: currently not ordered
            return this.contactEvents.get(1);

        }

    public class ContactEvent{

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
    }
    public class MovementEdge{

        private User user;
        private ContactEvent src;
        private ContactEvent dst;

        public MovementEdge(User user, ContactEvent src, ContactEvent dst) {
            this.user = user;
            this.src = src;
            this.dst = dst;
        }
    }



}
