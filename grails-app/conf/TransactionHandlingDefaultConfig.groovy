import org.springframework.transaction.TransactionDefinition

grails {
    plugin {
        transactionHandling {
            global {
               //timeout 
            }
            
            programmatic {
               // isolation =
               // timeout =
               // readOnly =
            }
            
            declarative {
               // isolation =
               // timeout =
               // readOnly =
               // rollbackFor =
               // noRollbackFor =
            }
            
            implicit {
               // isolation =
               // timeout =
               // readOnly =
               // rollbackFor =
               // noRollbackFor = 
            }
        }
    }
}