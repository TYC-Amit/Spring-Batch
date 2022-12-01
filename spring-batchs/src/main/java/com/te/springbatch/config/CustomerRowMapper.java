package com.te.springbatch.config;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.te.springbatch.entity.Customer;


public class CustomerRowMapper implements RowMapper<Customer> {

	@Override
	public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Customer customer=new Customer();
		customer.setCustId(rs.getInt("custId"));
		customer.setCustName(rs.getString("custName"));
		customer.setCustEmail(rs.getString("custEmail"));
		customer.setCustContactNo(rs.getLong("custContactNo"));
		customer.setCustAddress(rs.getString("custAddress"));

		
		return customer;
	}

}
