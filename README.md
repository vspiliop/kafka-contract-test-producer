# Generate Documentation

Generates maven documentation site from scratch.

```
clean compile site:site -o -Dmaven.test.skip
```

# Run local Jetty

Hosts the maven documentation site locally for development purposes.

```
site:run -o
```

# Generates Latex artifacts

```
maven mathan:latex -o
```

# Generates Plantuml diagrams

```
plantuml:generate
```
