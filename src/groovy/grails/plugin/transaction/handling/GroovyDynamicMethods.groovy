package grails.plugin.transaction.handling

import grails.util.GrailsNameUtils

import java.lang.reflect.Modifier

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClass
import org.springframework.context.ApplicationContext
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate

class GroovyDynamicMethods {
    
    private final Logger log
    
    public GroovyDynamicMethods(Logger log = null) {
        this.log = log
        if (this.log == null) {
            this.log = Logger.getLogger(this.class)
        }
    }
    
    public void doWith(ApplicationContext ctx, GrailsApplication application, GrailsClass targetDomainClass = null) {
        def constantModifier = Modifier.FINAL | Modifier.STATIC | Modifier.PUBLIC
        /* [transactionTemplatePropertyAlias: [transactionDefinitionConstantAlias: [name: constantName, value: constantValue]]] */
        Map constantMappings = [propagation: [:], isolation: [:], timeout: [:]]
        Set constantPrefixes = new LinkedHashSet(constantMappings.keySet().collect {it.toUpperCase()})
                
        for (field in TransactionDefinition.class.fields) {
            if ((field.modifiers & constantModifier) == constantModifier) {
                for (prefix in constantPrefixes) {
                    if (field.name.startsWith(prefix)) {
                        def key = field.name.replace(prefix, '').replace('_', '-').toLowerCase()
                        key = GrailsNameUtils.getPropertyNameForLowerCaseHyphenSeparatedName(key)
                        constantMappings[prefix.toLowerCase()][key] = [name: field.name, value: field.get(null)]
                    }
                }
            }
        }

        log.debug("constantMappings ${constantMappings}")
        
        Map pluginConfig = application.mergedConfig.asMap(true).grails.plugin.transactionHandling
        
        log.debug("pluginConfig ${pluginConfig}")

        /* [transactionTemplatePropertyAlias: [name: transactionTemplatePropertyName, value: transactionDefinitionConstantNameOrValue]] */
        Map propertyMappings = [propagation: [name: 'propagationBehaviorName', value: 'name'],
                                isolation: [name: 'isolationLevelName', value: 'name'],
                                timeout: [name: 'timeout', value: 'value']]

        Map withTrxDefaults = [propagation: 'required']
        Map withNewTrxDefaults = [propagation: 'requiresNew']
        
        for (defaults in [withTrxDefaults, withNewTrxDefaults]) {
            Map programmaticDefaults = [:]
            for (entry in pluginConfig.programmatic.defaults.entrySet()) {
                if (!defaults.containsKey(entry.key)) {
                    programmaticDefaults[entry.key] = entry.value
                }
            }
            programmaticDefaults.putAll(defaults)
            defaults.clear(); defaults.putAll(programmaticDefaults)
        }
        log.debug("withTrxDefaults ${withTrxDefaults}")
        log.debug("withNewTrxDefaults ${withNewTrxDefaults}")

        Closure withTrxImpl = {Map defaults, Map properties, Closure callable ->
            if (properties != Collections.EMPTY_MAP) {
                Map props = new LinkedHashMap(defaults)
                props.putAll(properties)
                properties = props
            } else {
                properties = defaults
            }

            if (log.isDebugEnabled()) {
                log.debug("transaction properties ${properties}")
            }
            
            TransactionTemplate template = new TransactionTemplate(ctx.getBean('transactionManager'))
            
            for (prop in properties) {
                String name = prop.key
                Object value = prop.value
                
                Map propMapping = propertyMappings[name]

                String newName = propMapping?.name
                Object newValue = constantMappings[name]
                
                if (value != null && !(value instanceof CharSequence)) {
                    // shortcut for non-text values
                    newValue = null
                }
                
                if (newValue != null && value != null) {
                    newValue = newValue[value.toString()]
                    if (newValue != null) { // Invalid property names produce null values
                        newValue = newValue[propMapping?.value]
                    }
                }
                
                name = (newName != null)? newName : name
                value = (newValue != null)? newValue: value

                if (log.isDebugEnabled()) {
                    log.debug("${name}=${value}")
                }
                template[name] = value
            }
            
            template.execute(
                {status ->
                    callable.call(status)
                } as TransactionCallback)
        }

        Closure withTrx = {Map properties = Collections.EMPTY_MAP, Closure callable ->
            withTrxImpl(withTrxDefaults, properties, callable)
        }
        Closure withNewTrx = {Map properties = Collections.EMPTY_MAP, Closure callable ->
            withTrxImpl(withNewTrxDefaults, properties, callable)
        }

        List domainClasses = targetDomainClass != null? [targetDomainClass] : application.domainClasses
        for (domainClass in domainClasses) {
            Class clazz = domainClass.clazz

            // Force the lazy init of GORM dynamic methods
            Closure c = clazz.&withTransaction

            clazz.metaClass.'static'.withTransaction = withTrx
            clazz.metaClass.'static'.withNewTransaction = withNewTrx
        }
    }

}
