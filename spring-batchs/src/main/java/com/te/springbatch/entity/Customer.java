package com.te.springbatch.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    @Id
	private Integer custId;
	private String custName;
	private String custEmail;
	private long custContactNo;
	private String custAddress;
}
