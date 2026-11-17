package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.application.usecase.GetAvatarUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

/**
 * Serves profile photos. Public on purpose: an {@code <img>} tag can't send a bearer token,
 * and OAuth clients read the picture URL from the userinfo claims.
 */
@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
public class AvatarController {
    private final GetAvatarUseCase getAvatar;

    @GetMapping("/{uuid}")
    public ResponseEntity<byte[]> get(@PathVariable UUID uuid) {
        return getAvatar.execute(uuid)
                .map(avatar -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(avatar.contentType()))
                        // Each upload gets a new ?v= in its URL, so a long cache is safe.
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                        .lastModified(avatar.updatedAt())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(avatar.data()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
