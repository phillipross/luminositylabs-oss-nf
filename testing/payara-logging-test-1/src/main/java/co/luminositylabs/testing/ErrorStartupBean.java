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
public class ErrorStartupBean {

    private static final Logger logger = LoggerFactory.getLogger(ErrorStartupBean.class);


    @PostConstruct
    public void initialize() {
        logger.error("********* Initializing - ERROR - ********* ");
    }


}
