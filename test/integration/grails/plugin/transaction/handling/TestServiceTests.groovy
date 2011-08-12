package grails.plugin.transaction.handling;

import grails.test.*
import groovy.util.GroovyTestCase;

class TestServiceTests  extends GroovyTestCase  {
    
    TestService testService
    
    void testA() {
        assertNotNull(testService)
        testService.serviceMethod1()
        testService.serviceMethod3()
    }

}
