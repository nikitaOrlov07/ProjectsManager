# 🚀 Project Manager

## 📌 About

Project Manager is a cutting-edge web application designed for efficient project and task management. Our solution empowers teams of all sizes to structure their work, track progress, and achieve their goals.

## ✨ Key Features

### 👀 For Unauthenticated Users
- Browse all projects
- View project details including description, number of people involved, start and end dates

### 🔐 For Authenticated Users
All features available to unauthenticated users, plus:
- View all registered users
- Add projects to personal project list
- Send friend invitations to other users
- Add and Remove friend
- Access and use chat functionality
- Create project-specific chats
- Create project
- Users can see their personal information (Profil)
- Set passwords for project access
- For all projects, user projects, all users, user friends, and requests to add to friends added search and when searching will be updated not the entire page, but the desired fragment  

### 🛠️ For Project Members
- Add tasks with descriptions
- Reset task list
- Track task progress with progress bars
- View task details on hover
- See other project team members
- Upload files to projects or delete files from project (via RestController)
- can see the file contents (view) or download it directly (download).
- Update project information
- Delete project

### 💬 Chat System 
- Chats are updated in real time
- Two types of chats: project-specific and user-to-user
- Delete messages , delete the chat and clear chats in user-to-user chats
- Clear chats and delete messages in project chats
- Notification when a user joins a chat
- Implemented using WebSocket, JavaScript, and controllers 

### 🔍 Additional Features
- Comprehensive logging throughout the application

### Roles
There are two roles in the project (admin and user): admin has the ability to view all information about the project (assigned tasks,add tasks, see participants, see attached files) without participating in it.

## 🛠️ Technologies Used
- Backend: Java
- Frontend: JavaScript, HTML, CSS
- Real-time Communication: WebSocket
- Docker: when docker containers are created -> an admin account will be automatically created in the database (via init.sql file) . Admin user : USERNAME: ADMIN , PASSWORD: Admin
- Also in the project there are a lot of JUNIT tests (Mockito was used).
  

## Screenshots

### Main Page
![image](https://github.com/nikitaOrlov07/ProjectsManager/assets/145924436/ce4ef788-5c00-4638-bc8b-7db0d791c3bb)
*main page in the application where all the necessary information is located.*
### Detail page
![image](https://github.com/nikitaOrlov07/ProjectsManager/assets/145924436/8b406cbd-8747-4b12-bc7a-0c1db75410d4)
![image](https://github.com/nikitaOrlov07/ProjectsManager/assets/145924436/3097e4d3-4a62-4167-b4c3-f36cb9aa10b1)
*page where the project is being worked on.*
### Chat page
![image](https://github.com/nikitaOrlov07/ProjectsManager/assets/145924436/854be572-6480-4b6a-b7c6-b172b75b5375)
*page where you can communicate with other users.* 
### Login and Register Pages
![image](https://github.com/nikitaOrlov07/ProjectsManager/assets/145924436/5b8cea26-5de0-4d88-9fe5-af80fdfe55e3)
*page where you can login or create a new account.*
### Create and Update pages
![image](https://github.com/nikitaOrlov07/ProjectsManager/assets/145924436/ffe91584-6559-43b0-8f9e-65fd07b5bb33)
*user can create a new project, or update the project he's a member of.*

