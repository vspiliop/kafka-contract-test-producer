## Generate documentation

Generate locally maven documentation site from scratch.

```
mvn clean compile site:site -o -Dmaven.test.skip
```

## Generate and push maven documentation site to GitHub Pages

```
clean compile site site:stage scm-publish:publish-scm -o -Dmaven.test.skip
```

## Run local Jetty

Hosts the maven documentation site locally for development purposes.

```
mvn site:run -o
```

## Generate Latex artifacts

```
mvn mathan:latex -o
```

## Generate Plantuml diagrams

```
mvn plantuml:generate
```
