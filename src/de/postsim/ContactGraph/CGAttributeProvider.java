package de.postsim.ContactGraph;

import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 07/08/15.
 */
public class CGAttributeProvider implements VertexNameProvider, EdgeNameProvider, ComponentAttributeProvider {
    @Override
    public Map<String, String> getComponentAttributes(Object o) {
        if (o instanceof ContactEvent)
            return ((ContactEvent) o).getComponentAttributes();
        else if (o instanceof MovementEvent)
            return ((MovementEvent) o).getComponentAttributes();
        else if (o instanceof CGEdge)
            return new HashMap<>();
        throw new RuntimeException("No Component Attributes given for "+ o.toString());
    }

    @Override
    public String getEdgeName(Object o) {
        if (o instanceof CGElement)
            return ((CGElement) o).toStringID();
        throw new RuntimeException("No Edge Name given for "+ o.toString());
    }

    @Override
    public String getVertexName(Object o) {
        if (o instanceof CGElement)
            return ((CGElement) o).toStringID();
        throw new RuntimeException("No Vertex Name given for "+ o.toString());
    }
}
