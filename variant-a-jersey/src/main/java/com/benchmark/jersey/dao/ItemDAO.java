package com.benchmark.jersey.dao;

import com.benchmark.common.dto.PageResult;
import com.benchmark.common.entity.Item;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.List;
import java.util.Optional;

@Singleton
public class ItemDAO {
    @Inject
    private SessionFactory sessionFactory;

    private static final boolean USE_JOIN_FETCH = Boolean.parseBoolean(System.getenv().getOrDefault("USE_JOIN_FETCH", "false"));

    public PageResult<Item> findAll(int page, int size) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            long total = (Long) session.createQuery("SELECT COUNT(i) FROM Item i", Long.class).uniqueResult();
            String query = USE_JOIN_FETCH 
                ? "SELECT i FROM Item i JOIN FETCH i.category ORDER BY i.id"
                : "FROM Item i ORDER BY i.id";
            List<Item> items = session.createQuery(query, Item.class)
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

    public Optional<Item> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            Item item = session.get(Item.class, id);
            if (item != null && USE_JOIN_FETCH) {
                item.getCategory().getName();
            }
            session.getTransaction().commit();
            return Optional.ofNullable(item);
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public PageResult<Item> findByCategoryId(Long categoryId, int page, int size) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            String countQuery = "SELECT COUNT(i) FROM Item i WHERE i.category.id = :categoryId";
            long total = (Long) session.createQuery(countQuery, Long.class)
                    .setParameter("categoryId", categoryId)
                    .uniqueResult();
            
            String query = USE_JOIN_FETCH
                ? "SELECT i FROM Item i JOIN FETCH i.category WHERE i.category.id = :categoryId ORDER BY i.id"
                : "FROM Item i WHERE i.category.id = :categoryId ORDER BY i.id";
            List<Item> items = session.createQuery(query, Item.class)
                    .setParameter("categoryId", categoryId)
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

    public Item save(Item item) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            session.persist(item);
            session.getTransaction().commit();
            return item;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public Item update(Item item) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            Item merged = session.merge(item);
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
            Item item = session.get(Item.class, id);
            if (item != null) {
                session.remove(item);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
    }
}
