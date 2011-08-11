import org.springframework.transaction.TransactionDefinition

grails {
    plugin {
        transactionHandling {
            global {
               timeout = TransactionDefinition.TIMEOUT_DEFAULT
            }
            
            programmatic {
               // isolation =
               // timeout =
               // readOnly =
            }
            
            declarative {
                timeout = 12
            }
        }
    }
}