package grails.plugin.transaction.handling;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.transaction.interceptor.TransactionInterceptor;

public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private final Log log = LogFactory.getLog(getClass());
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        //if (true) throw new RuntimeException("abc");
        String [] names = beanFactory.getBeanNamesForType(TransactionInterceptor.class, true, false);
        log.debug("postProcessBeanFactory(): names " + Arrays.asList(names));
        
        for (String name : names) {
            BeanDefinition def = beanFactory.getBeanDefinition(name);
            log.debug("postProcessBeanFactory(): old bean class " + def.getBeanClassName());
            def.setBeanClassName(MyTransactionInterceptor.class.getName());
        }
    }

}
