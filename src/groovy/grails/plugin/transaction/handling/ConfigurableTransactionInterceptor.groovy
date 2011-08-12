package grails.plugin.transaction.handling;

import groovy.util.Eval;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.Assert;

class ConfigurableTransactionInterceptor extends TransactionInterceptor
        implements GrailsApplicationAware {

    private final Log log = LogFactory.getLog(getClass());
    private Map configuredDefaults = null;

    public ConfigurableTransactionInterceptor() {
        log.debug("constructor()");
    }

    @Override
    public void setTransactionAttributeSource(
            TransactionAttributeSource transactionAttributeSource) {
        log.debug("setTransactionAttributeSource(): transactionAttributeSource ${transactionAttributeSource}");
        transactionAttributeSource = new MyTransactionAttributeSource(
                transactionAttributeSource, this.configuredDefaults);
        super.setTransactionAttributeSource(transactionAttributeSource);
    }

    @Override
    public void setTransactionAttributeSources(
            TransactionAttributeSource[] transactionAttributeSources) {
        log.debug("setTransactionAttributeSources(): transactionAttributeSources ${transactionAttributeSources}");
        super.setTransactionAttributeSources(transactionAttributeSources);
    }

    @Override
    public void setTransactionAttributes(Properties transactionAttributes) {
        log.debug("setTransactionAttributes(): transactionAttributes ${transactionAttributes}");
        super.setTransactionAttributes(transactionAttributes);
    }

    @Override
    public TransactionAttributeSource getTransactionAttributeSource() {
        // log.debug('getTransactionAttributeSource(): Begin')
        TransactionAttributeSource source = super
                .getTransactionAttributeSource();
        Assert.notNull(source);

        Assert.isAssignable(MyTransactionAttributeSource.class,
                source.getClass());

        return source;
        // log.debug('getTransactionAttributeSource(): End')
    }

    @Override
    public void setGrailsApplication(GrailsApplication grailsApplication) {
        configuredDefaults = null;
        if (grailsApplication != null) {
            TransactionPropertiesUtil txPropsUtil = new TransactionPropertiesUtil();
            Map config = (Map) Eval
                    .x(grailsApplication,
                            "x.mergedConfig.asMap(true).grails.plugin.transactionHandling.declarative");
            if (config != null && !config.isEmpty()) {
                configuredDefaults = txPropsUtil.expand(txPropsUtil
                        .removePropagationProperties(config));
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("setGrailsApplication(): template ${configuredDefaults}");
        }
    }

}
