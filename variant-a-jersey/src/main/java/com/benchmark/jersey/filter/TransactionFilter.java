package com.benchmark.jersey.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.SessionFactory;

import jakarta.inject.Inject;

@Provider
public class TransactionFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Inject
    private SessionFactory sessionFactory;

    @Override
    public void filter(ContainerRequestContext requestContext) {
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (sessionFactory.getCurrentSession() != null && sessionFactory.getCurrentSession().getTransaction().isActive()) {
            try {
                sessionFactory.getCurrentSession().getTransaction().commit();
            } catch (Exception e) {
                sessionFactory.getCurrentSession().getTransaction().rollback();
            }
        }
    }
}

