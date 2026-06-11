package practice.samay.ordermanagementsystem.config;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@EnableTransactionManagement
public class HibernateConfig {

	@Value("${hibernate.dialect}")
	private String dialect;

	@Value("${hibernate.hbm2ddl.auto}")
	private String ddlAuto;

	@Value("${hibernate.show_sql}")
	private String showSql;

	@Value("${hibernate.format_sql}")
	private String formatSql;

	@Value("${hibernate.current_session_context_class}")
	private String currentSessionContextClass;

	@Value("${hibernate.jdbc.batch_size}")
	private String batchSize;

	@Value("${hibernate.order_inserts}")
	private String orderInserts;

	@Value("${hibernate.order_updates}")
	private String orderUpdates;

	@Bean
	public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
		LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setPackagesToScan("practice.samay.ordermanagementsystem.model");

		Properties hibernateProperties = new Properties();
		hibernateProperties.put("hibernate.dialect", dialect);
		hibernateProperties.put("hibernate.hbm2ddl.auto", ddlAuto);
		hibernateProperties.put("hibernate.show_sql", showSql);
		hibernateProperties.put("hibernate.format_sql", formatSql);
		hibernateProperties.put("hibernate.current_session_context_class", currentSessionContextClass);
		hibernateProperties.put("hibernate.jdbc.batch_size", batchSize);
		hibernateProperties.put("hibernate.order_inserts", orderInserts);
		hibernateProperties.put("hibernate.order_updates", orderUpdates);
		sessionFactoryBean.setHibernateProperties(hibernateProperties);
		return sessionFactoryBean;
	}

	@Bean
	public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
		return new HibernateTransactionManager(sessionFactory);
	}
}
