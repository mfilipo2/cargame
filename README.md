# CarGame

Recruitment task.

### Table of contents
- [Project realisation details](#project-realisation-details)     
  - [Technological stack](#technological-stack)     
  - [Project Structure](#project-structure)     
  - [Backend application](#backend-application)     
  - [Frontend application](#frontend-application)   
  - [Assumptions](#assumptions)     
  - [Used design patterns](#used-design-patterns)   
- [How to run](#how-to-run)   
  - [With Docker](#with-docker)     
  - [With Maven](#with-maven)       
  - [For development](#for-development)     
- [API Documentation (Swagger UI)](#api-documentation-swagger-ui) 

#### Live demo  
If you prefer not to build and install required staff, you can check our demo [here](http://cargame.pl).

You can use example maps to for the test purpose (in directory `maps`):
1. map_5x5.csv (size: 5):   
0,1,0,0,0   
0,1,1,1,0   
0,1,0,1,1   
0,1,0,1,0   
1,1,1,1,1  

2. map_8x8.csv (size: 8):  
1,1,1,1,1,1,1,1     
1,0,0,0,0,1,0,1     
1,0,1,1,1,1,0,1     
1,0,0,0,0,1,0,1     
1,0,0,1,0,1,0,1     
1,0,0,1,0,0,0,1     
1,0,0,1,0,0,0,1        
1,1,1,1,1,1,1,1     

## Project realisation details
#### Technological stack
Used libraries, tools, modules etc.: 

For backend application:
* **Java 8**
* **Spring Boot** v2.0.1.RELEASE
* Spring v5.0.5.RELEASE
* Spring Data / JPA
* Hibernate 5.2.16.Final
* Spock
* Lombok
* Liquibase
* Swagger
* for demo purpose, with integrated H2 database engine (can be replaced with any relational database like PostgreSQL or MySQL)

For frontend application:
* **Vue.js** v2.5.x
* Axios
* Bootstrap 4.0
* Bootstrap-Vue 2.0
* Node.js / NPM / Webpack

#### Project Structure
Project has been divided into two applications: 
1. Backend (Spring Boot app)
2. Frontend (Vue.Js)

##### Backend application
Backend application is responsible for fulfilling all business functionalities and it's divided into few main parts/submodules:
1. REST/JSON API (`pl.speedapp.cargame.api`) - providing all needed endpoints regarding to requirements and resulting from frontend application needs. If you want you can browse API by [Swagger](#api-documentation-swagger-ui)
2. Entities, repositories, converters etc. (`pl.speedapp.cargame.db`) - providing all components to support communication with database via Hibernate and Spring Data (JPA repositories, CriteriaQueries) 
3. Services (`pl.speedapp.cargame.service`) to support business logic of the application
4. Game engine (`pl.speedapp.cargame.engine`) - the main part of the application. Here is the all logic of the game, moving cars, manage games, maps etc. Other application's components can communicate with the engine via dedicated component `GameManager`.
This component is responsible for performing all operations on games, cars and grids (`Grid` is a representation of the map).
5. Unit test - unit test to check if all business requirements are met. Tests are written using Spock (Groovy). For know, we don't cover all classes and methods, but we concentrate especially to test `Game engine`.

##### Frontend application
Frontend application is based on Vue.Js library as a new, modern a light JavaScript progressive framework for Single Page Applications. 
On the front, we used Bootstrap with special implementation for Vue.js - Bootstrap-vue.js which speed-up development of new components (without JQuery or similar frameworks).
In this part on our project, we wanted to show all business functionalities available on the backend and we've added one cool feature: presentation of current game status with map and cars, and possibility to move cars in real time. For this feature we had to add few new endpoints and extends some of the DTO used by API, but without influence on base requirements.

#### Assumptions
1. Positions on the map are indexed from top-left corner starting, so the most top-left corner has coordinates (x=1,y=1) and the most bottom-right as coordinates (x=N, y=N) where N is a number of columns/rows.
2. Map name is case-sensitive.
3. Map validations:
    - number of columns and rows should be the same
    - possibility to reach every point on the map from any other point
4. RACER car can move up to 2 fields per one move and we don't check if field between starting and target point is empty or if there is a wall.
5. Cars moves are stored in database only if move request has been accepted and executed by the car.
6. The requirements for the functionality 'back N-moves in the history' does not clearly specified what to do with historical moves when back in the history option is fired.
We adopted assumptions: If you make back N-movements in the history, the reverse moves are not stored in database. 
It implicate that if you fire again a back in the history, car can crashed (on a wall or move outside the map). 
For the future of the application, it could be good to for example removed that N-moves from the car history. 
7. For the purpose of UI, we added some extra endpoints and components to cover UI functionalities, for example: `pl.speedapp.cargame.api.controller.RunningGameController` which use `RunningGameDto`, `CarStatusDto` (contains 'real-time` information about cars in the game)
8. A game is starting automatically when created, no need to perform any other actions to start the game.

#### Used design patterns
We tried to use as many design patterns as we can. Hence you can find for example:
1. Strategy design (used in Game engine for Movements strategy: `pl.speedapp.cargame.engine.grid.movement`)
2. Factory (used for example to create proper strategy for different type of Car: `pl.speedapp.cargame.engine.grid.movement.MovementStrategyFactory` or for create grid object: `pl.speedapp.cargame.engine.grid.objects.GridObjectFactory`)
3. Builder (in DTOs: `pl.speedapp.cargame.api.model`)
4. Singleton (for example: all API RestControllers, services in `pl.speedapp.cargame.service`)
5. Observer (when reading CSV files line by line: `pl.speedapp.cargame.util.GameMapUtil`)
6. Producer-Consumer (used for example in Game Engine in components: `Game`, `Car` and `GameManager`, where we use different queues to communicate between running components/threads)
7. Facade (REST API as a way to interact with more complex system with simple, standardized interface)

## How to run

### With Docker
#### Prerequisites

- Installed docker with engine version 1.13.0+
- Installed docker-compose

#### Command to run

- Run Application:
`docker-compose up`

#### Parameters

- `DISABLE_CORS` - If true, CORS will be disabled in entire application (use it carefully), `default: false`
- `GAME_DURATION` - Game duration in seconds, `default: 30`
- `BACK_IN_HISTORY_DELAY` - Delay (in milliseconds) between each moves while moving back in the history of car movements, `default: 1000`

#### Run With parameters

Edit file `variables.env` and run:

- `docker-compose up`

Done, open [Application](http://localhost/) in your browser.

### With Maven
#### Prerequisites
- Installed Apache Maven version 3.3.9+

#### Parameters

- `DISABLE_CORS` - If true, CORS will be disabled in entire application (use it carefully), `default: false`
- `GAME_DURATION` - Game duration in seconds, `default: 30`
- `BACK_IN_HISTORY_DELAY` - Delay (in milliseconds) between each moves while moving back in the history of car movements, `default: 1000`

#### Commands to run

- Download all dependencies:
`./mvnw install`

- Run application:
`cd backend; ../mvnw spring-boot:run`

Done, open [Application](http://localhost:8888/) in your browser.

### For development

- Run `CargameApplication` in your IDE with enviroment variable `DISABLE_CORS=true`
- Go to `frontend` directory and run `npm run dev`

Done, open [Dev Application](http://localhost:8080/) in your browser.

## API Documentation (Swagger UI)

Swagger UI is available here: [when using Docker Compose](http://localhost/swagger-ui.html) or otherwise:  [here](http://localhost:8888/swagger-ui.html).