## Generate documentation

Generate maven documentation site from scratch.

```
mvn clean compile site:site -o -Dmaven.test.skip
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
