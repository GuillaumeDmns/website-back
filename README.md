# Backend guillaumedamiens.com

API Spring Boot (Java 25) de l'appli de mobilité Île-de-France. Voir `CLAUDE.md` pour l'architecture.

## Lancer en local

```bash
./mvnw spring-boot:run                 # profil dev, secrets dans src/main/resources/application-dev.yml (non versionné)
./mvnw clean package -DskipTests       # jar dans target/
```

## Production

Le VPS (Debian 13) fait tourner :

- le service systemd `guillaumedamiens` (`deploy/guillaumedamiens.service`), sous l'utilisateur système `guillaumedamiens`, avec le jar `/opt/guillaumedamiens/current.jar` (lien vers `releases/`) et le profil Spring `prod` ;
- ses secrets dans `/etc/guillaumedamiens/application-prod.yml` (640, `root:guillaumedamiens`) ; le jar n'en contient aucun ;
- nginx devant l'API (`/api/` sur `guillaumedamiens.com`), le site React et l'app web Flutter (`app.guillaumedamiens.com`).

Logs : `journalctl -u guillaumedamiens -f`. Santé : `curl http://127.0.0.1:8080/api/health`.

### Déployer

Pousser un tag sur un commit de `master` :

```bash
git tag v1.2.3 && git push origin v1.2.3
```

Le workflow `Deploy` builde le jar, l'envoie dans `/opt/guillaumedamiens/releases/` avec l'utilisateur `deploy`, puis lance `deploy/activate-release.sh` : bascule de `current.jar`, redémarrage, attente de `/api/health` (3 min au plus), retour automatique à la version précédente sinon. Les 5 dernières versions restent sur le disque.

Revenir à une version encore présente dans `releases/`, depuis ton poste :

```bash
ssh -i ~/.ssh/guillaumedamiens_deploy deploy@<vps> bash -s -- website-v1.2.2-abc1234.jar < deploy/activate-release.sh
```

Secrets GitHub du dépôt (ou de l'environnement `production`) :

| Secret | Valeur |
|---|---|
| `DEPLOY_HOST` | nom ou IP du VPS |
| `DEPLOY_SSH_KEY` | clé privée de l'utilisateur `deploy` |
| `DEPLOY_KNOWN_HOSTS` | sortie de `ssh-keyscan <hôte>` |

### Migration initiale du serveur (une seule fois)

Avant : le service tournait en `root` depuis `/root/target`, sans profil, avec les secrets embarqués dans le jar. Ne plus déployer de jar buildé depuis ce code avant cette migration : il ne contient plus `application-dev.yml`.

1. En local, builder le jar et créer la clé de déploiement :
   ```bash
   ./mvnw clean package -DskipTests
   ssh-keygen -t ed25519 -N "" -C github-actions-deploy -f ~/.ssh/guillaumedamiens_deploy
   ssh root@<vps> mkdir -p /root/migration
   scp -r deploy target/website-0.0.1-SNAPSHOT.jar ~/.ssh/guillaumedamiens_deploy.pub root@<vps>:/root/migration/
   ```
2. Sur le VPS, en `root` :
   ```bash
   cd /root/migration
   bash deploy/migrate-server.sh website-0.0.1-SNAPSHOT.jar guillaumedamiens_deploy.pub
   ```
   Le script crée les utilisateurs `guillaumedamiens` et `deploy`, copie la configuration du jar actuel dans `/etc/guillaumedamiens/application-prod.yml` (il en affiche les clés, valeurs masquées : vérifier qu'il n'y reste rien de propre au poste de dev), installe le jar et l'unité, donne à `deploy` le seul droit de redémarrer le service, puis redémarre. Si le service ne répond pas, l'ancienne unité est remise.
3. Toujours sur le VPS, appliquer la configuration nginx :
   ```bash
   bash deploy/nginx/apply.sh
   ```
   Compression, logs de l'API sans les paramètres (positions), limite de débit par IP, en-têtes de sécurité, réponse JSON pendant les redémarrages. Le bloc `location /api` du site `guillaumedamiens.com` est remplacé par le snippet ; tout est remis si `nginx -t` échoue (copies dans `/root/nginx-backup-*`).
4. Vérifier que `deploy` peut se connecter (si `sshd_config` restreint les utilisateurs avec `AllowUsers`, l'y ajouter), puis renseigner les secrets GitHub :
   ```bash
   ssh -i ~/.ssh/guillaumedamiens_deploy deploy@<vps> true
   ssh-keyscan <vps>        # → DEPLOY_KNOWN_HOSTS
   ```
5. Après quelques jours sans souci : supprimer `/root/target` (anciens jars avec les secrets, logs depuis 2025) et la copie de l'ancienne unité. Changer alors le secret JWT dans `/etc/guillaumedamiens/application-prod.yml` (tout le monde est déconnecté) et, si possible, les clés PRIM.

### App web (plus tard)

Le site `app.guillaumedamiens.com` est prêt dans `/etc/nginx/sites-available/` ; ses premières lignes expliquent comment l'activer une fois l'entrée DNS créée (lien dans `sites-enabled`, puis `certbot --nginx -d app.guillaumedamiens.com`). Les builds vont dans `/var/www/guillaumedamiens-app/releases/<version>/`, avec un lien `current`.

## Licence

Voir [LICENSE.md](LICENSE.md)
