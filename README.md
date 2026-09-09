# Dispatch Load Balancer

A Spring Boot REST service that assigns delivery orders to a fleet of
vehicles, respecting vehicle capacity and order priority while minimizing
total travel distance (computed with the Haversine formula).

## Tech Stack

- Java 17
- Spring Boot 3.2.5 (Web, Data JPA, Validation)
- H2 in-memory database (upsert-friendly via `JpaRepository.save`/`saveAll`)
- JUnit 5 + Mockito + Spring `MockMvc` for tests
- Maven

## Project Structure

```
src/main/java/com/example/dispatch/
  controller/   OrderController, VehicleController, DispatchController
  model/        Order, Vehicle, Priority (JPA entities)
  dto/          Request/response payloads (validation + shaping API output)
  repository/   OrderRepository, VehicleRepository (Spring Data JPA)
  service/      DistanceCalculator (Haversine), DispatchService (assignment algorithm)
  exception/    DispatchException, ErrorResponse, GlobalExceptionHandler
src/test/java/com/example/dispatch/
  service/      DistanceCalculatorTest, DispatchServiceTest
  controller/   OrderControllerTest, DispatchControllerTest
```

## How the Optimization Works

`DispatchService.buildPlan()`:

1. Orders are sorted by priority — **HIGH → MEDIUM → LOW** — and, within a
   tier, heaviest-first (so large orders aren't stranded once vehicles fill up).
2. For each order, every vehicle with enough *remaining* capacity is a
   candidate. The candidate whose current position (start location, or the
   location of its last assigned order) is closest by Haversine distance is
   chosen — this keeps each vehicle's route local and minimizes total travel.
3. The chosen vehicle's remaining capacity, position, load and running
   distance are updated.
4. Any order that no vehicle can fit is returned in `unassignedOrders`
   instead of being silently dropped.

This greedy nearest-neighbor-with-priority approach is the standard
practical heuristic for this class of capacitated vehicle routing problem
(exact optimization is NP-hard), and runs in O(orders × vehicles), which
comfortably handles large datasets.

## Running the Project

### Prerequisites
- JDK 17+
- Maven 3.8+ (or use the included `mvnw` if you add one — a system Maven install works fine)

### Build & Run
```bash
cd dispatch-load-balancer
mvn spring-boot:run
```
The API starts on **http://localhost:8080**.

Alternatively, build a jar and run it:
```bash
mvn clean package
java -jar target/dispatch-load-balancer-1.0.0.jar
```

### Run the Tests
```bash
mvn test
```
This runs the Haversine distance unit tests, the dispatch algorithm unit
tests (priority ordering, capacity limits, nearest-vehicle selection,
unassignable-order handling), and full MockMvc integration tests for the
controllers.

> Note: this environment does not have outbound access to Maven Central, so
> the build could not be compiled inside the assistant's sandbox. The code
> has been manually reviewed for correctness; running `mvn test` on a normal
> machine with internet access will download the dependencies and execute
> the full suite.

## API Reference

### 1. Submit Delivery Orders
`POST /api/dispatch/orders`

```json
{
  "orders": [
    {
      "orderId": "ORD001",
      "latitude": 12.9716,
      "longitude": 77.5946,
      "address": "MG Road, Bangalore, Karnataka, India",
      "packageWeight": 10,
      "priority": "HIGH"
    }
  ]
}
```
Response `200 OK`:
```json
{ "message": "Delivery orders accepted.", "status": "success" }
```
Submitting an order with an `orderId` that already exists **upserts** it
(updates in place) rather than creating a duplicate.

Also available:
- `GET /api/dispatch/orders` — list all stored orders (handy for verifying state in Postman)
- `DELETE /api/dispatch/orders` — clear all orders (handy for resetting between test runs)

### 2. Submit Fleet Details
`POST /api/dispatch/vehicles`

```json
{
  "vehicles": [
    {
      "vehicleId": "VEH001",
      "capacity": 100,
      "currentLatitude": 12.9716,
      "currentLongitude": 77.6413,
      "currentAddress": "Indiranagar, Bangalore, Karnataka, India"
    }
  ]
}
```
Response `200 OK`:
```json
{ "message": "Vehicle details accepted.", "status": "success" }
```
- `GET /api/dispatch/vehicles` — list all stored vehicles
- `DELETE /api/dispatch/vehicles` — clear all vehicles

### 3. Get the Dispatch Plan
`GET /api/dispatch/plan`

Response `200 OK`:
```json
{
  "dispatchPlan": [
    {
      "vehicleId": "VEH001",
      "totalLoad": 10.0,
      "totalDistance": "5.32 km",
      "assignedOrders": [
        {
          "orderId": "ORD001",
          "latitude": 12.9716,
          "longitude": 77.5946,
          "address": "MG Road, Bangalore, Karnataka, India",
          "packageWeight": 10.0,
          "priority": "HIGH"
        }
      ]
    }
  ],
  "unassignedOrders": []
}
```
If some orders couldn't be placed on any vehicle (e.g. every vehicle is
already full), they show up here instead of vanishing:
```json
"unassignedOrders": [
  { "orderId": "ORD017", "reason": "No vehicle with sufficient remaining capacity (30.0) available" }
]
```

### Error Handling
- Missing/invalid fields (blank IDs, out-of-range lat/lon, non-positive
  weight/capacity, missing priority, etc.) → `400 Bad Request` with a list
  of field-level validation errors.
- Malformed JSON → `400 Bad Request`.
- `GET /api/dispatch/plan` called with no vehicles or no orders registered
  yet → `400 Bad Request` with a clear message telling you which one is
  missing.
- Any unexpected server error → `500 Internal Server Error`.

All errors share this shape:
```json
{
  "status": "error",
  "message": "Validation failed for the request payload",
  "details": ["packageWeight: packageWeight must be positive"],
  "timestamp": "2026-09-08T10:00:00Z"
}
```

## Testing with Postman

1. Import the three endpoints above into a Postman collection (or create
   requests manually) pointing at `http://localhost:8080`.
2. `POST /api/dispatch/orders` with the sample orders from the assignment
   (or your own).
3. `POST /api/dispatch/vehicles` with the sample vehicle fleet.
4. `GET /api/dispatch/plan` to retrieve the optimized dispatch plan.
5. Use `DELETE /api/dispatch/orders` and `DELETE /api/dispatch/vehicles` to
   reset state between test scenarios.

You can also inspect the in-memory H2 database directly at
`http://localhost:8080/h2-console` while the app is running
(JDBC URL: `jdbc:h2:mem:dispatchdb`, user `sa`, empty password) — useful for
debugging.
