package cache.replacement;

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

public class HashQueue<K> {

    private Node<K> head;
    private Map<K, Node<K>> map;

    public HashQueue(Map<K, Node<K>> map, Node<K> head) {
        this.map = map;
        this.head = head;
    }

    public HashQueue() {
        map = new HashMap<>();
        head = new Node<>();
        head.setNextNode(head);
        head.setPreviousNode(head);
    }

    public Node<K> popFirst() {
        Node<K> top = head.getNextNode();
        removeNode(map.remove(top.getId()));
        return top;
    }

    public Node<K> popLast() {
        Node<K> bottom = head.getPreviousNode();
        removeNode(map.remove(bottom.getId()));
        return bottom;
    }

    public void remove(Object key) {
        removeNode(map.remove(key));
    }

    public void push(K key) {
        Node<K> node = new Node<>();
        node.setId(key);
        pushNode(node);
    }

    public boolean isEmpty() {
        return head.getNextNode() == head;
    }

    private synchronized void pushNode(Node<K> pushed) {
        map.put(pushed.getId(), pushed);
        Node<K> last = head.getPreviousNode();
        pushed.setNextNode(head);
        pushed.setPreviousNode(last);
        head.setPreviousNode(pushed);
        last.setNextNode(pushed);
    }

    private synchronized void removeNode(Node<K> removed) {
        if (nonNull(removed)) {
            Node<K> next = removed.getNextNode();
            Node<K> previous = removed.getPreviousNode();
            next.setPreviousNode(previous);
            previous.setNextNode(next);
        }
    }

    public static class Node<K> {
        private K id;
        private Node<K> previousNode;
        private Node<K> nextNode;

        public K getId() {
            return id;
        }

        public void setId(K id) {
            this.id = id;
        }

        public Node<K> getPreviousNode() {
            return previousNode;
        }

        public void setPreviousNode(Node<K> previousNode) {
            this.previousNode = previousNode;
        }

        public Node<K> getNextNode() {
            return nextNode;
        }

        public void setNextNode(Node<K> nextNode) {
            this.nextNode = nextNode;
        }
    }
}
