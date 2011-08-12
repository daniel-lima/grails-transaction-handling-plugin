package grails.plugin.transaction.handling

import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import grails.test.*


class TransactionPropertiesUtilTests extends GrailsUnitTestCase {
    
    private TransactionPropertiesUtil txPropsUtil = new TransactionPropertiesUtil()
    
    void testExpandWithoutRollbackRules() {
        Map result = null
        result = txPropsUtil.expand([:])
        assertNotNull result
        assertTrue result.isEmpty()
        
        result = txPropsUtil.expand([propagation: 'required'])
        assertEquals([propagationBehaviorName: 'PROPAGATION_REQUIRED'], result)
        
        result = txPropsUtil.expand([isolation: 'serializable', timeout: 'default'])
        assertEquals([isolationLevelName: 'ISOLATION_SERIALIZABLE', timeout: TransactionDefinition.TIMEOUT_DEFAULT], result)
                
        result = txPropsUtil.expand([propagationBehaviorName: 'PROPAGATION_REQUIRES_NEW', isolationLevel: TransactionDefinition.ISOLATION_REPEATABLE_READ])
        assertEquals([propagationBehaviorName: 'PROPAGATION_REQUIRES_NEW', isolationLevel: TransactionDefinition.ISOLATION_REPEATABLE_READ], result)
        
        result = txPropsUtil.expand([timeout: 987, readOnly: true])
        assertEquals([timeout: 987, readOnly: true], result)
                
        result = txPropsUtil.expand([propagationBehaviorName: 'PROPAGATION_NEVER', propagation: 'supports'])
        assertEquals([propagationBehaviorName: 'PROPAGATION_SUPPORTS'], result)
                
        result = txPropsUtil.expand([isolation: 'default', isolationLevel: TransactionDefinition.ISOLATION_READ_UNCOMMITTED])
        assertEquals([isolationLevelName: 'ISOLATION_DEFAULT', isolationLevel: TransactionDefinition.ISOLATION_READ_UNCOMMITTED], result)               
    }
    
    
    void testExpandWithRollbackRules() {
        Map result = null
        result = txPropsUtil.expand([rollbackFor: ['Abc']])
        assertEquals([rollbackRules: [new RollbackRuleAttribute('Abc')]], result)
                
        result = txPropsUtil.expand([propagation: 'requiresNew', noRollbackFor: [RuntimeException]])
        assertEquals([propagationBehaviorName: 'PROPAGATION_REQUIRES_NEW', rollbackRules: [new NoRollbackRuleAttribute(RuntimeException)]], result)

        
        result = txPropsUtil.expand([isolation: 'default', readOnly: true, rollbackFor: [IllegalStateException, 'B'], noRollbackFor: [RuntimeException, 'A']])
        assertEquals([isolationLevelName: 'ISOLATION_DEFAULT', readOnly: true, 
            rollbackRules: [new RollbackRuleAttribute(IllegalStateException), new RollbackRuleAttribute('B'),
                new NoRollbackRuleAttribute(RuntimeException), new NoRollbackRuleAttribute('A')]], result)
    }
    
    
    void testApplyWithoutRollbackRules() {
        for (txDefClass in [DefaultTransactionDefinition, DefaultTransactionAttribute, RuleBasedTransactionAttribute]) {
            TransactionDefinition txDef = txDefClass.newInstance()
            txPropsUtil.applyTo([:], txDef)            
            assertEquals(TransactionDefinition.PROPAGATION_REQUIRED, txDef.propagationBehavior)
            assertEquals(TransactionDefinition.ISOLATION_DEFAULT, txDef.isolationLevel)
            assertEquals(TransactionDefinition.TIMEOUT_DEFAULT, txDef.timeout)
            assertEquals(false, txDef.readOnly)
            
            
            txDef = txDefClass.newInstance()
            txPropsUtil.applyTo([propagation: 'supports', timeout: 123], txDef)
            assertEquals(TransactionDefinition.PROPAGATION_SUPPORTS, txDef.propagationBehavior)
            assertEquals(TransactionDefinition.ISOLATION_DEFAULT, txDef.isolationLevel)
            assertEquals(123, txDef.timeout)
            assertEquals(false, txDef.readOnly)
        }        
    }
    
    
    void testApplyWithRollbackRules() {
        TransactionDefinition txDef = new RuleBasedTransactionAttribute()
        txPropsUtil.applyTo([propagation: 'never', readOnly: true, rollbackFor: [IllegalArgumentException], noRollbackFor: [IllegalStateException]], txDef)
        assertEquals(TransactionDefinition.PROPAGATION_NEVER, txDef.propagationBehavior)
        assertEquals(TransactionDefinition.ISOLATION_DEFAULT, txDef.isolationLevel)
        assertEquals(TransactionDefinition.TIMEOUT_DEFAULT, txDef.timeout)
        assertEquals(true, txDef.readOnly)
        assertEquals([new RollbackRuleAttribute(IllegalArgumentException), new NoRollbackRuleAttribute(IllegalStateException)], txDef.rollbackRules)        
    }
    
    
    void testRemovePropagation() {
        Map result = null
        
        result = txPropsUtil.removePropagationProperties([:])
        assertNotNull result
        assertTrue result.isEmpty()
                
        result = txPropsUtil.removePropagationProperties([propagation:'required'])
        assertNotNull result
        assertTrue result.isEmpty()
        
        result = txPropsUtil.removePropagationProperties([isolation:'default'])        
        assertEquals([isolation:'default'], result)

        result = txPropsUtil.removePropagationProperties([isolation:'default', propagationBehavior: TransactionDefinition.PROPAGATION_MANDATORY, timeout:12])
        assertEquals([isolation:'default', timeout: 12], result)
                
        result = txPropsUtil.removePropagationProperties([isolationLevel: TransactionDefinition.ISOLATION_REPEATABLE_READ, propagationBehaviorName: 'PROPAGATION_MANDATORY', rollbackFor: [], noRollbackFor: []])
        assertEquals([isolationLevel: TransactionDefinition.ISOLATION_REPEATABLE_READ, rollbackFor: [], noRollbackFor: []], result)        
    }
}
