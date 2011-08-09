package grails.plugin.transaction.handling;

import groovy.util.Eval;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.util.Assert;

public class TransactionManagerPostProcessor implements BeanPostProcessor,
        GrailsApplicationAware {

    private Log log = LogFactory.getLog(getClass());
    private GrailsApplication grailsApplication;
    private int timeout;

    @Override
    public Object postProcessAfterInitialization(Object bean, String name)
            throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String name)
            throws BeansException {
        if (log.isDebugEnabled()) {
            log.debug("postProcessBeforeInitialization(): name " + name);
        }

        if (bean != null && bean instanceof PlatformTransactionManager) {

            if (bean instanceof AbstractPlatformTransactionManager) {

                AbstractPlatformTransactionManager tm = (AbstractPlatformTransactionManager) bean;

                if (log.isDebugEnabled()) {
                    log.debug("postProcessBeforeInitialization(): transactionManager "
                            + tm);
                    log.debug("postProcessBeforeInitialization(): transactionManager.defaultTimeout "
                            + tm.getDefaultTimeout());
                    log.debug("postProcessBeforeInitialization(): timeout "
                            + this.timeout);
                }

                if (tm.getDefaultTimeout() != this.timeout) {
                    tm.setDefaultTimeout(this.timeout);
                }

                if (log.isDebugEnabled()) {
                    log.debug("postProcessBeforeInitialization(): transactionManager.defaultTimeout "
                            + tm.getDefaultTimeout());
                }
            } else {
                if (this.timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                    log.error("postProcessBeforeInitialization(): Default timeout cannot be set for "
                            + bean);
                }
            }
        }

        return bean;
    }

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
        Assert.notNull(this.grailsApplication);

        this.timeout = (Integer) Eval
                .x(this.grailsApplication,
                        "x.mergedConfig.asMap(true).grails.plugin.transactionHandling.global.timeout");
    }

}
