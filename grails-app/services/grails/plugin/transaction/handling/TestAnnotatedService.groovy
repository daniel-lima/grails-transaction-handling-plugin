package grails.plugin.transaction.handling

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

class TestAnnotatedService {

    static transactional = false

    @Transactional
    def serviceMethod1() {
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    def serviceMethod2() {
    }


    @Transactional(timeout = 123)
    def serviceMethod3() {
    }
    
    
    @Transactional(rollbackFor = RuntimeException, readOnly = true)
    def serviceMethod4() {
    }
}
