# Algorithm Comparison

| Metric | Fixed Window | Sliding Window |
|--------|--------------|----------------|
| Throughput | X req/s | Y req/s |
| Memory | X MB | Y MB |
| Boundary Burst | Poor | Better |
| Complexity | O(1) | O(1) |

Observations:

- Sliding Window improves burst handling.
- Memory overhead is slightly higher.
- Throughput remains comparable.