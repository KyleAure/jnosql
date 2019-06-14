/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.artemis.document.spi;


import org.jnosql.artemis.DatabaseQualifier;
import jakarta.nosql.mapping.DatabaseType;
import jakarta.nosql.mapping.document.DocumentTemplate;
import jakarta.nosql.mapping.document.DocumentTemplateProducer;
import org.jnosql.artemis.spi.AbstractBean;
import jakarta.nosql.document.DocumentCollectionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

class TemplateBean extends AbstractBean<DocumentTemplate> {

    private final Set<Type> types;

    private final String provider;

    private final Set<Annotation> qualifiers;

    /**
     * Constructor
     *
     * @param beanManager the beanManager
     * @param provider    the provider name, that must be a
     */
    public TemplateBean(BeanManager beanManager, String provider) {
        super(beanManager);
        this.types = Collections.singleton(DocumentTemplate.class);
        this.provider = provider;
        this.qualifiers = Collections.singleton(DatabaseQualifier.ofDocument(provider));
    }

    @Override
    public Class<?> getBeanClass() {
        return DocumentTemplate.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public DocumentTemplate create(CreationalContext<DocumentTemplate> creationalContext) {

        DocumentTemplateProducer producer = getInstance(DocumentTemplateProducer.class);
        DocumentCollectionManager manager = getManager();
        return producer.get(manager);
    }

    private DocumentCollectionManager getManager() {
        Bean<DocumentCollectionManager> bean = (Bean<DocumentCollectionManager>) getBeanManager().getBeans(DocumentCollectionManager.class,
                DatabaseQualifier.ofDocument(provider) ).iterator().next();
        CreationalContext<DocumentCollectionManager> ctx = getBeanManager().createCreationalContext(bean);
        return (DocumentCollectionManager) getBeanManager().getReference(bean, DocumentCollectionManager.class, ctx);
    }

    @Override
    public void destroy(DocumentTemplate instance, CreationalContext<DocumentTemplate> creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return DocumentTemplate.class.getName() + DatabaseType.COLUMN + "-" + provider;
    }

}
