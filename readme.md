## Run tests

Run normal tests that won't block the program:

```
sbt "run -n"
```

Or run only a specific runner:

```
sbt "run -n <runner class name>"
```

For example:

```
sbt "run -n BatchIOApp"
```

Test blocking queue can block the whole program:

```
sbt "run -b"
```
