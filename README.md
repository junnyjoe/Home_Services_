# Abidjan Prices — Application de comparaison des prix agricoles

Prototype minimal pour collecte, comparaison et visualisation des prix agricoles à Abidjan.

Run (dev):

1. En développement rapide sans PostgreSQL : lancer l'application avec le profil `dev` (H2 en mémoire, Flyway désactivé) :

```powershell
# si vous avez Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# ou (Windows) si vous avez le Maven Wrapper
#.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

2. Pour utiliser PostgreSQL (production ou persistance réelle) :

 - Créer la base `abidjan_prices` et définir les variables d'environnement ou mettre à jour `src/main/resources/application.yml` :

```powershell
# exemple pour créer la DB si psql est disponible
psql -U postgres -c "CREATE DATABASE abidjan_prices;"
```

 - Exemples d'env vars (ou configurez dans `application.yml`):
	 - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/abidjan_prices`
	 - `SPRING_DATASOURCE_USERNAME=postgres`
	 - `SPRING_DATASOURCE_PASSWORD=postgres`

3. Lancer en mode production (avec Flyway activé) :

```powershell
mvn spring-boot:run
# ou
.\mvnw.cmd spring-boot:run
```

Frontend: pages statiques dans `src/main/resources/static`.

Push to GitHub
----------------
If you want to publish this repository to GitHub under your account `bellaangemickael2006`, run the following from the project root:

```powershell
git config --global user.name "bellaangemickael2006"
git config --global user.email "bellaangemickael2006@gmail.com"

# (optional) remove large Chocolatey scratch files first
git rm -r --cached hoco/ChocolateyScratch || echo "no hoco folder"
echo "hoco/ChocolateyScratch/" >> .gitignore
git add .gitignore
git commit -m "Remove Chocolatey scratch files from repo" || echo "no changes"

# set remote (replace if needed)
git remote remove origin 2>$null || echo "no origin to remove"
git remote add origin https://github.com/bellaangemickael2006/abidjan_prices.git

# authenticate and push
gh auth login    # follow interactive prompt, choose HTTPS or SSH
git push -u origin main
```

If you prefer SSH, add your SSH key to GitHub and then run:

```powershell
git remote set-url origin git@github.com:bellaangemickael2006/abidjan_prices.git
git push -u origin main
```

Notes:
- Use `.\
vmw.cmd` on Windows to run the included Maven wrapper after running `.\n+scripts\setup-maven-wrapper.ps1` to download the wrapper jar.
- If you encounter permission errors (403), ensure you're authenticated with the correct GitHub account (`bellaangemickael2006`).
