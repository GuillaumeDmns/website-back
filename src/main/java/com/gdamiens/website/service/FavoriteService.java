package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.FavoriteDto;
import com.gdamiens.website.controller.object.v2.FavoriteRequest;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.model.Favorite;
import com.gdamiens.website.model.Favorite.Kind;
import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.FavoriteRepository;
import com.gdamiens.website.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Favorites of the signed-in user. Stop areas and lines are stored by id and returned with their current GTFS data.
 */
@Service
public class FavoriteService {

    private static final int MAX_FAVORITES = 200;

    private static final int MAX_LABEL_LENGTH = 200;

    private final FavoriteRepository favoriteRepository;

    private final UserRepository userRepository;

    private final NetworkService networkService;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, NetworkService networkService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.networkService = networkService;
    }

    @Transactional(readOnly = true)
    public List<FavoriteDto> list(String login) {
        return favoriteRepository.findByUserIdOrderByIdAsc(user(login).getId()).stream().map(this::toDto).toList();
    }

    /**
     * Saves a favorite: replaces the home / work, returns the existing favorite for a stop area or a line saved twice.
     */
    @Transactional
    public FavoriteDto add(String login, FavoriteRequest request) {
        User user = user(login);
        if (request == null || request.kind() == null) {
            throw new CustomException("Favorite kind is required", HttpStatus.BAD_REQUEST);
        }

        Favorite favorite = switch (request.kind()) {
            case STOP -> {
                StopAreaSummary stop = stopArea(request.stopAreaId());
                yield favoriteRepository.findFirstByUserIdAndKindAndStopAreaId(user.getId(), Kind.STOP, stop.id())
                    .orElseGet(() -> {
                        Favorite created = newFavorite(user, Kind.STOP);
                        created.setStopAreaId(stop.id());
                        created.setLabel(stop.name());
                        created.setLat(stop.lat());
                        created.setLon(stop.lon());
                        return created;
                    });
            }
            case LINE -> {
                String lineId = networkService.getLine(StringUtils.trimToEmpty(request.lineId()))
                    .orElseThrow(() -> new CustomException("Unknown line", HttpStatus.BAD_REQUEST))
                    .id();
                yield favoriteRepository.findFirstByUserIdAndKindAndLineId(user.getId(), Kind.LINE, lineId)
                    .orElseGet(() -> {
                        Favorite created = newFavorite(user, Kind.LINE);
                        created.setLineId(lineId);
                        return created;
                    });
            }
            case HOME, WORK, PLACE -> {
                Favorite place = request.kind() == Kind.PLACE
                    ? newFavorite(user, Kind.PLACE)
                    : favoriteRepository.findFirstByUserIdAndKind(user.getId(), request.kind()).orElseGet(() -> newFavorite(user, request.kind()));
                fillPlace(place, request);
                yield place;
            }
        };

        return toDto(favoriteRepository.save(favorite));
    }

    /**
     * @throws CustomException 404 if the favorite does not exist or belongs to someone else
     */
    @Transactional
    public void delete(String login, Long id) {
        Favorite favorite = favoriteRepository.findByIdAndUserId(id, user(login).getId())
            .orElseThrow(() -> new CustomException("Favorite not found", HttpStatus.NOT_FOUND));
        favoriteRepository.delete(favorite);
    }

    private Favorite newFavorite(User user, Kind kind) {
        if (favoriteRepository.findByUserIdOrderByIdAsc(user.getId()).size() >= MAX_FAVORITES) {
            throw new CustomException("Too many favorites", HttpStatus.BAD_REQUEST);
        }
        return new Favorite(user, kind);
    }

    private void fillPlace(Favorite favorite, FavoriteRequest request) {
        String label = StringUtils.trimToNull(request.label());
        if (label == null || label.length() > MAX_LABEL_LENGTH) {
            throw new CustomException("A label of at most " + MAX_LABEL_LENGTH + " characters is required", HttpStatus.BAD_REQUEST);
        }
        if (request.lat() == null || request.lon() == null || Math.abs(request.lat()) > 90 || Math.abs(request.lon()) > 180) {
            throw new CustomException("Invalid position", HttpStatus.BAD_REQUEST);
        }

        favorite.setLabel(label);
        favorite.setLat(request.lat());
        favorite.setLon(request.lon());
        favorite.setStopAreaId(StringUtils.isBlank(request.stopAreaId()) ? null : stopArea(request.stopAreaId()).id());
    }

    private StopAreaSummary stopArea(String stopAreaId) {
        return networkService.getStopAreaSummary(StringUtils.trimToEmpty(stopAreaId))
            .orElseThrow(() -> new CustomException("Unknown stop area", HttpStatus.BAD_REQUEST));
    }

    private FavoriteDto toDto(Favorite favorite) {
        StopAreaSummary stop = favorite.getKind() == Kind.STOP
            ? networkService.getStopAreaSummary(favorite.getStopAreaId()).orElse(null)
            : null;
        return new FavoriteDto(
            favorite.getId(),
            favorite.getKind(),
            favorite.getLabel(),
            favorite.getLat(),
            favorite.getLon(),
            favorite.getStopAreaId(),
            stop,
            favorite.getKind() == Kind.LINE ? networkService.getLine(favorite.getLineId()).orElse(null) : null);
    }

    private User user(String login) {
        return Optional.ofNullable(login)
            .flatMap(userRepository::getByLoginIgnoreCase)
            .orElseThrow(() -> new CustomException("Unknown user", HttpStatus.UNAUTHORIZED));
    }
}
