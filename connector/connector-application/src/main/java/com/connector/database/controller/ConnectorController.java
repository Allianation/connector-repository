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
			var sql = getQuery(connector.getConnectorDetails().get(0).getQueryDetails(), request.getQueryName());
			try (var con = firstDataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery();) {
				return ResponseEntity.status(HttpStatus.OK).body(getJSON(rs));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (request.getDataSource().equals("secondDataSource")) {
			var sql = getQuery(connector.getConnectorDetails().get(1).getQueryDetails(), request.getQueryName());
			try (var con = secondDataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery();) {
				return ResponseEntity.status(HttpStatus.OK).body(getJSON(rs));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Connection not found!!!");
	}

	public String getQuery(List<QueryDetails> queryDetails, String queryName) {
		
		for (QueryDetails qd : queryDetails) {
			if (qd.getName().equals(queryName)) {
				return qd.getQuery();
			}
		}
		return null;
	}

	public String getJSON(ResultSet rs) throws SQLException {

		// Collect column names
		List<String> columnNames = new ArrayList<>();
		ResultSetMetaData rsmd = rs.getMetaData();
		for (var i = 1; i <= rsmd.getColumnCount(); i++) {
			columnNames.add(rsmd.getColumnLabel(i));
		}

		var sb = new StringBuilder("[");
		var rowIndex = 0;

		// Extract data from result set
		while (rs.next()) {

			rowIndex++;

			// Collect row data as objects in a List
			List<Object> rowData = new ArrayList<>();
			for (var i = 1; i <= rsmd.getColumnCount(); i++) {
				rowData.add(rs.getObject(i));
			}

			sb.append("{");
			var dataSeparator = "";

			for (var colIndex = 0; colIndex < rsmd.getColumnCount(); colIndex++) {
				var objType = "null";
				var objString = "";
				var columnObject = rowData.get(colIndex);

				sb.append(dataSeparator);

				if (columnObject != null) {
					objString = columnObject.toString() + " ";
					objType = columnObject.getClass().getName();
				}

				sb.append("\"" + columnNames.get(colIndex).toLowerCase() + "\"" + ":");
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

		log.info("Cantidad de Filas: " + rowIndex);
		log.info(sb.toString());

		return sb.toString();
	}

}
