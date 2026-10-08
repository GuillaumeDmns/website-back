# Backend guillaumedamiens.com

API Spring Boot (Java 25) de l'appli de mobilité Île-de-France. Voir `CLAUDE.md` pour l'architecture.

## Développement (tout reste sur ta machine)

Le profil `dev` (par défaut) tape une **base PostgreSQL locale** ; la prod a sa propre base sur le VPS. Rien de la configuration de dev ne part en production : `application-dev.yml` est hors du dépôt et hors du jar.

1. Base locale (une fois) : PostgreSQL 18 + PostGIS installés, puis en superutilisateur (`sudo -u postgres psql`) :
   ```sql
   CREATE ROLE guillaumedamiens LOGIN PASSWORD 'guillaumedamiens';
   CREATE DATABASE guillaumedamiens OWNER guillaumedamiens;
   \c guillaumedamiens
   CREATE EXTENSION IF NOT EXISTS postgis;
   CREATE EXTENSION IF NOT EXISTS pg_trgm;
   CREATE EXTENSION IF NOT EXISTS unaccent;
   ```
2. Configuration : copier `src/main/resources/application-dev-example.yml` en `application-dev.yml` et la remplir. Pour le moment, la dev garde les **mêmes clés PRIM et le même secret JWT que la prod** : les tests locaux consomment les quotas de la prod, et un jeton de dev est valable en prod.
3. Lancer :
   ```bash
   ./mvnw spring-boot:run                 # http://localhost:8080
   ./mvnw clean package -DskipTests       # jar dans target/
   ```
   Au premier démarrage, Liquibase crée les tables ; le GTFS s'importe quelques minutes après (environ 3 min, 3,5 Go), puis à chaque nouvelle publication IDFM.

Les apps de dev (`website-app`, `website-react` en `npm run dev`) appellent ce backend sur `http://localhost:8080` ; un téléphone Android y accède par `adb reverse tcp:8080 tcp:8080`. Le CORS du profil `dev` n'accepte que `localhost` et `127.0.0.1` ; celui de la prod, seulement `guillaumedamiens.com` et `app.guillaumedamiens.com`. Bruno : environnement `Local`.

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

**Faite le 9 octobre 2026.** Le VPS est un conteneur LXC (ZAP-Hosting) : les directives systemd qui créent des namespaces (`PrivateTmp`, `ProtectSystem`…) y font échouer le service, l'unité ne les a plus. La configuration de prod vient de l'`application-dev.yml` du jar de mars (`/root/target/website-0.0.1-SNAPSHOT.jar.old`), le seul à contenir encore les secrets, plus le client Google.

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

### Connexion avec Google

Dans `/etc/guillaumedamiens/application-prod.yml`, sous `application:` :

```yaml
  google:
    client-ids:                 # clients OAuth dont les ID tokens sont acceptés (web, iOS, desktop)
      - 1234-web.apps.googleusercontent.com
  admin-emails:                 # emails Google vérifiés qui reçoivent ROLE_ADMIN à la connexion
      - ton.adresse@gmail.com
```

Sans client id, `POST /api/auth/google` répond 503. Aucun secret Google n'est nécessaire sur le serveur.

### App web (plus tard)

Le site `app.guillaumedamiens.com` est prêt dans `/etc/nginx/sites-available/` ; ses premières lignes expliquent comment l'activer une fois l'entrée DNS créée (lien dans `sites-enabled`, puis `certbot --nginx -d app.guillaumedamiens.com`). Les builds vont dans `/var/www/guillaumedamiens-app/releases/<version>/`, avec un lien `current`.

## Licence

Voir [LICENSE.md](LICENSE.md)
