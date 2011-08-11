package grails.plugin.transaction.handling;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@SuppressWarnings("serial")
public class MyTransactionInterceptor extends TransactionInterceptor {

    private final Log log = LogFactory.getLog(getClass());
    
    public MyTransactionInterceptor() {
        log.debug("constructor()");
    }

    @Override
    public void setTransactionAttributeSource(
            TransactionAttributeSource transactionAttributeSource) {
        log.debug("setTransactionAttributeSource(): transactionAttributeSource "
                + transactionAttributeSource);
        super.setTransactionAttributeSource(transactionAttributeSource);
    }

    @Override
    public void setTransactionAttributeSources(
            TransactionAttributeSource[] transactionAttributeSources) {
        log.debug("setTransactionAttributeSources(): transactionAttributeSources "
                + transactionAttributeSources);
        super.setTransactionAttributeSources(transactionAttributeSources);
    }

    @Override
    public void setTransactionAttributes(Properties transactionAttributes) {
        log.debug("setTransactionAttributes(): transactionAttributes "
                + transactionAttributes);
        super.setTransactionAttributes(transactionAttributes);
    }

}
