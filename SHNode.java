package java.lang;

import java.util.List;
import java.util.ArrayList;

/**
 * A node element to create trees
 * @param <T> type of the node element
 */
public class SHNode<T> {
    private final T element;
    private final List<SHNode<T>> parents;
    private final List<SHNode<T>> children;

    /**
     * Constructor taking no parents
     * @param element element of the node
     */
    public SHNode(T element){
        this.element = element;
        parents = new ArrayList<SHNode<T>>();
        children = new ArrayList<SHNode<T>>();
    }

    /**
     * Constructor taking one parent
     * @param element element of the node
     * @param parent a parent node
     */
    public SHNode(T element, SHNode<T> parent){
        this.element = element;
        parents = new ArrayList<SHNode<T>>();
        children = new ArrayList<SHNode<T>>();
        parent.addChild(this);
    }

    /**
     * Constructor taking two parents
     * @param element element of the node
     * @param parent1 a parent node
     * @param parent2 a parent node
     */
    public SHNode(T element, SHNode<T> parent1, SHNode<T> parent2){
        this.element = element;
        parents = new ArrayList<SHNode<T>>();
        parent1.addChild(this);
        parent2.addChild(this);
        children = new ArrayList<SHNode<T>>();
    }

    /**
     * Constructor taking parent List
     * @param element element of the node
     * @param parents list of parents
     */
    public SHNode(T element, List<SHNode<T>> parents){
        this.element = element;
        this.parents = new ArrayList<SHNode<T>>();
        for(SHNode<T> parent : parents)
            parent.addChild(this);
        children = new ArrayList<SHNode<T>>();
    }

    /**
     * Add a child to the node and register this node as parent
     * @param child a child node
     */
    private void addChild(SHNode<T> child){
        this.children.add(child);
        child.parents.add(this);
    }

    /**
     * Get the list of children
     * @return clone of the list of children
     */
    public List<SHNode<T>> getChildren() {
        return new ArrayList<SHNode<T>>(children);
    }

    /**
     * Get the list of parents
     * @return clone of the list of parents
     */
    public List<SHNode<T>> getParents() {
        return new ArrayList<SHNode<T>>(parents);
    }
}