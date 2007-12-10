// AbstractValue.java

package jmri.jmrit.symbolicprog;

import java.awt.Color;

/**
 * Define common base class methods for CvValue and VariableValue classes
 * <P>
 * The ToRead parameter (boolean, unbound) is used to remember whether
 * this object has been read during a "read all" operation.  This allows
 * removal of duplicate operations.
 * <P>
 * The ToWrite parameter (boolean, unbound) is used to remember whether
 * this object has been read during a "write all" operation.  This allows
 * removal of duplicate operations.
 *
 * Description:		Represents a single CV value
 * @author			Bob Jacobsen   Copyright (C) 2001, 2005
 * @version			$Revision: 1.8 $
 */
public abstract class AbstractValue {

    // method to handle color changes for states
    abstract void setColor(Color c);

    /** Defines state when nothing is known about the real value */
    public static final int UNKNOWN  =   0;

    /** Defines state where value has been edited, no longer same as in decoder or file */
    public static final int EDITED  =   4;

    /** Defines state where value has been read from (hence same as) decoder, but perhaps
        not same as in file */
    public static final int READ     =  16;

    /** Defines state where value has been written to (hence same as) decoder, but perhaps
        not same as in file */
    public static final int STORED   =  64;

    /** Defines state where value was read from a config file, but might not be
        the same as the decoder */
    public static final int FROMFILE = 256;
    
    /** Defines state where value was read from a config file, and is
    the same as the decoder */
    public static final int SAME = 512;
    
    /** Defines state where value was read from a config file, and is
    the not the same as the decoder */
    public static final int DIFF = 1024;

    /** Define color to denote UNKNOWN state.  null means to use default for the component */
    static final Color COLOR_UNKNOWN  = Color.red.brighter();

    /** Define color to denote EDITED state.  null means to use default for the component */
    static final Color COLOR_EDITED  = Color.orange;

    /** Define color to denote READ state.  null means to use default for the component */
    static final Color COLOR_READ     = null;

    /** Define color to denote STORED state.  null means to use default for the component */
    static final Color COLOR_STORED   = null;

    /** Define color to denote FROMFILE state.  null means to use default for the component */
    static final Color COLOR_FROMFILE = Color.yellow;
    
    /** Define color to denote SAME state.  null means to use default for the component */
    static final Color COLOR_SAME     = null;
    
    /** Define color to denote DIFF state.  null means to use default for the component */
    static final Color COLOR_DIFF = Color.red.brighter();

    public void setToRead(boolean state) {
        _toRead = state;
    }
    public boolean isToRead() { return _toRead; }
    private boolean _toRead = false;

    public void setToWrite(boolean state) {
        _toWrite = state;
    }
    public boolean isToWrite() { return _toWrite; }
    private boolean _toWrite = false;

    public static String stateNameFromValue(int val) {
        switch (val) {
            case UNKNOWN:
                return "Unknown";
            case EDITED:
                return "Edited";
            case READ:
                return "Read";
            case STORED:
                return "Stored";
            case FROMFILE:
                return "FromFile";
            case SAME:
                return "Same";
            case DIFF:
                return "Different";
            default:
                return "<unexpected value: "+val+">";
        }
    }
}
