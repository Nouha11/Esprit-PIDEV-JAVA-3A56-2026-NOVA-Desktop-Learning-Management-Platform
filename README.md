NOVA – Desktop Learning Management Platform (JavaFX)
Overview
This project was developed as part of the PIDEV – 3rd Year Engineering Program at Esprit School of Engineering (Academic Year 2025–2026).

NOVA is a comprehensive, desktop-based learning management system built with JavaFX. It combines traditional course management with modern gamification, AI-powered features, and productivity tools. The platform enables students to track study sessions, earn rewards through games, collaborate on forums, and receive personalized AI recommendations, all within a sleek, modern desktop application.

Features
🖥️ Modern Desktop UI/UX
Dynamic Theming: Built-in ThemeManager supporting automated and manual switching between Dark and Light modes.

Glassmorphism Design: Sleek, modern interface using translucent panes, drop shadows, and premium CSS styling.

Fluid Animations: Smooth JavaFX transitions, continuous 3D floating hero elements, and staggered data loading.

Responsive Navigation: Collapsible sidebar, integrated search, and quick-access hubs.

🎮 Gamification & Rewards System
AI-Powered Question Generator: Generate trivia questions using Hugging Face AI.

Multiple Game Types: PUZZLE, MEMORY, TRIVIA, ARCADE.

Game Categories:

Full Games: Reward-based games that cost tokens and provide XP/tokens.

Mini Games: Free energy regeneration games for quick breaks (Breathing Exercises, Eye Rest, Hydration, Stretching).

Token Economy & XP: Earn tokens and level up through a 60-level progression system.

Achievement System: Unlock badges, special rewards, and PDF certificates with QR codes.

Leaderboards & Favorites: Real-time rankings and personalized game collections.

📚 Study Session Management
Session Tracking: Create, edit, and manage study sessions with detailed metadata.

Pomodoro Timer: Built-in productivity timer with customizable work/break intervals.

Energy System: Track and regenerate energy through mini-games. Energy depletes during study sessions and affects learning effectiveness.

Mood & Streak Tracking: Record emotional states and monitor study streaks.

🤖 AI Integration
AI Chat Assistant: Context-aware study buddy powered by Hugging Face.

Reward Recommendations: Personalized suggestions based on progress.

Note Summarization & Quiz Gen: AI-generated summaries and automatic quiz creation.

Forum AI Assist: NOVA AI helps answer student questions directly in the app.

📖 Course & Library Management
Course Creation: Tutors can create and manage courses and attach PDF study materials.

Note-Taking System: Create, edit, and search notes with tag-based organization.

Library System: Complete book inventory, automated borrowing tracking, late fee calculations, and reservation management.

💬 Forum & Peer-to-Peer Collaboration
Discussion Spaces: Organized categories for targeted academic discussions.

Reputation System: Upvote/downvote mechanics and "Accepted Solution" tracking.

Content Moderation: Automated profanity filters and user reporting systems.

Rich Media Replies: Commenting system supporting threaded replies and direct image uploads.

🔐 Authentication & Security
Secure Login: Password hashing using jBCrypt.

Role-Based Access: Specialized dashboards for Students, Tutors, and Admins.

Two-Factor Authentication (2FA): Enhanced desktop security.

Gravatar Integration: Dynamic profile picture fetching and custom local uploads.

Tech Stack
Frontend (UI/UX)
JavaFX 21: Core GUI framework.

FXML: Markup language for defining user interfaces.

JavaFX CSS: Custom styling for Dark/Light themes and modern UI components.

SceneBuilder: Visual layout design.

Backend (Core Logic)
Java 17+: Core programming language.

JDBC: Database connectivity and querying.

jBCrypt: Cryptographic password hashing.

Database
MySQL 8.0 / MariaDB: Relational database management.

APIs & External Services
Hugging Face API: AI text generation & hints.

Gravatar API: Global profile picture integration.

Twemoji: Emojis and visual badges rendering.

Development Tools
Maven: Dependency management and build lifecycle.

IntelliJ IDEA / Eclipse: IDE.

Architecture
The application follows a strict MVC (Model-View-Controller) pattern tailored for JavaFX:

src/
├── main/
│   ├── java/
│   │   ├── controllers/      # FXML Controllers handling UI logic (e.g., NovaDashboardController)
│   │   ├── models/           # Data entities (User, Game, Notification, etc.)
│   │   ├── services/         # Business logic & JDBC Database operations (UserService, etc.)
│   │   └── utils/            # Shared utilities (ThemeManager, SessionManager, PasswordUtils)
│   └── resources/
│       ├── views/            # FXML layout files separated by module
│       ├── css/              # Application stylesheets (app-light.css, app-dark.css)
│       └── images/           # Local UI assets, icons, and logos

Contributors
This project was developed by a team of engineering students at Esprit School of Engineering:

Nouha Hamrouni - Full-Stack Developer

Acil Jouini - Full-Stack Developer

Said Hadj Abdallah - Full-Stack Developer

Oussema Ben Zinouba - Full-Stack Developer

Oumeyma Radhouani - Full-Stack Developer

Wassim Ouni - Full-Stack Developer

Academic Context
Institution: Esprit School of Engineering – Tunisia

Program: PIDEV – 3rd Year Engineering (3A)

Academic Year: 2025–2026

Project Type: Integrated Development Project (Projet Intégré de Développement)

This project demonstrates the practical application of object-oriented programming, desktop application design, database management, and modern software engineering practices.

Acknowledgments
We would like to thank:

Esprit School of Engineering for providing the academic framework and resources.

Our project supervisors for their guidance and support.

Developed at Esprit School of Engineering – Tunisia PIDEV 3A | Academic Year 2025–2026
