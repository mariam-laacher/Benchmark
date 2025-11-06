package com.benchmark.jersey.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import com.benchmark.common.util.HikariCPConfig;
import javax.sql.DataSource;
import jakarta.inject.Singleton;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class JerseyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        DataSource dataSource = HikariCPConfig.createDataSource();
        Configuration hibernateConfig = new Configuration();
        hibernateConfig.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        hibernateConfig.setProperty("hibernate.hbm2ddl.auto", "none");
        hibernateConfig.setProperty("hibernate.show_sql", "false");
        hibernateConfig.setProperty("hibernate.format_sql", "false");
        hibernateConfig.setProperty("hibernate.use_sql_comments", "false");
        hibernateConfig.setProperty("hibernate.cache.use_second_level_cache", "false");
        hibernateConfig.setProperty("hibernate.cache.use_query_cache", "false");
        hibernateConfig.setProperty("hibernate.current_session_context_class", "thread");
        hibernateConfig.addAnnotatedClass(com.benchmark.common.entity.Category.class);
        hibernateConfig.addAnnotatedClass(com.benchmark.common.entity.Item.class);
        
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.datasource", dataSource)
                .build();
        
        SessionFactory sessionFactory = hibernateConfig.buildSessionFactory(serviceRegistry);
        bind(sessionFactory).to(SessionFactory.class).in(Singleton.class);
        bind(dataSource).to(DataSource.class).in(Singleton.class);
    }
}

