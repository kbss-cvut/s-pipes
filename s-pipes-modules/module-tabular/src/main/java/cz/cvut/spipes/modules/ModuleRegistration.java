package cz.cvut.spipes.modules;

import cz.cvut.spipes.modules.handlers.HandlerRegistry;
import cz.cvut.spipes.modules.handlers.ModeHandler;

public class ModuleRegistration {

    public static void register(){
        HandlerRegistry.getInstance().registerHandler(Mode.class, ModeHandler.class);
    }

}
