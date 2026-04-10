Chatbox Backend
A real-time chat backend built with Spring Boot.
Users authenticate via JWT, then connect to WebSocket using a short-lived ticket. Messages are delivered instantly to online members and persisted to the database.
Tech Stack

Spring Boot, Spring Security, WebSocket
JWT + Ticket-based authentication
JPA / Hibernate
### Configuration
Create `src/main/resources/application.yml` and fill in:
```yaml
spring:
  datasource:
    url: your_database_url
    username: your_username
    password: your_password

app:
  jwt:
    secret: your_jwt_secret
```

Running
bashmvn spring-boot:run
API docs → http://localhost:8080/swagger-ui.html





<img width="1919" height="906" alt="image" src="https://github.com/user-attachments/assets/d6232675-9a33-437b-811c-d4173a52dc5f" />
<img width="1918" height="910" alt="image" src="https://github.com/user-attachments/assets/32f33359-4da8-485d-b396-43514c92d2e7" />
<img width="1919" height="886" alt="image" src="https://github.com/user-attachments/assets/bd638653-559e-4679-be75-7da0b3ce56d7" />



