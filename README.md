## Generate documentation

Generate locally maven documentation site from scratch.

```
mvn clean compile site:site -o -Dmaven.test.skip
```

Generate and push maven documentation site to GitHub Pages. Remember to create and use a Github temp token.
Execute from command line and provide GitHub username and temp token when asked.

```
mvnw clean compile site site:stage scm-publish:publish-scm -o -Dmaven.test.skip
```

## Run local Jetty

Hosts the maven documentation site locally for development purposes.

```
mvn site:run -o
```

## Generate Latex artifacts

```
mvn latex:latex
```

## Generate Plantuml diagrams

```
mvn plantuml:generate
```
