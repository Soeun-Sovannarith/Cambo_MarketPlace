# Cambo MarketPlace - Real-Time Chat System

A Spring Boot marketplace application with real-time chat functionality using WebSocket (STOMP over SockJS).

## Table of Contents
- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Chat API Architecture](#chat-api-architecture)
- [REST API Endpoints](#rest-api-endpoints)
- [WebSocket Communication](#websocket-communication)
- [Database Schema](#database-schema)
- [Setup and Configuration](#setup-and-configuration)
- [How It Works](#how-it-works)

## Overview

This marketplace application allows buyers and sellers to communicate in real-time through product-specific chat rooms. When a buyer clicks on a product's "Chat with Seller" button, a chat room is automatically created (or retrieved if it already exists) for that specific buyer-seller-product combination.

Key features:
- ✅ Automatic chat room creation per product-buyer-seller combination
- ✅ Real-time bidirectional messaging using WebSocket
- ✅ Personal notification system for each user
- ✅ Message persistence in PostgreSQL database
- ✅ RESTful API for chat history and room management

## Technology Stack

- **Backend**: Spring Boot 4.0.1, Java 21
- **Database**: PostgreSQL
- **Real-time**: WebSocket (STOMP protocol over SockJS)
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven

## Chat API Architecture

### Core Components

1. **ChatController** - Handles WebSocket message routing
2. **ChatRoomController** - REST API for chat room management
3. **ChatService** - Business logic for chat operations
4. **Models**: User, Product, ChatRoom, Message
5. **Repositories**: JPA repositories for database access

### Data Models

```java
User {
  id: Long
  username: String
  email: String
  role: String
}

Product {
  id: Long
  title: String
  description: String
  price: Double
  status: String (AVAILABLE, SOLD)
  seller: User (ManyToOne)
}

ChatRoom {
  id: Long
  buyer: User (ManyToOne)
  seller: User (ManyToOne)
  product: Product (ManyToOne)
  createdAt: LocalDateTime
}

Message {
  id: Long
  content: String
  sender: User (ManyToOne)
  chatRoom: ChatRoom (ManyToOne)
  sentAt: LocalDateTime
}
```

## REST API Endpoints

### Product API

#### Get All Products
```http
GET /api/products
```
**Response:**
```json
[
  {
    "id": 1,
    "title": "iPhone 14",
    "description": "Barely used, excellent condition",
    "price": 899.99,
    "status": "AVAILABLE",
    "seller": {
      "id": 1,
      "username": "alice"
    }
  }
]
```

### Chat Room API

#### 1. Create or Get Chat Room
Automatically creates a new chat room or retrieves existing one for a buyer-seller-product combination.

```http
POST /api/chatrooms/create-or-get
Content-Type: application/json

{
  "productId": 1,
  "buyerId": 2,
  "sellerId": 1
}
```

**Response:**
```json
{
  "chatRoomId": 1,
  "buyerId": 2,
  "buyerUsername": "bob",
  "sellerId": 1,
  "sellerUsername": "alice",
  "productId": 1,
  "productTitle": "iPhone 14"
}
```

**Logic:**
- Checks if a chat room already exists for this buyer-seller-product combination
- If exists: returns existing chat room
- If not: creates new chat room and returns it

#### 2. Get User's Chat Rooms
Retrieves all chat rooms where the user is either a buyer or seller.

```http
GET /api/chatrooms/user/{userId}
```

**Response:**
```json
[
  {
    "id": 1,
    "chatRoomId": 1,
    "buyer": {
      "id": 2,
      "username": "bob"
    },
    "seller": {
      "id": 1,
      "username": "alice"
    },
    "product": {
      "id": 1,
      "title": "iPhone 14"
    }
  }
]
```

#### 3. Get Chat Room Details
```http
GET /api/chatrooms/{chatRoomId}
```

**Response:** Single chat room object with buyer, seller, and product details.

#### 4. Get Chat Room Messages
Retrieves all messages in chronological order for a specific chat room.

```http
GET /api/chatrooms/{chatRoomId}/messages
```

**Response:**
```json
[
  {
    "id": 1,
    "content": "Is this still available?",
    "sender": {
      "id": 2,
      "username": "bob"
    },
    "sentAt": "2026-01-11T10:30:00"
  },
  {
    "id": 2,
    "content": "Yes, it is!",
    "sender": {
      "id": 1,
      "username": "alice"
    },
    "sentAt": "2026-01-11T10:31:15"
  }
]
```

## WebSocket Communication

### Connection Setup

**WebSocket Endpoint:** `ws://localhost:8080/ws`

**Client Connection:**
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to topics
  stompClient.subscribe('/topic/chatroom/1', onMessageReceived);
  stompClient.subscribe('/topic/user/2/notifications', onNotificationReceived);
});
```

### STOMP Topics

#### 1. Chat Room Topic
**Topic:** `/topic/chatroom/{chatRoomId}`

All participants in a chat room subscribe to this topic to receive messages in real-time.

**Example:** `/topic/chatroom/1`

#### 2. Personal Notification Topic
**Topic:** `/topic/user/{userId}/notifications`

Each user subscribes to their personal notification topic to receive messages when they're not actively viewing a chat room.

**Example:** `/topic/user/2/notifications`

### Sending Messages

**Destination:** `/app/chat.sendMessage`

**Message Format:**
```json
{
  "chatRoomId": 1,
  "senderId": 2,
  "senderUsername": "bob",
  "content": "Hello! Is this available?",
  "type": "CHAT"
}
```

**Client Code:**
```javascript
const message = {
  chatRoomId: 1,
  senderId: 2,
  senderUsername: "bob",
  content: "Hello!",
  type: "CHAT"
};

stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
```

### Message Flow

1. **User sends message:**
   - Client sends message to `/app/chat.sendMessage`
   - Server receives via `@MessageMapping("/chat.sendMessage")`

2. **Server processes:**
   - Saves message to database
   - Converts to DTO with timestamp

3. **Server broadcasts:**
   - Sends to chat room topic: `/topic/chatroom/{chatRoomId}`
     - All users in the chat room receive it
   - Sends to recipient's notification topic: `/topic/user/{recipientId}/notifications`
     - Only the other user receives notification

### Backend Implementation

```java
@MessageMapping("/chat.sendMessage")
public void sendMessage(@Payload ChatMessageDTO chatMessage) {
    // Save message to database
    Message savedMessage = chatService.saveMessage(chatMessage);
    
    // Convert to DTO
    ChatMessageDTO responseDTO = chatService.convertToDTO(savedMessage);
    
    // Broadcast to chat room
    messagingTemplate.convertAndSend(
        "/topic/chatroom/" + chatMessage.getChatRoomId(), 
        responseDTO
    );
    
    // Determine the recipient (the other user)
    var chatRoom = chatService.getChatRoom(chatMessage.getChatRoomId());
    if (chatRoom.isPresent()) {
        Long recipientId = chatMessage.getSenderId().equals(chatRoom.get().getBuyer().getId()) 
            ? chatRoom.get().getSeller().getId() 
            : chatRoom.get().getBuyer().getId();
        
        // Send notification to recipient
        messagingTemplate.convertAndSend(
            "/topic/user/" + recipientId + "/notifications",
            responseDTO
        );
    }
}
```

## Database Schema

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50)
);

-- Products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    status VARCHAR(50),
    seller_id BIGINT REFERENCES users(id)
);

-- Chat rooms table
CREATE TABLE chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT REFERENCES users(id),
    seller_id BIGINT REFERENCES users(id),
    product_id BIGINT REFERENCES products(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Messages table
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    sender_id BIGINT REFERENCES users(id),
    chat_room_id BIGINT REFERENCES chat_rooms(id),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Setup and Configuration

### 1. Environment Variables

Create a `.env` file in the project root:

```properties
DB_URL=jdbc:postgresql://localhost:5432/marketplace_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
SERVER_PORT=8080
```

### 2. Application Properties

`application.properties`:
```properties
spring.application.name=Cambo_MarketPlace

# Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Server
server.port=${SERVER_PORT}
```

### 3. Dependencies

Add to `pom.xml`:
```xml
<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Environment Variables -->
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 4. Run the Application

```bash
# Start PostgreSQL database
# Create database: marketplace_db

# Run Spring Boot application
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

## How It Works

### Scenario: Buyer Contacts Seller

1. **Browse Products:**
   - Buyer visits marketplace
   - Sees list of products fetched from `/api/products`

2. **Initiate Chat:**
   - Buyer clicks "Chat with Seller" on a product
   - Frontend calls `POST /api/chatrooms/create-or-get` with:
     - `productId`: The product being discussed
     - `buyerId`: Current user's ID
     - `sellerId`: Product owner's ID

3. **Chat Room Creation:**
   - Backend checks if chat room exists for this combination
   - If not, creates new `ChatRoom` entity and saves to database
   - Returns chat room details to frontend

4. **WebSocket Connection:**
   - Both users' browsers connect to WebSocket: `ws://localhost:8080/ws`
   - Each user subscribes to:
     - Chat room topic: `/topic/chatroom/{chatRoomId}`
     - Personal notifications: `/topic/user/{userId}/notifications`

5. **Load Message History:**
   - Frontend calls `GET /api/chatrooms/{chatRoomId}/messages`
   - Displays all previous messages in chronological order

6. **Send Message:**
   - User types message and clicks send
   - Client sends via WebSocket to `/app/chat.sendMessage`
   - Server receives, saves to database, broadcasts to:
     - `/topic/chatroom/{chatRoomId}` → Both users receive if in chat
     - `/topic/user/{recipientId}/notifications` → Other user receives notification

7. **Receive Messages:**
   - Users in chat room see message instantly
   - If user is on product listing page, notification badge updates
   - User can click notification to enter chat room

### Notification System

**When a user is NOT in the chat room:**
- Message arrives via `/topic/user/{userId}/notifications`
- Frontend increments unread count
- Updates notification badge on floating chat toggle
- Adds to "Recent Messages" section in chat dropdown
- Shows toast notification popup

**When a user IS in the chat room:**
- Message arrives via `/topic/chatroom/{chatRoomId}`
- Message appears instantly in chat
- No notification shown (user is already viewing)

### Chat Room Uniqueness

Each chat room is unique per combination of:
- Product ID
- Buyer ID
- Seller ID

This means:
- Same buyer and seller discussing **different products** = Different chat rooms
- **Different buyers** and same seller discussing same product = Different chat rooms
- Same buyer and seller discussing **same product** = Same chat room (reused)

## API Testing

### Using cURL

**Get all products:**
```bash
curl http://localhost:8080/api/products
```

**Create chat room:**
```bash
curl -X POST http://localhost:8080/api/chatrooms/create-or-get \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"buyerId":2,"sellerId":1}'
```

**Get chat messages:**
```bash
curl http://localhost:8080/api/chatrooms/1/messages
```

### WebSocket Testing

Use a WebSocket client or the provided frontend pages:
- `index.html` - Product listing with chat notifications
- `chat-room.html` - Real-time chat interface

## Troubleshooting

### WebSocket Connection Issues
- Ensure backend is running on port 8080
- Check browser console for connection errors
- Verify CORS configuration allows your frontend origin

### Messages Not Persisting
- Check database connection
- Verify JPA entities have proper relationships
- Check application logs for SQL errors

### Notifications Not Received
- Verify user is subscribed to correct topic: `/topic/user/{userId}/notifications`
- Check that recipientId is calculated correctly in ChatController
- Ensure WebSocket connection is active

## License

MIT License
