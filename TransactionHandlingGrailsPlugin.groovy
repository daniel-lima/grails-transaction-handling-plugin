/*
* Copyright 2010-2011 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import grails.util.GrailsNameUtils

import java.lang.reflect.Modifier

import org.apache.log4j.Logger
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate

/**
* @author Daniel Henrique Alves Lima
*/
class TransactionHandlingGrailsPlugin {
    private final Logger log = Logger.getLogger(getClass())
    
    // the plugin version
    def version = "0.1.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2.5 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    def loadAfter = ['hibernate']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            'grails-app/**/*',
            'web-app/**/*',
            'scripts/**/Eclipse.groovy'
    ]

    // TODO Fill in these fields
    def author = "Daniel Henrique Alves Lima"
    def authorEmail = "email_daniel_h@yahoo.com.br"
    def title = 'Grails Transaction Handling Plugin'
    def description = '''\\
Plugin for advanced management of transactions in Grails.
Possibly a backport of http://jira.grails.org/browse/GRAILS-7093. 
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/transaction-handling"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->

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

        /* [transactionTemplatePropertyAlias: [name: transactionTemplatePropertyName, value: transactionDefinitionConstantNameOrValue]] */
        Map propertyMappings = [propagation: [name: 'propagationBehaviorName', value: 'name'], 
                                isolation: [name: 'isolationLevelName', value: 'name'], 
                                timeout: [name: 'timeout', value: 'value']]

        Map withTrxDefaults = [propagation: 'required']
        Map withNewTrxDefaults = [propagation: 'requiresNew']

        Closure withTrxImpl = {Map defaults, Map properties, Closure callable ->
            if (properties != Collections.EMPTY_MAP) {
                Map props = new LinkedHashMap(defaults)
                props.putAll(properties)
                properties = props
            } else {
                properties = defaults
            }

            log.debug("transaction properties ${properties}")
            
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

                log.debug("${name}=${value}")   
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

        for (domainClass in application.domainClasses) {
            Class clazz = domainClass.clazz

            // Force the lazy init of GORM dynamic methods
            Closure c = clazz.&withTransaction

            clazz.metaClass.'static'.withTransaction = withTrx
            clazz.metaClass.'static'.withNewTransaction = withNewTrx
        }
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
