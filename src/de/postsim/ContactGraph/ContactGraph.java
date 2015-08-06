package de.postsim.ContactGraph;

import de.postsim.Objects.Coordinate;
import de.postsim.Objects.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.*;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.DOTExporter;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
//import org.jgrapht.traverse.*;



/**
 * Created by lars on 24/07/15.
 * Model the Contact Graph
S */
public class ContactGraph implements Serializable  {
    private static final Logger log = Logger.getLogger(ContactGraph.class.getName());


    private HashMap<User, Stack<ContactEvent>> contactEvents = new HashMap<User,Stack<ContactEvent>>();
    private List<MovementEvent> movement = new ArrayList<MovementEvent>();

    private Graph<CGVertex,CGEdge> graph;


    public ContactGraph(){
        log.fine("Contact Graph initiated");
        graph = new DirectedAcyclicGraph<CGVertex, CGEdge>(CGEdge.class);
        log.setLevel(Level.ALL);
    }

    /** Write to file using default writer.
     *
     * @param basename
     */
    public void writeGraph(String basename) throws IOException {
        File graphfile = new File(basename+".dot");
        if(graphfile.exists()) {
            int i = 0;
            while (graphfile.exists()) {
                i++;
                graphfile = new File(basename+ "-" + i + ".xml");
            }
        }
        this.writeDOT(new FileWriter(graphfile));
    }


    private void writeDOT(FileWriter file)
            throws IOException
    {
        DOTExporter<CGVertex,CGEdge> exporter = new DOTExporter<>();
        exporter.export(file, graph);
    }


    private void writeGraphML(FileWriter file)
            throws IOException
    {
        GraphMLExporter<CGVertex,CGEdge> export = new GraphMLExporter<CGVertex,CGEdge>();
        try {
            export.export(file, graph);
        } catch (SAXException e) {
            throw new IOException(e);
        } catch (TransformerConfigurationException e) {
            throw new IOException(e);
        }
    }



    private ContactEvent newContact(List<User> users, Coordinate coord,
                            long startTime, long endTime) {
        ContactEvent contact = new ContactEvent(users, coord, startTime, endTime);
        addNewContacts(users, contact);
        graph.addVertex(contact);
        return contact;
    }

    private MovementEvent newMovement(User user, ContactEvent src, ContactEvent dst) {
        MovementEvent move = new MovementEvent(user, src, dst);
        movement.add(move);
        log.fine(move.toString());
        graph.addVertex(move);
        log.fine("newMovement inserting movement between " + src.toShortString() + " to " + dst.toShortString());
      //FIXME: vertice not in graph
        log.fine("addEgde([" + dst.toShortString() + "], [" + move.toString() + "])");
        graph.addEdge(src, move);
        log.fine("addEgde(["+move.toString()+"], [" + dst.toShortString()+"])");
        graph.addEdge(move, dst);
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
        ContactEvent prev_u = getPreviousContact(u, time);
        ContactEvent prev_u2 = getPreviousContact(u2, time);

        // check if it is still the same contact
        if (null!= prev_u && prev_u == prev_u2) {
            ContactEvent contact = prev_u;
            contact.setEndTime(time);
        } else {
            // Create new contact event

            ContactEvent contact = newContact(users, coord, time, time + 1);

            graph.addVertex(contact);
            addNewContacts(users, contact);
            // Todo: make ordered contact event list

            if (null != prev_u) {
                MovementEvent mov_u = newMovement(u, prev_u, contact);
                this.movement.add(mov_u);
            }
            if (null != prev_u2) {
                MovementEvent mov_u2 = newMovement(u2, prev_u2, contact);
                this.movement.add(mov_u2);
            }
        }
       /* graph.addEdge(prev_u, mov_u);
        graph.addEdge(mov_u, contact);
        graph.addEdge(prev_u2, mov_u2);
        graph.addEdge(mov_u2, contact);
        */
    }


    private void addNewContacts(List<User> users, ContactEvent contact) {
        Iterator<User> it = users.iterator();
        while(it.hasNext()) {
            User u = it.next();
            if(contactEvents.containsKey(u))
                contactEvents.get(u).push(contact);
            else {
                Stack<ContactEvent> l = new Stack<>();
                l.push(contact);
                contactEvents.put(u, l);
            }
        }
    }

        private ContactEvent getPreviousContact(User user, long time)
        {
            Stack<ContactEvent> userEvents = contactEvents.get(user);
            if(null != userEvents){
              return userEvents.peek();
            } else
                return null;
        }

}
