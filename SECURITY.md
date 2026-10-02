# Security

Secret handling and leak prevention for the SmartFoodFitness backend.

## The rule: secrets live in the environment, never in the repository

Every credential the backend uses is read from an environment variable through a
Spring property placeholder, and the real secrets deliberately have **no default
value** — the app fails to start rather than falling back to something committed:

```properties
app.jwt.secret=${APP_JWT_SECRET}
anthropic.api.key=${ANTHROPIC_API_KEY}
usda.fdc.apiKey=${USDA_API_KEY}
wger.api-key=${WGER_API_KEY}
```

- **Production (Railway):** set them under the service's *Variables*. They are
  never written to a file in the image.
- **Local development:** `src/main/resources/application-local.properties`, which
  is gitignored. Keep it that way; do not move values into
  `application.properties`.
- The datasource entries in `application.properties` carry localhost defaults
  (`jdbc:postgresql://localhost:5432/…`, a throwaway local password). Those exist
  so a fresh clone runs against a local database — they must never be the values
  a deployed instance actually uses, and the deployed instance overrides all
  three via `SPRING_DATASOURCE_*`.

### Anything shipped to a client is public

This applies to the `frontend/` web app in this repo and to the React Native app
in `smartfoodfitness-mobile`. A browser bundle and an APK are both readable
archives — minification is not protection. Third-party API keys stay server side
and clients call our endpoints (`/api/ai/*`, `/api/foods/*`) instead of calling
the provider directly. The only credential a client holds is the user's own JWT,
issued at login.

## Automated prevention

Two dependency-free pieces, both version controlled:

| File | What it does |
| --- | --- |
| `.gitleaks.toml` | Rules for Anthropic keys, high-entropy key/token/secret assignments, AWS keys, PEM private-key blocks, JWT literals and GitHub tokens, plus an allowlist for `${ENV_VAR}` placeholders, build output and the committed `frontend/node_modules`. |
| `.githooks/pre-commit` | Scans **staged** content before every commit and aborts if it looks like a secret. |

### Enable the hook (once per clone)

```sh
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit   # macOS / Linux only
```

`core.hooksPath` is per-clone local config, so every developer and every fresh
clone has to run it — that is the cost of keeping the hook in version control
instead of in the untracked `.git/hooks/`.

### Install gitleaks (recommended, not required)

```sh
brew install gitleaks                                    # macOS
scoop install gitleaks                                   # Windows
go install github.com/zricethezav/gitleaks/v8@latest     # any platform with Go
# or a prebuilt binary: https://github.com/gitleaks/gitleaks/releases
```

The hook works either way. Without gitleaks it falls back to a grep sweep of the
staged diff covering the highest-signal patterns only (Anthropic keys, AWS key
IDs, PEM blocks, JWTs, GitHub tokens, quoted high-entropy assignments) and tells
you how to install the real thing. A missing tool never blocks a commit.

Only **added** lines of staged content are inspected, so a commit that *removes*
a secret always goes through.

Manual scans:

```sh
gitleaks git --staged --config .gitleaks.toml    # what the hook runs (>= 8.19)
gitleaks git --config .gitleaks.toml             # entire history
gitleaks dir --config .gitleaks.toml             # working tree
```

If a finding is a false positive, add an entry to the `[allowlist]` in
`.gitleaks.toml` — do not disable the hook. In a genuine emergency,
`git commit --no-verify` bypasses it.

## Known exposure: the old Anthropic key

An Anthropic API key (`sk-ant-api03-…`) was hardcoded in the **mobile** repo
(`smartfoodfitness-mobile`, `src/config.js`) and shipped inside the app bundle.
It is the same key this backend now uses as the `ANTHROPIC_API_KEY` environment
variable, which is why it matters here.

**Status: revoked.** The leaked value no longer authenticates, so this is
hygiene, not an open incident. The backend has always read the key from the
environment; the exposure came from the client, not from this repository.

### What is still true

The old value remains readable in the mobile repository's git history and in any
clone or fork taken before it was removed. Removing it from the current file does
not remove it from history. This repository's own history has not been scanned
end to end yet — run `gitleaks git --config .gitleaks.toml` once and record the
result.

### Options — the developer decides

1. **Leave history alone (reasonable here).** The key is dead, this is a
   university FYP with a small clone footprint, and rewriting history has real
   costs. Document it and move on.
2. **Rewrite history** with [`git-filter-repo`](https://github.com/newren/git-filter-repo)
   (preferred) or the [BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/):

   ```sh
   # from a fresh clone — filter-repo rewrites in place and is not reversible
   git filter-repo --replace-text secrets.txt   # secrets.txt: literal==>REDACTED
   ```

   Understand the consequences before doing this:

   - **Every commit hash after the first rewritten commit changes.** Tags,
     branches and any commit SHA referenced in an issue, PR or write-up become
     invalid.
   - It requires a **force-push** (`git push --force-with-lease --all --tags`),
     which rewrites the published branch. Anyone with an existing clone must
     re-clone or reset; a normal `git pull` will produce a mess.
   - **Forks, existing clones, and PR refs keep the old objects.** On GitHub the
     unreachable objects can stay served through the API until garbage
     collection — you have to ask GitHub Support to purge them.
   - Railway build logs and deployment history are outside git and unaffected
     either way.

Do not attempt a rewrite as part of an unrelated change, and never on a branch
someone else is working on.

## If a key leaks again

1. **Revoke and rotate it first** — at the provider, immediately. Everything else
   is secondary.
2. Remove it from the code and read it from an environment variable instead.
3. Set the new value in Railway → Variables (and in your local
   `application-local.properties`), then redeploy.
4. Decide about history using the section above.
5. Rotate anything that shared its blast radius — for `APP_JWT_SECRET` that means
   every issued token stops validating and users must log in again, which is the
   correct outcome.

## Reporting

This is a final-year project repository. Send anything security-relevant to the
maintainer directly rather than opening a public issue.

## Deployment check: rate limiting depends on the client IP being resolved correctly

`RateLimitingFilter` keys its per-client buckets on `request.getRemoteAddr()`. It used to read
the `X-Forwarded-For` header directly, which any caller can set — that made the limits
trivially bypassable by sending a different value each request, so the header handling was
removed.

`getRemoteAddr()` returns the *real* client IP only because `server.forward-headers-strategy=native`
puts Tomcat's `RemoteIpValve` in front of the filter chain. That valve rewrites the address from
the proxy headers **only when the immediate peer matches its trusted-proxy list** (by default the
private ranges: `10/8`, `172.16-31`, `192.168/16`, `127/8`, `169.254/16`, `::1`).

If Railway's edge reaches the container from an address outside that set, the valve leaves the
address as the proxy's own and **every user in the world shares a single bucket** — 5 logins per
minute, 3 registrations per minute and 20 AI calls per minute, globally. Two people signing in
during a demo would start seeing HTTP 429.

This cannot be reproduced locally. Verify it once against the deployed instance:

1. Log the value of `request.getRemoteAddr()` for a few requests from different devices/networks
   (or add a temporary debug endpoint that echoes it).
2. Distinct clients producing distinct addresses means the configuration is correct — nothing
   further is needed.
3. If they all collapse to one address, set `server.tomcat.remoteip.internal-proxies` in
   `application.properties` to a regex covering Railway's actual peer range.

Do **not** "fix" this by restoring the raw `X-Forwarded-For` read — that reintroduces the bypass.

Related, and worth knowing: brute-forcing a single account is still bounded regardless, because
`LoginAttemptService` locks an account after 5 consecutive failures per **email address**, which
does not depend on IP resolution at all.
