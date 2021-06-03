package com.connector.database.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.connector.database.model.Request;
import com.connector.database.yaml.Connector;
import com.connector.database.yaml.ConnectorDetails;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@Slf4j
@RequestMapping(path = { "v1/connector" }, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class ConnectorController {

	@PostMapping
	public ResponseEntity<String> getData(@RequestBody Request request) {

		Yaml yaml = new Yaml(new Constructor(Connector.class));
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("yaml/connector.yaml");
		Connector connector = yaml.load(inputStream);
		
		log.info(connector.toString());
		return ResponseEntity.status(HttpStatus.OK).body("Algo Paso");

	}

}
