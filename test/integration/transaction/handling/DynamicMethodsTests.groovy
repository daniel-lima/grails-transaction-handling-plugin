package transaction.handling

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import grails.test.*



class DynamicMethodsTests extends GroovyTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testWithTransactionTemplate() {

        TransactionTemplate template = null

        TransactionTemplate.metaClass.constructor = {PlatformTransactionManager trMgr ->
            template = new TransactionTemplate()
            template.transactionManager =  trMgr
            template
        }

        TestUser.withTransaction {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_REQUIRED
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_DEFAULT
            assertEquals template.timeout, TransactionDefinition.TIMEOUT_DEFAULT
            assertEquals template.readOnly, false
        }


        TestUser.withTransaction(isolation: 'readUncommitted') {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_REQUIRED
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_READ_UNCOMMITTED
            assertEquals template.timeout, TransactionDefinition.TIMEOUT_DEFAULT
            assertEquals template.readOnly, false
        }

        TestUser.withTransaction(readOnly: true, timeout: 'default') {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_REQUIRED
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_DEFAULT
            assertEquals template.timeout, TransactionDefinition.TIMEOUT_DEFAULT
            assertEquals template.readOnly, true
        }

        TestUser.withTransaction(propagationBehaviorName: 'PROPAGATION_MANDATORY', timeout: 765) {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_MANDATORY
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_DEFAULT
            assertEquals template.timeout, 765
            assertEquals template.readOnly, false
        }


        TestUser.withTransaction(propagation: "mandatory") {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_MANDATORY
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_DEFAULT
            assertEquals template.timeout, TransactionDefinition.TIMEOUT_DEFAULT
            assertEquals template.readOnly, false
        }
    }


    void testWithNewTransactionTemplate() {

        TransactionTemplate template = null

        TransactionTemplate.metaClass.constructor = {PlatformTransactionManager trMgr ->
            template = new TransactionTemplate()
            template.transactionManager =  trMgr
            template
        }

        TestUser.withNewTransaction {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_REQUIRES_NEW
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_DEFAULT
            assertEquals template.timeout, TransactionDefinition.TIMEOUT_DEFAULT
            assertEquals template.readOnly, false
        }


        TestUser.withNewTransaction(isolationLevel: TransactionDefinition.ISOLATION_SERIALIZABLE) {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_REQUIRES_NEW
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_SERIALIZABLE
            assertEquals template.timeout, TransactionDefinition.TIMEOUT_DEFAULT
            assertEquals template.readOnly, false
        }


        TestUser.withNewTransaction(propagation: 'supports', readOnly: true, timeout: 612) {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_SUPPORTS
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_DEFAULT
            assertEquals template.timeout, 612
            assertEquals template.readOnly, true
        }


        TestUser.withNewTransaction(isolationLevelName: 'ISOLATION_REPEATABLE_READ') {
            println "template ${template}"
            assertEquals template.propagationBehavior, TransactionDefinition.PROPAGATION_REQUIRES_NEW
            assertEquals template.isolationLevel, TransactionDefinition.ISOLATION_REPEATABLE_READ
            assertEquals template.timeout, TransactionDefinition.TIMEOUT_DEFAULT
            assertEquals template.readOnly, false
        }


        try {
            TestUser.withNewTransaction(isolationLevelName: 'abc') {
            }
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }


    void testWithXYZTransactionStatus() {
        TestUser.withTransaction {TransactionStatus status ->
            assertEquals false, status.isNewTransaction()

            TestUser.withTransaction {TransactionStatus innerStatus ->
                assertEquals false, innerStatus.isNewTransaction()
            }
        }


        TestUser.withTransaction {TransactionStatus status ->
            assertEquals false, status.isNewTransaction()

            TestUser.withNewTransaction {TransactionStatus innerStatus ->
                assertEquals true, innerStatus.isNewTransaction()
            }
        }


        TestUser.withNewTransaction {TransactionStatus status ->
            assertEquals true, status.isNewTransaction()

            TestUser.withNewTransaction {TransactionStatus innerStatus ->
                assertEquals true, innerStatus.isNewTransaction()
            }
        }
    }
    
    
    void testWithTransaction() {
        List users = TestUser.list()
        int size = users.size()
        
        TestUser.withTransaction {
            new TestUser(name: 'A').save()
            new TestUser(name: 'B').save()
        }
        
        assertEquals(size + 2, TestUser.list().size())
    }
    
    
    void testWithNewTransaction() {
        List users = TestUser.list()
        int size = users.size()
        
        TestUser.withNewTransaction {
            new TestUser(name: 'A').save()
            new TestUser(name: 'B').save()
        }
        
        assertEquals(size + 2, TestUser.list().size())
        size += 2
        
        
        TestUser.withNewTransaction {status ->
            
            new TestUser(name: 'C').save()
            new TestUser(name: 'D').save(true)
            
            status.setRollbackOnly()
        }
        
        assertEquals(size, TestUser.list().size())
    }
}
