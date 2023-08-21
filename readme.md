
## Test results

### (Almost) Balanced queue
```
min consume(10) < produce(1000) < max consume(2000)
Stream: Time used: 72006.227658 ms
Stream queue: Time used: 62360.921699 ms
```

### Fast consume queue
```
min consume(10) < max consume(1000) = produce(1000)
Stream: Time used: 53360.905145 ms
Stream queue: Time used: 51660.810424 ms
```
