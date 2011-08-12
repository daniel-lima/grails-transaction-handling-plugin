package grails.plugin.transaction.handling;

import grails.test.*
import groovy.util.GroovyTestCase;

class TestAnnotatedServiceTests  extends GroovyTestCase  {
    
    TestAnnotatedService testAnnotatedService
    
    void testA() {
        assertNotNull(testAnnotatedService)
        testAnnotatedService.serviceMethod1()
        testAnnotatedService.serviceMethod2()
        testAnnotatedService.serviceMethod3()
        testAnnotatedService.serviceMethod4()
    }

}
