package org.springframework.samples.travel.services;

import com.gemstone.gemfire.cache.Cache;
import org.hibernate.cache.HashtableCacheProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.gemfire.GemfireCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple class that initializes all the services including data access logic
 */

@Configuration
public class ServicesConfiguration {

	private Class<? extends Dialect> dialect = H2Dialect.class;

	@Value("classpath:/import.sql")
	private Resource importSqlResource;

	@Value("${ds.url}")
	protected String url;

	@Value("${ds.password}")
	protected String password;

	@Value("${ds.driverClassName}")
	protected String driverClassName;

	@Value("${ds.user}")
	protected String user;

	@Value("${ds.name}")
	protected String database;

	@Bean(name = "dataSource")
	public DataSource dataSource() {
		DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
		driverManagerDataSource.setUrl(this.url);
		driverManagerDataSource.setPassword(this.password);
		driverManagerDataSource.setUsername(this.user);
		driverManagerDataSource.setDriverClassName(this.driverClassName);
		return driverManagerDataSource;
	}

	public String getPersistenceXmlLocation() {
		return "classpath:/META-INF/persistence.xml";
	}

	@Bean
	public Map<String, Object> jpaProperties() {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("hibernate.dialect", this.dialect.getName());
//		 props.put("hibernate.hbm2ddl.auto", "create");
		//props.put("hibernate.show_sql", "true");
		props.put("hibernate.cache.provider_class", HashtableCacheProvider.class.getName());
		return props;
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
		hibernateJpaVendorAdapter.setShowSql(false);
		hibernateJpaVendorAdapter.setGenerateDdl(false);
		hibernateJpaVendorAdapter.setDatabase(Database.H2);
		return hibernateJpaVendorAdapter;
	}

	@Bean
	public JpaTransactionManager transactionManager() {
		return new JpaTransactionManager(this.localContainerEntityManagerFactoryBean().getObject());
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean() {
		LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
		lef.setDataSource(this.dataSource());
		lef.setJpaVendorAdapter(this.jpaVendorAdapter());
		lef.setJpaPropertyMap(this.jpaProperties());
		lef.setPersistenceUnitName("travelDatabase");
		lef.setPersistenceProviderClass(HibernatePersistence.class);
		lef.setPersistenceXmlLocation(getPersistenceXmlLocation());
		return lef;
	}

	@Autowired
	@Qualifier("c") private Cache gemfireCache;

	@Bean
	public CacheManager gemfireCacheManager() {
		GemfireCacheManager cacheManager = new GemfireCacheManager();
		cacheManager.setGemfireCache(this.gemfireCache);
		return cacheManager ;
	}
}
