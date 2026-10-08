package com.gdamiens.website.controller.object.v2;

import java.time.Instant;
import java.util.List;

/** Everything the server keeps about an account (GDPR right of access): the account and its favorites */
public record AccountExport(Instant exportedAt, Account account, List<FavoriteDto> favorites) {
}
