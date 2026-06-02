# CI/CD process

## Goal

Every functional change starts from updated requirements, then code and tests are updated together. Only a green build can be promoted to VDS. Production deployment is manual.

## Flow

1. Update requirements in `README.md` or the relevant docs.
2. Implement the new version on a feature branch.
3. Update or add tests that prove the new behavior.
4. Open a merge request.
5. CI runs:
   - requirements/deploy file presence checks
   - backend tests: `gradle test`
   - frontend tests: `npm ci && npm test`
   - backend build: `gradle bootJar`
   - frontend build: `npm run build`
6. Merge only after the pipeline is green.
7. Create a release tag if the change should be traceable as a version, for example `v0.2.0`.
8. Run the manual `deploy_vds` job.
9. Verify `https://<domain>/diary/login` and key user flows after deploy.

## Manual approval

The `deploy_vds` job is manual by design. Use it for:

- production deploys to VDS;
- database schema changes;
- auth / Keycloak / SSL changes;
- rollback operations.

## Required GitLab CI variables

Configure these variables in GitLab project settings. Mark secrets as protected and masked where possible.

| Variable | Required | Description |
| --- | --- | --- |
| `VDS_HOST` | yes | VDS host or IP |
| `VDS_USER` | yes | SSH user, for example `root` |
| `VDS_SSH_PRIVATE_KEY` | yes | Private key with access to VDS |
| `VDS_APP_PATH` | yes | Project path on VDS, for example `/root/mooddiary` |
| `VDS_DOMAIN` | yes | Public domain, for example `nonmagicfly.ru` |

The VDS itself keeps production secrets in `deploy/.env`. The CI job does not print or copy those secrets.

## VDS prerequisites

The target VDS directory must be a git checkout of this repository:

```bash
cd /root/mooddiary
git remote -v
```

The deployment script expects:

- Docker and Docker Compose installed;
- `deploy/.env` present on the VDS;
- GlobalSign certificates available through the configured `SSL_DIR`;
- ports `80` and `443` free for `mooddiary-nginx`.

## Rollback

Use the same manual deploy job against a previous stable commit/tag, or run on VDS:

```bash
cd /root/mooddiary
git fetch --all --tags --prune
git checkout --detach <stable-commit-or-tag>
cd deploy
./deploy.sh
```
