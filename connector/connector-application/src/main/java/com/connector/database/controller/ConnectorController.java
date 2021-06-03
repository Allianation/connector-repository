package com.connector.database.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.connector.database.model.Request;
import com.connector.database.yaml.Connector;
import com.connector.database.yaml.QueryDetails;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@Slf4j
@RequestMapping(path = { "v1/connector" }, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class ConnectorController {

	@Autowired
	@Qualifier("firstDataSource")
	HikariDataSource firstDataSource;

	@Autowired
	@Qualifier("secondDataSource")
	HikariDataSource secondDataSource;

	@PostMapping
	public ResponseEntity<String> getData(@RequestBody Request request) {

		var yaml = new Yaml(new Constructor(Connector.class));
		var inputStream = this.getClass().getClassLoader().getResourceAsStream("yaml/connector.yaml");
		Connector connector = yaml.load(inputStream);

		if (request.getDataSource().equals("firstDataSource")) {

			for (QueryDetails qd : connector.getConnectorDetails().get(0).getQueryDetails()) {

				if (qd.getName().equals(request.getQueryName())) {
					
					var sql = qd.getQuery();
					
					try (var con = firstDataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery();) {

						// Collect column names
			        	List<String> columnNames = new ArrayList<>();
			        	ResultSetMetaData rsmd = rs.getMetaData();
			        	for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			        	    columnNames.add(rsmd.getColumnLabel(i));
			        	}
			        	
			        	StringBuilder sb = new StringBuilder("[");
			        	int rowIndex = 0;
			        	
			        	// Extract data from result set
			        	while (rs.next()) {	
			        		
			        	    rowIndex++;
			        	    
			        	    // Collect row data as objects in a List
			        	    List<Object> rowData = new ArrayList<>();
			        	    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			        	        rowData.add(rs.getObject(i));
			        	    }
			     
			        	    sb.append("{");
			        	    String dataSeparator = "";
			        	    
			        	    for (int colIndex = 0; colIndex < rsmd.getColumnCount(); colIndex++) {
			        	        String objType = "null";
			        	        String objString = "";
			        	        Object columnObject = rowData.get(colIndex);
			        	        
			    				sb.append(dataSeparator);
			        	        
			        	        if (columnObject != null) {
			        	            objString = columnObject.toString() + " ";
			        	            objType = columnObject.getClass().getName();
			        	        }
			        	       
			        	        sb.append("\"" + columnNames.get(colIndex).toLowerCase() + "\"" + ":" );
			        	        if (objType.equals("java.math.BigDecimal")) {
			        	        	sb.append(objString);
			        	        } else {
			        	        	sb.append("\"" + objString + "\"");
			        	        }
			        	        dataSeparator = ", ";
			        	   
			        	    }
			        	    sb.append("},");
			        	}
			        	
			        	sb.append("]");
			        	
			        	log.info("Cantidad de Filas: " + rowIndex + "%n");
			        	log.info(sb.toString());

						return ResponseEntity.status(HttpStatus.OK).body(sb.toString());

					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

		} else if (request.getDataSource().equals("secondDataSource")) {

		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Connection not found!!!");

	}

}
