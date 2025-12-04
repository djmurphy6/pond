# Pond

**The Student Marketplace for the University of Oregon.**

Pond is a full-stack marketplace application designed to help university students buy, sell, and connect safely. It features secure authentication, real-time messaging, and an intuitive listing management system.

## Tech Stack

### Frontend
- Framework: Next.js 15 (React)
- Language: TypeScript
- Styling: Tailwind CSS and ShadCN UI
- State Management: Zustand
- Animations: Framer Motion

### Backend
- Framework: Java 21 and Spring Boot
- Security: Spring Security with JWT Authentication
- Database: PostgreSQL via Spring Data JPA
- Real-time: WebSocket with Stomp and SockJS
- Storage: Supabase Object Storage

## Features

- User Accounts: UO Email verification and secure JWT authentication
- Marketplace: Create, edit, and delete listings with image uploads
- Search and Filter: Fuzzy search logic and category filtering
- Real-time Messaging: Instant chat rooms between buyers and sellers
- Saved Listings: Bookmark items for later
- Reporting System: Report inappropriate listings for admin review
- Admin Dashboard: Tools for moderation and user management

## Getting Started

### Prerequisites

Ensure you have the following installed:

- Java JDK 21 or higher
- Node.js 18 or higher and npm
- Docker (optional for local database)

## 1. Database Setup

You can quickly spin up PostgreSQL using the included Docker Compose file.
```
cd server  
docker-compose up -d
```
This starts a local Postgres database on port 5432.

## 2. Backend Setup

Navigate to the server directory.
```
cd server
```
Create a .env file in the server directory or update src/main/resources/application.properties.

Example configuration:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydatabase  
SPRING_DATASOURCE_USERNAME=myuser  
SPRING_DATASOURCE_PASSWORD=secret  

SUPABASE_URL=https://your-project.supabase.co  
SUPABASE_SERVICE_ROLE_KEY=your_service_key  
SUPABASE_STORAGE_URL=https://your-project.supabase.co  

JWT_SECRET_KEY=your_secure_256bit_secret_key  
MAIL_USERNAME=your_email@gmail.com  
MAIL_PASSWORD=your_app_password  
```
Run the backend.
```
./mvnw spring-boot:run
```
The backend will start on http://localhost:8080.

## 3. Frontend Setup

Navigate to the client directory.
```
cd client
```

Install dependencies.
```
npm install
```
Configure the API url inside client/src/api/WebService.ts.

url: string = "http://localhost:8080";

Run the development server.
```
npm run dev
```
The app will be available at http://localhost:3000.

## Docker Support

To build and run the backend in Docker:
```
cd server  
docker build -t pond-server .  
docker run -p 8080:8080 --env-file .env pond-server
```
## Project Structure

client  
  src/app  
  src/components  
  src/api  

server  
  src/main/java/com/pond/server/controller  
  src/main/java/com/pond/server/service  
  src/main/java/com/pond/server/model  

## Attribution

This project utilizes the following open-source libraries:

- ShadCN UI  
- Apache Commons Text for fuzzy search algorithms  
- SockJS and StompJS for WebSocket communication  
