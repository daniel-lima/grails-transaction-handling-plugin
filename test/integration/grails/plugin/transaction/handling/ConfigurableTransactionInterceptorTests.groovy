package grails.plugin.transaction.handling

import grails.test.*

import java.lang.reflect.Method
import java.util.Map

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute
import org.springframework.transaction.interceptor.TransactionAttributeSource



class ConfigurableTransactionInterceptorTests extends GroovyTestCase {
    
    GrailsApplication grailsApplication
    TestAnnotatedService testAnnotatedService
    private ConfigurableTransactionInterceptor transactionInterceptor

    @Override
    protected void setUp() throws Exception {
        super.setUp()
        this.transactionInterceptor = grailsApplication.mainContext.getBean(ConfigurableTransactionInterceptor)
        assertNotNull(transactionInterceptor)
        getPluginConfig true
        this.transactionInterceptor.grailsApplication = grailsApplication
        reloadTransactionInterceptor()  
    }    
     
    
    public void testGetAttribute() {        
        TransactionAttribute result = getAttribute('serviceMethod1', testAnnotatedService)
        assertNotNull result
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_DEFAULT, result.isolationLevel)
        assertEquals(TransactionAttribute.TIMEOUT_DEFAULT, result.timeout)
        assertEquals(false, result.readOnly)
        assertSame(result, getAttribute('serviceMethod1', testAnnotatedService))
        
        
        result = getAttribute('serviceMethod2', testAnnotatedService)
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_REPEATABLE_READ, result.isolationLevel)
        assertEquals(TransactionAttribute.TIMEOUT_DEFAULT, result.timeout)
        assertEquals(false, result.readOnly)
        assertSame(result, getAttribute('serviceMethod2', testAnnotatedService))
        
        
        result = getAttribute('serviceMethod3', testAnnotatedService)
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_DEFAULT, result.isolationLevel)
        assertEquals(123, result.timeout)
        assertEquals(false, result.readOnly)
        assertSame(result, getAttribute('serviceMethod3', testAnnotatedService))
    }
    
    
    
    public void testGetAttributeDefaultConfig() {
        reloadTransactionInterceptor([propagation: 'requiresNew', timeout: 765, isolation: 'serializable', readOnly: true])                      
        //reloadTransactionInterceptor([propagation: 'requiresNew', timeout: 765, isolationLevel: TransactionAttribute.ISOLATION_SERIALIZABLE, readOnly: true])
        
        TransactionAttribute result = getAttribute('serviceMethod1', testAnnotatedService)
        assertNotNull result
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_SERIALIZABLE, result.isolationLevel)
        assertEquals(765, result.timeout)
        assertEquals(false, result.readOnly)
        assertTrue(result.rollbackRules == null || result.rollbackRules.isEmpty())
        assertSame(result, getAttribute('serviceMethod1', testAnnotatedService))
        
        
        result = getAttribute('serviceMethod2', testAnnotatedService)
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_REPEATABLE_READ, result.isolationLevel)
        assertEquals(765, result.timeout)
        assertEquals(false, result.readOnly)
        assertTrue(result.rollbackRules == null || result.rollbackRules.isEmpty())
        assertSame(result, getAttribute('serviceMethod2', testAnnotatedService))
        
        
        result = getAttribute('serviceMethod3', testAnnotatedService)
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_SERIALIZABLE, result.isolationLevel)
        assertEquals(123, result.timeout)
        assertEquals(false, result.readOnly)
        assertTrue(result.rollbackRules == null || result.rollbackRules.isEmpty())
        assertSame(result, getAttribute('serviceMethod3', testAnnotatedService))
        
        
        result = getAttribute('serviceMethod4', testAnnotatedService)
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_SERIALIZABLE, result.isolationLevel)
        assertEquals(765, result.timeout)
        assertEquals(true, result.readOnly)
        assertEquals([new RollbackRuleAttribute(RuntimeException)], result.rollbackRules)
        assertSame(result, getAttribute('serviceMethod4', testAnnotatedService))
    }
    
    
    public void testGetAttributeDefaultWitExceptionRules() {
        reloadTransactionInterceptor([isolationLevel: TransactionDefinition.ISOLATION_READ_UNCOMMITTED, rollbackFor: [IllegalArgumentException], noRollbackFor: [IllegalStateException]])
        
        TransactionAttribute result = getAttribute('serviceMethod1', testAnnotatedService)
        assertNotNull result
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_READ_UNCOMMITTED, result.isolationLevel)
        assertEquals(TransactionAttribute.TIMEOUT_DEFAULT, result.timeout)
        assertEquals(false, result.readOnly)
        assertEquals([new RollbackRuleAttribute(IllegalArgumentException), new NoRollbackRuleAttribute(IllegalStateException)], result.rollbackRules)
        assertSame(result, getAttribute('serviceMethod1', testAnnotatedService))
        
        
        result = getAttribute('serviceMethod4', testAnnotatedService)
        assertNotNull result
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, result.propagationBehavior)
        assertEquals(TransactionAttribute.ISOLATION_READ_UNCOMMITTED, result.isolationLevel)
        assertEquals(TransactionAttribute.TIMEOUT_DEFAULT, result.timeout)
        assertEquals(true, result.readOnly)
        assertEquals([new RollbackRuleAttribute(RuntimeException)], result.rollbackRules)
        assertSame(result, getAttribute('serviceMethod4', testAnnotatedService))
        
    }
    
    
    
    private TransactionAttribute getAttribute(String methodName, Object service) {
        TransactionAttributeSource attSource = transactionInterceptor.transactionAttributeSource
        Method m = service.class.getMethod(methodName)
        assertNotNull(m)
        TransactionAttribute result = attSource.getTransactionAttribute(m, service.class)
        return result
    }
    
    private void reloadTransactionInterceptor(Map cfg = [:]) {
        Map config = getPluginConfig(true).declarative
        for (c in cfg.entrySet()) {
            config[c.key] = c.value
        }
        println getPluginConfig(false)        
        this.transactionInterceptor.afterPropertiesSet()        
    }
    
    private Map getPluginConfig(boolean reload = false) {
        return grailsApplication.getMergedConfig(reload).grails.plugin.transactionHandling.asMap(true)
    }
}
