package java.lang;

/**
* The {@code IStringBehavior} class represents custom behavior that can be applied to
* a String.
*
* @author  Christian Zuercher
*/
public interface IStringBehavior {
    /**
     * Custom behavior to be applied during initialization or the moment
     * the behavior is added
     * 
     * @param value bytes of the string to verify
     * @param coder encoding of the string to verify
     * @return string that should be used in further execution
     */
    default public String applyOnCreation(byte[] value, byte coder) { return new String(value, coder); };

    /**
     * Custom behavior to be applied inside the toString method
     * 
     * @param s the string to verify
     * @return string that should be used in further execution. Can also return "s" as it is
     */
    default public String applyOnRead(String s) { return s; };

    /**
     * Whether the string is inherited to its children
     * 
     * @param stt the string transformation to be checked
     * @return if the child of this transformation should inherit the behavior
     */
    public boolean inheritToChild(StringTransformType stt);

    /**
     * Whether the string should keep track of its children
     * 
     * @return if the String keeps track of its children
     */
    public boolean recordHistory();
    
    /**
     * @return representation of the behavior
     */
    public String getDescription();

    /**
     * Different types of transformations a string can go through to create a new one
     */
    public enum StringTransformType { 
        /** An entire String was used */ 
        COPY, 
        /** Something was removed from the string */
        DELETE,
        /** Anything changed in the string */ 
        REPLACE,
        /** Split the string */
        SPLIT
    }
}