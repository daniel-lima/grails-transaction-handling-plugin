package grails.plugin.transaction.handling;

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.IdentityHashMap
import java.util.Map

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.transaction.interceptor.DefaultTransactionAttribute
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
import org.springframework.transaction.interceptor.TransactionAttribute
import org.springframework.transaction.interceptor.TransactionAttributeSource
import org.springframework.util.Assert


public class MyTransactionAttributeSource implements TransactionAttributeSource {

    private final Log log = LogFactory.getLog(getClass());
    private final Map cache = new IdentityHashMap();
    protected final TransactionAttributeSource source;
    private final Map configuredDefaults;
    private final TransactionAttribute defaults;

    public MyTransactionAttributeSource(TransactionAttributeSource source, Map configuredDefaults) {
        this.source = source;
        Assert.notNull(this.source);
        this.configuredDefaults = configuredDefaults;
        if (this.configuredDefaults != null) {
            this.defaults = new RuleBasedTransactionAttribute();
        } else {
            this.defaults = null;
        }
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