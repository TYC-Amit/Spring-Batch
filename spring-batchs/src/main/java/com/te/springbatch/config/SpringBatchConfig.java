package com.te.springbatch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.te.springbatch.config.repository.CustomerRepository;
import com.te.springbatch.entity.Customer;

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

	private JobBuilderFactory jobBuilderFactory;

	private StepBuilderFactory stepBuilderFactory;

	private CustomerRepository customerDao;

	@Autowired
	private DataSource dataSource;

	public FlatFileItemReader<Customer> reader() {
		FlatFileItemReader<Customer> fileItemReader = new FlatFileItemReader<>();
		fileItemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
		fileItemReader.setName("csvReader");
		fileItemReader.setLinesToSkip(1);
		fileItemReader.setLineMapper(LineMapper());
		return fileItemReader;

	}

	private LineMapper<Customer> LineMapper() {
		DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		delimitedLineTokenizer.setDelimiter(",");
		delimitedLineTokenizer.setStrict(false);
		delimitedLineTokenizer.setNames("custId", "custName", "custEmail", "custContactNo", "custAddress");

		BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(Customer.class);

		lineMapper.setLineTokenizer(delimitedLineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);

		return lineMapper;
	}

	@Bean
	public CustomerProcessor processor() {
		return new CustomerProcessor();
	}

	@Bean
	public RepositoryItemWriter<Customer> writer() {
		RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
		writer.setRepository(customerDao);
		writer.setMethodName("save");
		return writer;
	}

	@Bean
	public Step stepA() {
		return stepBuilderFactory.get("csv-step").<Customer, Customer>chunk(10).reader(reader()).processor(processor())
				.writer(writer()).build();

	}

	@Bean
	public Job runJob() {
		return jobBuilderFactory.get("importcustomers").flow(stepA()).next(stepB())
				.end().build();

	}

	@Bean
	public JdbcCursorItemReader<Customer> reader1() {
		JdbcCursorItemReader<Customer> cursorItemReader = new JdbcCursorItemReader<>();
		cursorItemReader.setDataSource(dataSource);
		cursorItemReader.setSql("SELECT custId,custName,custEmail,custContactNo,custAddress FROM customer");
		cursorItemReader.setRowMapper(new CustomerRowMapper());
		return cursorItemReader;
	}

	@Bean
	public CustomerProcessor processor1() {
		return new CustomerProcessor();
	}

	@Bean
	public FlatFileItemWriter<Customer> writer1() {
		FlatFileItemWriter<Customer> fileItemWriter = new FlatFileItemWriter<>();
		fileItemWriter.setResource(new ClassPathResource("cust1.csv"));
		DelimitedLineAggregator<Customer> lineAggregator = new DelimitedLineAggregator<>();
		lineAggregator.setDelimiter(",");

		BeanWrapperFieldExtractor<Customer> wrapperFieldExtractor = new BeanWrapperFieldExtractor<>();
		wrapperFieldExtractor
				.setNames(new String[] { "custId", "custName", "custEmail", "custContactNo", "custAddress" });

		lineAggregator.setFieldExtractor(wrapperFieldExtractor);
		fileItemWriter.setLineAggregator(lineAggregator);

		return fileItemWriter;
	}

	@Bean
	public Step stepB() {
		return	stepBuilderFactory.get("stepB").<Customer, Customer>chunk(10).reader(reader1()).processor(processor1())
				.writer(writer1()).build();
	}
	
	

}
