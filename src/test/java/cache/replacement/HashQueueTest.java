package cache.replacement;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import cache.replacement.HashQueue.Node;

@RunWith(MockitoJUnitRunner.class)
public class HashQueueTest {

    @Mock
    private Node<String> head;

    @Mock
    private Map<String, Node<String>> map;

    //Do not inject mocks because Mockito doesn't know which to inject
    private HashQueue<String> hashQueue;

    @Mock
    private Node<String> here;

    @Mock
    private Node<String> next;

    @Mock
    private Node<String> prev;
    
    @Before
    public void setup() {
        hashQueue = new HashQueue<>(map, head);
    }
    
    @Test
    public void testRemove() {
        String key = "123";

        when(map.remove(key)).thenReturn(here);
        when(here.getNextNode()).thenReturn(next);
        when(here.getPreviousNode()).thenReturn(prev);

        hashQueue.remove(key);

        verify(map, times(1)).remove(key);
        verify(next, times(1)).setPreviousNode(prev);
        verify(prev, times(1)).setNextNode(next);
    }

    @Test
    public void testRemove_null() {
        String key = "123";

        when(map.remove(key)).thenReturn(null);

        hashQueue.remove(key);

        verify(map, times(1)).remove(key);
        verify(next, times(0)).setPreviousNode(prev);
        verify(prev, times(0)).setNextNode(next);
    }


    @Test
    public void testPush() {
        when(head.getPreviousNode()).thenReturn(head);

        String key = "123";
        hashQueue.push(key);

        verify(map, times(1)).put(anyString(), ArgumentMatchers.<Node<String>>any());
        verify(head, times(1)).setPreviousNode(ArgumentMatchers.<Node<String>>any());
        verify(head, times(1)).setNextNode(ArgumentMatchers.<Node<String>>any());
    }
}
