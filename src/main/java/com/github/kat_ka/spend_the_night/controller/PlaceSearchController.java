package com.github.kat_ka.spend_the_night.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.kat_ka.spend_the_night.model.item.PlaceSearchResult;
import com.github.kat_ka.spend_the_night.service.PlaceSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@ApiResponse(responseCode = "401", description = "User authentication failed", content = @Content)
@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content)
@RestController
@RequestMapping("/place_search")
@Slf4j
public class PlaceSearchController {

	@Autowired
	private PlaceSearchService placeSearchService;

	@Operation(description = "Find all place offers for a given town")
	@ApiResponse(responseCode = "200", description = "Success")
	@ApiResponse(responseCode = "400", description = "Request parameter town or userName is missing or empty", content = @Content)
	@Parameters({
		@Parameter(in = QUERY, name = "page", schema = @Schema(type = "string")),
		@Parameter(in = QUERY, name = "size", schema = @Schema(type = "string")),
		@Parameter(in = QUERY, name = "sort", content = @Content(array = @ArraySchema(schema = @Schema(type = "string")))),
		@Parameter(in = QUERY, name = "pageable", hidden = true) })
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PlaceSearchResult>> getPlaceSearchResults(
			@RequestParam String userName,
			@RequestParam String town,
			@SortDefault(sort = "priceInEuro", direction = Sort.Direction.ASC) final Pageable pageable) {
		log.info("PlaceSearch GET request for userName value '{}' and town value '{}' and Pageable instance: {}",
				userName, town, pageable);
		final Page<PlaceSearchResult> placeSearchResults = placeSearchService.getPlaceSearchResults(
				userName, town, pageable);
		return ResponseEntity.ok(placeSearchResults);
	}
}
