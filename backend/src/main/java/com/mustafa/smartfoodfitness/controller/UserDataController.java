package com.mustafa.smartfoodfitness.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.auth.security.TokenVersionService;
import com.mustafa.smartfoodfitness.dto.UserDataExportResponse;
import com.mustafa.smartfoodfitness.service.UserDataService;

@RestController
@RequestMapping("/api/user-data") // define a REST controller for the data-subject endpoints, with a base URL of "/api/user-data" to group the export and account-deletion endpoints together and allow for organized routing within the application
public class UserDataController {

    private final UserDataService userDataService;
    private final TokenVersionService tokenVersionService;

    public UserDataController(UserDataService userDataService, TokenVersionService tokenVersionService) {
        this.userDataService = userDataService;
        this.tokenVersionService = tokenVersionService;
    }

    // Neither endpoint accepts a user id in any form — the account acted on is always
    // the one in the verified JWT, so there is no identifier for a caller to tamper with.

    @GetMapping("/export") // handle HTTP GET requests to download a full copy of the authenticated user's own data as a single JSON document, covering their profile, goals, food entry logs, weight entries, water entries, workout logs, completed workouts, notifications and messages
    public UserDataExportResponse exportMyData() {
        return userDataService.exportCurrentUserData();
    }

    @DeleteMapping("/account") // handle HTTP DELETE requests to permanently erase the authenticated user's account and every record belonging to it, returning 204 No Content once the erasure has committed
    public ResponseEntity<Void> deleteMyAccount() {
        Long deletedUserId = userDataService.deleteCurrentUserAccount();

        // Deliberately outside the service's transaction: the token cache is not
        // transactional, so revoking before commit would outlive a rollback and lock a
        // still-live account out for good. Reached only once the erasure has committed,
        // and still before the 204 is written.
        tokenVersionService.revokeAll(deletedUserId);

        return ResponseEntity.noContent().build();
    }
}
