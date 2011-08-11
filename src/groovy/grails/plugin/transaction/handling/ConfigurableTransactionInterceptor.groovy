package grails.plugin.transaction.handling

import grails.spring.BeanBuilder.ConfigurableRuntimeBeanReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method
import java.util.Properties

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute
import org.springframework.transaction.interceptor.TransactionAttributeSource
import org.springframework.transaction.interceptor.TransactionInterceptor
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

public class ConfigurableTransactionInterceptor extends TransactionInterceptor implements GrailsApplicationAware {

    private final Log log = LogFactory.getLog(getClass())    
    private Map configuredDefaults = null
    private TransactionAttribute defaults

    public ConfigurableTransactionInterceptor() {
        log.debug("constructor()")
    }

    @Override
    public void setTransactionAttributeSource(
    TransactionAttributeSource transactionAttributeSource) {
        log.debug("setTransactionAttributeSource(): transactionAttributeSource ${transactionAttributeSource}")        
        transactionAttributeSource = new MyTransactionAttributeSource(transactionAttributeSource)        
        super.setTransactionAttributeSource(transactionAttributeSource)
    }

    @Override
    public void setTransactionAttributeSources(
    TransactionAttributeSource[] transactionAttributeSources) {
        log.debug("setTransactionAttributeSources(): transactionAttributeSources ${transactionAttributeSources}")
        super.setTransactionAttributeSources(transactionAttributeSources)
    }

    @Override
    public void setTransactionAttributes(Properties transactionAttributes) {
        log.debug("setTransactionAttributes(): transactionAttributes ${transactionAttributes}")
        super.setTransactionAttributes(transactionAttributes)
    }
    
    


    @Override
    public TransactionAttributeSource getTransactionAttributeSource() {        
         //log.debug('getTransactionAttributeSource(): Begin')
         TransactionAttributeSource source = super.getTransactionAttributeSource();
         Assert.notNull source
         Assert.isAssignable MyTransactionAttributeSource, source.getClass()
         return source
         //log.debug('getTransactionAttributeSource(): End')
    }

    @Override
    public void setGrailsApplication(GrailsApplication grailsApplication) {        
        configuredDefaults = null
        defaults = null
        if (grailsApplication != null) {
            TransactionPropertiesUtil txPropsUtil = new TransactionPropertiesUtil()
            Map config = grailsApplication.mergedConfig.asMap(true).grails.plugin.transactionHandling.declarative
            if (config && !config.isEmpty()) {
                configuredDefaults = txPropsUtil.expand(config)
                defaults = new RuleBasedTransactionAttribute()                                  
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("setGrailsApplication(): template ${configuredDefaults}")
        }
    }





    protected class MyTransactionAttributeSource implements TransactionAttributeSource {

        private final Map cache = new IdentityHashMap()
        protected final TransactionAttributeSource source
        
        public MyTransactionAttributeSource(TransactionAttributeSource source) {
            this.source = source
            Assert.notNull(this.source)
        }

        @Override
        public TransactionAttribute getTransactionAttribute(Method method, Class<?> clazz) {
            TransactionAttribute att = this.source.getTransactionAttribute(method, clazz)
            
            if (log.isDebugEnabled()) {
                log.debug("getTransactionAttribute(): att ${att}; template ${configuredDefaults}")
            }

            if (configuredDefaults != null) {
                TransactionAttribute newAtt = cache[att]
                if (newAtt == null) {                    
                    if (att != null) {
                        Constructor c = att.getClass().getConstructor(att.getClass())
                        newAtt = c.newInstance(att)
                    } else {
                        newAtt = new DefaultTransactionAttribute()
                    }
                                                                               
                    for (entry in configuredDefaults.entrySet()) {
                        if (log.isDebugEnabled()) {
                            log.debug("getTransactionAttribute(): key ${entry.key}")
                        }
                        
                        if (newAtt[entry.key] == defaults[entry.key] && entry.value != defaults[entry.key]) {
                            if (log.isDebugEnabled()) {
                                log.debug("getTransactionAttribute(): ${entry.key} = ${entry.value}")
                            }
                            newAtt[entry.key] = entry.value
                        }
                    }                                      
                }
                
                cache[att] = newAtt
                att = newAtt
            }

            if (log.isDebugEnabled()) {
                log.debug("getTransactionAttribute(): att = ${att}")
            }
            
            return att
        }
    }
}
