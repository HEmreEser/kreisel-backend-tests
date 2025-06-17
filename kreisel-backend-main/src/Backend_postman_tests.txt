
🔐 AUTH

POST 
http://localhost:8080/api/auth/register

{
  "fullName": "Max Mustermann",
  "email": "max@hm.edu",
  "password": "max123"
}

POST 
http://localhost:8080/api/auth/login

{
  "email": "max@hm.edu",
  "password": "max123"
}

POST 
http://localhost:8080/api/auth/logout


👤 USER

GET 
http://localhost:8080/api/users

GET 
http://localhost:8080/api/users/1

GET 
http://localhost:8080/api/users/email/ben@hm.edu

POST 
http://localhost:8080/api/users

{
  "fullName": "Erika Musterfrau",
  "email": "erika@hm.edu",
  "password": "pass123",
  "role": "USER"
}

PUT 
http://localhost:8080/api/users/1

{
  "fullName": "Moritz Mustermann",
  "email": "moritz@hm.edu",
  "password": "pass123",
  "role": "USER"
}

DELETE
http://localhost:8080/api/users/1


🎽 ITEMS

GET 
http://localhost:8080/api/items?location=LOTHSTRASSE&available=true&searchQuery=jacke

GET 
http://localhost:8080/api/items/1

POST 
http://localhost:8080/api/items

{
  "name": "Skihelm",
  "size": "M",
  "available": true,
  "description": "Schützt den Kopf beim Skifahren",
  "brand": "Uvex",
  "location": "PASING",
  "gender": "UNISEX",
  "category": "EQUIPMENT",
  "subcategory": "HELME",
  "zustand": "NEU"
}

PUT 
http://localhost:8080/api/items/1

{
  "name": "Winterjacke",
  "size": "L",
  "available": false,
  "description": "Jetzt leider nicht verfügbar",
  "brand": "North Face",
  "location": "LOTHSTRASSE",
  "gender": "DAMEN",
  "category": "KLEIDUNG",
  "subcategory": "JACKEN",
  "zustand": "GEBRAUCHT"
}

DELETE 
http://localhost:8080/api/items/1

📦 RENTALS

GET 
http://localhost:8080/api/rentals

GET 
http://localhost:8080/api/rentals/user/2

GET 
http://localhost:8080/api/rentals/user/2/active

GET 
http://localhost:8080/api/rentals/user/2/history

POST 
http://localhost:8080/api/rentals/user/2/rent

{
  "item": {
    "id": 4
  },
  "rentalDate": "2025-05-24",
  "endDate": "2025-06-10",
  "extended": false
}

POST 
http://localhost:8080/api/rentals/1/extend

POST 
http://localhost:8080/api/rentals/1/return
