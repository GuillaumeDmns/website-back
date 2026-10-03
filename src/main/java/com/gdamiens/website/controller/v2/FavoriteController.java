package com.gdamiens.website.controller.v2;

import com.gdamiens.website.controller.object.v2.FavoriteDto;
import com.gdamiens.website.controller.object.v2.FavoriteRequest;
import com.gdamiens.website.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Favorites of the signed-in user, synced between their devices.
 */
@RestController
@RequestMapping("/api/v2/me/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    @Operation(summary = "Favorites of the user, oldest first", security = @SecurityRequirement(name = "Auth. Token"))
    public List<FavoriteDto> list(Principal principal) {
        return favoriteService.list(principal.getName());
    }

    @PostMapping
    @Operation(summary = "Save a favorite (home/work are replaced, a stop area or line saved twice is returned as is)",
        security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<FavoriteDto> add(Principal principal, @RequestBody FavoriteRequest request) {
        return new ResponseEntity<>(favoriteService.add(principal.getName(), request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a favorite", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        favoriteService.delete(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
