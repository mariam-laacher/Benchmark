package com.benchmark.jersey.dao;

import com.benchmark.common.dto.PageResult;
import com.benchmark.common.entity.Category;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.List;
import java.util.Optional;

@Singleton
public class CategoryDAO {
    @Inject
    private SessionFactory sessionFactory;

    public PageResult<Category> findAll(int page, int size) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            long total = (Long) session.createQuery("SELECT COUNT(c) FROM Category c", Long.class).uniqueResult();
            List<Category> items = session.createQuery("FROM Category c ORDER BY c.id", Category.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .list();
            session.getTransaction().commit();
            return new PageResult<>(items, page, size, total);
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public Optional<Category> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            Category category = session.get(Category.class, id);
            session.getTransaction().commit();
            return Optional.ofNullable(category);
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public Category save(Category category) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            session.persist(category);
            session.getTransaction().commit();
            return category;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public Category update(Category category) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            Category merged = session.merge(category);
            session.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(Long id) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            Category category = session.get(Category.class, id);
            if (category != null) {
                session.remove(category);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
    }
}

