# NOVA â€“ Desktop Learning Management Platform (JavaFX)

## Overview

This project was developed as part of the **PIDEV â€“ 3rd Year Engineering Program** at **Esprit School of Engineering** (Academic Year 2025â€“2026).

NOVA is a comprehensive, desktop-based learning management system built with JavaFX. It combines traditional course management, an expansive digital library, and interactive skill quizzes with modern gamification, AI-powered features, and productivity tools. The platform empowers students and educators to manage user profiles securely, track intense study sessions, earn rewards through interactive games, collaborate on forums, and receive personalized AI recommendations, all within a sleek, modern desktop application.

## Features

### ðŸ–¥ï¸ Modern Desktop UI/UX
- **Adaptable Interfaces**: Seamlessly transition between finely-tuned Light and Dark modes designed to reduce eye strain during long study sessions.
- **Premium Visual Aesthetics**: A modern, distraction-free environment featuring translucent glassmorphism elements, depth-based shadow hierarchies, and a clean, intuitive layout.
- **Fluid Animations**: Smooth JavaFX transitions, continuous 3D floating hero elements, and staggered data loading.
- **Responsive Navigation**: Collapsible sidebar, integrated search, and quick-access hubs.

### User Management & Security

#### Authentication
- **Secure Login**: Password hashing using jBCrypt with advanced validation (length, uppercase, digits, special characters).
- **Show/Hide Password**: Toggle visibility on login and signup forms.
- **OAuth 2.0 Sign-In / Sign-Up**: One-click login and account creation via Google and LinkedIn (port 8888 callback server).
- **Face Recognition Login**: Biometric authentication using the Face++ API - register your face once, log in with a scan.
- **Google Authenticator (TOTP) 2FA**: Time-based one-time password via Google Authenticator app.
- **Behavioral CAPTCHA**: Invisible bot-detection on login using mouse movement and timing analysis.

#### Registration
- **Student / Tutor Signup**: Role selection with full validation.
- **OAuth Signup**: Create an account instantly via Google or LinkedIn - email pre-verified, username auto-generated.
- **Password Strength Meter**: Real-time 4-bar strength indicator (Weak / Fair / Good / Strong) on the signup form.
- **Username Suggestions**: Auto-generated username suggestions based on email prefix during signup.

#### Profile & Account
- **Profile Page**: Hero card with username, role badge, XP, status, join date, and favorite games tab.
- **Edit Profile Page**: Dedicated page to update username, email, and password with current-password verification.
- **Profile Picture Upload**: Upload a custom avatar stored locally, persisted to the database and reflected in the navbar instantly.
- **Gravatar Fallback**: If no custom photo is uploaded, the navbar avatar falls back to the user's Gravatar (identicon).
- **Face Login Setup**: Register and manage face tokens directly from the Edit Profile page.
- **2FA Management**: Enable/disable Google Authenticator from the profile settings.

#### Admin Dashboard - User Management
- **User List**: Full table with username (+ country flag [TN]), email, role, status, XP, and action buttons.
- **Add / Edit Users**: Modal form to create or update any user account.
- **Delete Users**: Single-user deletion with confirmation dialog.
- **Double-Click to Edit**: Double-click any row in the user table to open the edit form instantly.
- **Search & Filter**: Real-time search by username/email and role-based filtering dropdown.
- **Bulk Actions**: Select multiple users via checkboxes and apply Activate, Ban, or Delete in one click.
- **Export to CSV**: Export the full user list to a semicolon-separated CSV file (Excel-compatible, with BOM), including a summary block.
- **Stats Cards**: Live counters for Total, Active, Banned, Verified, Students, Tutors, and Admins.
- **Login History Timeline**: View per-user login activity in the profile page.

#### Geo & Location
- **IP Geolocation Widget**: Admin dashboard shows the admin's location, timezone, IP, and currency using ipwho.is API (no API key required).
- **Country Flags in User List**: Each user's country code shown as [XX] prefix next to their username, populated automatically on login.

#### Security & Resilience
- **Role-Based Access Control**: Students, Tutors, and Admins each get a dedicated dashboard with restricted navigation.
- **DB Auto-Migration**: On startup, MyConnection automatically adds missing columns (profile_picture, totp_enabled, totp_secret, face_token, country_code, last_lat, last_lon) if they do not exist.
- **Auto-Reconnect**: Database connection automatically re-establishes if the MySQL server restarts mid-session.
- **Dark Mode**: Full dark/light theme toggle with scheduled automation and quick presets, applied across all pages.

### ðŸ“š Digital Library & Resources
- **Comprehensive Book Inventory**: Discover, search, and manage a curated collection of physical and digital books.
- **PDF & Digital Reading**: Direct access to digital PDF resources for seamless studying.
- **AI Library Assistant**: Built-in AI helper to summarize book contents or recommend reading materials.
- **Automated Borrowing Mechanics**: Track borrowed books, due dates, reservation management, and automated late-fee calculations.

### ðŸŽ¯ Interactive Quiz System
- **Dynamic Skill Quizzes**: Tutors can create and deploy rich, multimedia multiple-choice quizzes to test student knowledge.
- **AI-Powered Hints**: Students can request context-aware, progressive hints generated by AI when stuck on difficult questions.
- **Automated Grading & Feedback**: Instant score calculation and performance feedback upon completion.
- **Quality Control**: Reporting system allowing students to flag incorrect or inappropriate questions for review.

### ðŸŽ® Gamification & Rewards System
- **Multiple Game Types**: PUZZLE, MEMORY, TRIVIA, ARCADE.
- **Token Economy & XP**: Earn tokens and level up through a 60-level progression system.
- **Energy Regeneration Mini Games**: Free games designed for quick study breaks (Breathing Exercises, Eye Rest, Hydration, Stretching) to restore focus points.
- **Achievement System**: Unlock badges, special rewards, and downloadable PDF certificates with QR codes.

### â±ï¸ Study Session Management
- **Integrated Pomodoro Engine**: Fully customizable focus and break intervals (e.g., 25-minute focus, 5-minute break, extended break after 4 cycles). Features visual countdowns and auditory notifications.
- **Advanced Session Tracking & Analytics**: Comprehensive logging of study habits, mood, and focus levels. Includes visual analytics dashboards to track productivity over weeks, months, or years.
- **Energy & Streak Mechanics**: A unique energy system where focus depletes over time and is restored via quick mini-game breaks. Tracks current and longest daily study streaks to maintain motivation.
- **Interactive Calendar Planner**: Drag-and-drop scheduling interface allowing students and tutors to visually organize upcoming study sessions and exams.

### ðŸ’¬ Forum & Peer-to-Peer Collaboration
- **Targeted Discussion Spaces**: Categorized academic hubs with threaded replies and rich media support (image attachments and formatted text).
- **Algorithmic Sorting & Filtering**: Smart feeds allowing users to sort discussions by Hot, New, Top, or by specific trending academic tags.
- **StackOverflow-Style Reputation**: Upvote/downvote mechanics on posts and comments, with the ability for authors to mark the "Accepted Solution".
- **Automated Moderation**: Background censorship services to filter profanity and maintain a safe academic environment, plus tools for admins to lock toxic threads.
- **Reporting System**: Users have the ability to report posts for different reasons.

### ðŸ¤– AI Integration & APIs
- **AI Chat Assistant**: Context-aware study buddy powered by external LLMs (Hugging Face / Gemini).
- **Code Compilation**: Live code execution and testing via JDoodle API integration.
- **Smart Enrichments**: Wikipedia and YouTube API integrations to pull external context and video resources directly into the platform.
- **AI-Powered Forum Assistants**: Built-in AI grammar and formatting checks before publishing, and Thread Summarization for long, complex discussion threads.

---

## Tech Stack

### Frontend (UI/UX)
- **JavaFX 21**: Core GUI framework.
- **FXML**: Markup language for defining user interfaces.
- **JavaFX CSS**: Custom styling for Dark/Light themes and modern UI components.

### Backend (Core Logic)
- **Java 17+**: Core programming language.
- **JDBC**: Database connectivity and querying.
- **jBCrypt**: Cryptographic password hashing.

### Database
- **MySQL 8.0 / MariaDB**: Relational database management.

### APIs & External Services
- **Hugging Face API & Gemini API**: AI text generation, chat analytics, and hints.
- **JDoodle API**: Remote code execution sandbox.
- **YouTube Data API v3 & Wikipedia API**: Content enrichment and video fetching.
- **GeoLocation API**: Location-based services.
- **OAuth2**: External authentication handling.
- **Twemoji API**: Cross-platform emoji rendering for uniform visual badges and UI elements.

### Development Tools
- **Maven**: Dependency management and build lifecycle (`pom.xml`).
- **IntelliJ IDEA**: Primary IDE.

---

## Architecture

The application follows a strict **MVC (Model-View-Controller)** pattern tailored for JavaFX, organized for modular scalability:

```text
Pi_Java/
â”œâ”€â”€ src/
â”‚   â”œâ”€â”€ main/
â”‚   â”‚   â”œâ”€â”€ java/
â”‚   â”‚   â”‚   â”œâ”€â”€ controllers/      # FXML Controllers handling UI logic (Users, Quiz, Library, etc.)
â”‚   â”‚   â”‚   â”œâ”€â”€ models/           # Data entities (User, Game, Notification, etc.)
â”‚   â”‚   â”‚   â”œâ”€â”€ services/         # Business logic & APIs
â”‚   â”‚   â”‚   â”‚   â”œâ”€â”€ ai/           # HuggingFaceService, GeminiService, ChatAnalytics
â”‚   â”‚   â”‚   â”‚   â””â”€â”€ api/          # JDoodle, Kroki, YouTube, Wikipedia, GeoLocation
â”‚   â”‚   â”‚   â”œâ”€â”€ utils/            # Shared utilities (ThemeManager, SessionManager)
â”‚   â”‚   â”‚   â””â”€â”€ MainFX.java       # Application entry point
â”‚   â”‚   â””â”€â”€ resources/
â”‚   â”‚       â”œâ”€â”€ views/            # FXML layout files separated by module
â”‚   â”‚       â”œâ”€â”€ css/              # Application stylesheets (app-light.css, app-dark.css)
â”‚   â”‚       â””â”€â”€ images/           # Local UI assets, icons, and logos
â”‚   â””â”€â”€ test/                     # Unit and integration tests
â”œâ”€â”€ uploads/                      # Local storage for user-generated content
â”œâ”€â”€ recreate_tables.sql           # Database schema initialization
â””â”€â”€ pom.xml                       # Maven dependencies and configuration
```

---

## Contributors

This project was developed by a team of engineering students at **Esprit School of Engineering**:

- **[Nouha Hamrouni](https://github.com/Nouha11)** - Full-Stack Developer
- **[Acil Jouini](https://github.com/aciljouini)** - Full-Stack Developer
- **[Said Hadj Abdallah](https://github.com/Ha-Said)** - Full-Stack Developer
- **[Oussema Ben Zinouba](https://github.com/obenzinouba)** - Full-Stack Developer
- **[Oumeyma Radhouani](https://github.com/oumeyma-radhouani)** - Full-Stack Developer
- **[Wassim Ouni](https://github.com/wisssouni)** - Full-Stack Developer

## Academic Context

**Institution**: Esprit School of Engineering â€“ Tunisia  
**Program**: PIDEV â€“ 3rd Year Engineering (3A)  
**Academic Year**: 2025â€“2026  
**Project Type**: Integrated Development Project (Projet IntÃ©grÃ© de DÃ©veloppement)

This project demonstrates the practical application of object-oriented programming, desktop application design, secure database management, and integration of modern APIs.

## Acknowledgments

We would like to thank:
- **Esprit School of Engineering** for providing the academic framework and resources.
- Our **project supervisors** for their guidance and support.

---
**Developed at Esprit School of Engineering â€“ Tunisia** PIDEV 3A | Academic Year 2025â€“2026
