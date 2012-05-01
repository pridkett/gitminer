package net.wagstrom.research.github.v3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventServiceV3 extends EventService {
    private static final Logger log = LoggerFactory.getLogger(EventService.class); // NOPMD
    
    public EventServiceV3(final IGitHubClient ghc) {
        super(ghc);
    }

    protected <V> List<V> getAll(final PageIterator<V> iterator) {
        List<V> elements = new ArrayList<V>();
        Collection<V> elements2 = null;
        try {
            while (iterator.hasNext()) {
                elements2 = iterator.next();
                elements.addAll(elements2);
            }
        } catch (NoSuchPageException pageException) {
            log.error("NoSuchPageException caught: ", pageException);
        }
        return elements;
    }
    
    public List<Event> getUserEvents(final String user) {
        List<Event> events = null;
            log.warn("Getting all user events for {}", user);
            events = getAll(pageUserEvents(user));
        return events;
    }
}
