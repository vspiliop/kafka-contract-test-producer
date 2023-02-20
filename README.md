## Generate documentation

Generate maven documentation site from scratch.

```
clean compile site:site -o -Dmaven.test.skip
```

## Run local Jetty

Hosts the maven documentation site locally for development purposes.

```
site:run -o
```

## Generate Latex artifacts

```
mvn mathan:latex -o
```

## Generate Plantuml diagrams

```
mvn plantuml:generate
```
