package com.github.kat_ka.spend_the_night.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.kat_ka.spend_the_night.conversion.JsonConverter;
import com.github.kat_ka.spend_the_night.model.item.PlaceOffer;
import com.github.kat_ka.spend_the_night.service.PlaceOfferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.validation.Valid;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@ApiResponse(responseCode = "401", description = "User authentication failed", content = @Content)
@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content)
@RestController
@RequestMapping("/place_offers")
@Slf4j
public class PlaceOfferController {

	@Autowired
	private PlaceOfferService placeOfferService;

	@Autowired
	private JsonConverter jsonConverter;

	@Operation(description = "Get place offer by id")
	@ApiResponse(responseCode = "200", description = "Success")
	@ApiResponse(responseCode = "404", description = "PlaceOffer doesn't exist", content = @Content)
	@ApiResponse(responseCode = "400", description = "Path variable id is invalid or missing", content = @Content)
	@GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<PlaceOffer> getPlaceOffer(@PathVariable final UUID id) {
		log.info("PlaceOffer GET request for id value '{}'", id);
		final PlaceOffer placeOffer = placeOfferService.getPlaceOffer(id);
		return (placeOffer != null) ? ResponseEntity.ok(placeOffer) : responseEntity(NOT_FOUND);
	}

	@Operation(description = "Find all place offers for a given user name")
	@ApiResponse(responseCode = "200", description = "Success")
	@ApiResponse(responseCode = "400", description = "Request parameter userName is missing or empty", content = @Content)
	@Parameters({
		@Parameter(in = QUERY, name = "page", schema = @Schema(type = "string")),
		@Parameter(in = QUERY, name = "size", schema = @Schema(type = "string")),
		@Parameter(in = QUERY, name = "sort", content = @Content(array = @ArraySchema(schema = @Schema(type = "string")))),
		@Parameter(in = QUERY, name = "pageable", hidden = true) })
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PlaceOffer>> getPlaceOffers(
			@RequestParam String userName,
			@SortDefault(sort = "priceInEuro", direction = Sort.Direction.ASC) final Pageable pageable) {
		log.info("PlaceOffer GET request for userName value '{}' and Pageable instance: {}",
				userName, pageable);
		final Page<PlaceOffer> placeOffers = placeOfferService.getPlaceOffers(userName, pageable);
		return ResponseEntity.ok(placeOffers);
	}

	@Operation(description = "Add a new place offer using a generated id")
	@ApiResponse(responseCode = "201", description = "Success")
	@ApiResponse(responseCode = "400", description = "Invalid data input", content = @Content)
	@ApiResponse(responseCode = "415", description = "Invalid format", content = @Content)
	@PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<PlaceOffer> addPlaceOffer(@Valid @RequestBody final PlaceOffer placeOffer) {
		log.info("PlaceOffer POST request for PlaceOffer instance '{}'", jsonConverter.toJson(placeOffer));
		final PlaceOffer responsePlaceOffer = placeOfferService.addPlaceOffer(placeOffer);
		return ResponseEntity.status(CREATED).body(responsePlaceOffer);
	}

	@Operation(description = "Update an existing place offer")
	@ApiResponse(responseCode = "200", description = "Success")
	@ApiResponse(responseCode = "404", description = "PlaceOffer doesn't exist")
	@ApiResponse(responseCode = "400", description = "Invalid data input")
	@ApiResponse(responseCode = "415", description = "Invalid format")
	@PutMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> updatePlaceOffer(
			@PathVariable final UUID id,
			@Valid @RequestBody final PlaceOffer placeOffer) {
		log.info("PlaceOffer PUT request for id value '{}' and PlaceOffer instance '{}'",
				id, jsonConverter.toJson(placeOffer));
		return (placeOfferService.updatePlaceOffer(id, placeOffer)) ? responseEntity(OK) : responseEntity(NOT_FOUND);
	}

	@Operation(description = "Delete an existing place offer")
	@ApiResponse(responseCode = "204", description = "Success")
	@ApiResponse(responseCode = "404", description = "PlaceOffer doesn't exist")
	@ApiResponse(responseCode = "400", description = "Path variable id is invalid or missing")
	@DeleteMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deletePlaceOffer(@PathVariable final UUID id) {
		log.info("PlaceOffer DELETE request for id value '{}'", id);
		return (placeOfferService.deletePlaceOffer(id)) ? responseEntity(NO_CONTENT) : responseEntity(NOT_FOUND);
	}

	private static <T> ResponseEntity<T> responseEntity(HttpStatus status) {
		return ResponseEntity
					.status(status)
					.contentType(APPLICATION_JSON)
					.build();
	}
}
