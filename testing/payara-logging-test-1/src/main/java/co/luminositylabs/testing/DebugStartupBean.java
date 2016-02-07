package co.luminositylabs.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;


/**
 * Initialization code to be run on application startup.
 *
 * @author Phillip Ross
 */
@Singleton
@Startup
public class DebugStartupBean {

    private static final Logger logger = LoggerFactory.getLogger(DebugStartupBean.class);


    @PostConstruct
    public void initialize() {
        logger.debug("********* Initializing - DEBUG - ********* ");
    }


}
