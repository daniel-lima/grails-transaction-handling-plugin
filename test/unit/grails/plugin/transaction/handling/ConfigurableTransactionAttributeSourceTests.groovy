package grails.plugin.transaction.handling

import java.lang.reflect.Method;
import java.util.Properties;

import org.codehaus.groovy.grails.orm.support.GroovyAwareNamedTransactionAttributeSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import grails.test.*

class ConfigurableTransactionAttributeSourceTests extends GrailsUnitTestCase {
    
    private GroovyAwareNamedTransactionAttributeSource parentSource
    
    protected void setUp() {
        super.setUp()
        
        Properties props = new Properties();
        props.setProperty("*", "PROPAGATION_REQUIRED");
        parentSource = new GroovyAwareNamedTransactionAttributeSource();
        parentSource.setProperties(props);
    }

   
    void testGetAttribute() {
        TransactionAttributeSource source = getSource()
        TransactionAttribute result = getAttribute(source, 'serviceMethod1', TestService.class)
        assertNotNull(result)
        assertEquals TransactionDefinition.PROPAGATION_REQUIRED, result.propagationBehavior
        assertEquals TransactionDefinition.ISOLATION_DEFAULT, result.isolationLevel
        assertEquals false, result.readOnly
        assertSame result, getAttribute(source, 'serviceMethod1', TestService.class)
        
        source = getSource([isolation: 'readUncommitted'])
        result = getAttribute(source, 'serviceMethod1', TestService.class)        
        assertNotNull(result)
        assertEquals TransactionDefinition.PROPAGATION_REQUIRED, result.propagationBehavior
        assertEquals TransactionDefinition.ISOLATION_READ_UNCOMMITTED, result.isolationLevel
        assertEquals false, result.readOnly
        assertTrue result.rollbackRules == null || result.rollbackRules.isEmpty()
        assertSame result, getAttribute(source, 'serviceMethod1', TestService.class)
        
        
        result = getAttribute(source, 'serviceMethod3', TestService.class)
        assertNotNull(result)
        assertEquals TransactionDefinition.PROPAGATION_REQUIRED, result.propagationBehavior
        assertEquals TransactionDefinition.ISOLATION_READ_UNCOMMITTED, result.isolationLevel
        assertEquals false, result.readOnly
        assertTrue result.rollbackRules == null || result.rollbackRules.isEmpty()
        assertSame result, getAttribute(source, 'serviceMethod3', TestService.class)
        
        
        source = getSource([propagation: 'requiresNew', readOnly: true])
        result = getAttribute(source, 'serviceMethod1', TestService.class)
        assertNotNull(result)
        assertEquals TransactionDefinition.PROPAGATION_REQUIRED, result.propagationBehavior
        assertEquals TransactionDefinition.ISOLATION_DEFAULT, result.isolationLevel
        assertEquals true, result.readOnly
        assertTrue result.rollbackRules == null || result.rollbackRules.isEmpty()
        assertSame result, getAttribute(source, 'serviceMethod1', TestService.class)
    }
    
    
    void testGetAttributeWithRollbackRules() {
        TransactionAttributeSource source = getSource([isolation: 'readUncommitted', rollbackFor: ['AException']])
        result = getAttribute(source, 'serviceMethod1', TestService.class)        
        assertNotNull(result)
        assertEquals TransactionDefinition.PROPAGATION_REQUIRED, result.propagationBehavior
        assertEquals TransactionDefinition.ISOLATION_READ_UNCOMMITTED, result.isolationLevel
        assertEquals false, result.readOnly
        assertEquals ([new RollbackRuleAttribute('AException')], result.rollbackRules)
        assertSame result, getAttribute(source, 'serviceMethod1', TestService.class)            
    }
    
    
    private TransactionAttribute getAttribute(TransactionAttributeSource source, String methodName, Class serviceClass) {        
        Method m = serviceClass.getMethod(methodName)
        assertNotNull(m)
        TransactionAttribute result = source.getTransactionAttribute(m, serviceClass)
        return result
    }
    
    private ConfigurableTransactionAttributeSource getSource(Map implicitConfig = [:]) {
        ConfigurableTransactionAttributeSource newSource = new ConfigurableTransactionAttributeSource(
            parentSource, implicitConfig, false);
        return newSource    
    }
}
