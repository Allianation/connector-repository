package com.connector.database.yaml;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectorDetails {
	
	private String type;
	private List<QueryDetails> queryDetails;

}
