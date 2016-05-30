/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.sempipes.cli;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 *
 * @author blcha
 */
public class SubCommandOptionHandler <T extends SubCommand> extends OptionHandler<T> {

    private final Class<T> enumType;
    
//    public SubCommandOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super T> setter, Class<T> enumType) {
//        super(parser, option, setter);        
//        this.enumType = enumType;
//    }
    
    public SubCommandOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super T> setter) {
        super(parser, option, setter);        
        this.enumType = (Class<T>) SubCommand.class;
    }


    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        String s = params.getParameter(0);
        T value = null;
        for( T o : enumType.getEnumConstants() )
            if(o.toString().equalsIgnoreCase(s)) {
                value = o;
                break;
            }

        if(value==null)
            //
            throw new CmdLineException(owner, String.format("\n\"%s\" is not a valid value for \"%s\"", s, option.toString()));
        setter.addValue(value);
        return 1;
    }
    
    /* 
     * Returns all values of an enum type split by pipe.
     * <tt>[ one | two | three]</tt>
     * @see org.kohsuke.args4j.spi.OptionHandler#getDefaultMetaVariable()
     */
    @Override
    public String getDefaultMetaVariable() {
    	StringBuffer rv = new StringBuffer();
    	rv.append("[");
    	for (T t : enumType.getEnumConstants()) {
			rv.append(t.toString()).append(" | ");
		}
    	rv.delete(rv.length()-3, rv.length());
    	rv.append("]");
    	return rv.toString();
    }
    
    
}