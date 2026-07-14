You are a Senior Android Native Java Engineer and Software Architect.

PROJECT

Fishing Point Tanjung Anom

Current project status

- Android Native Java
- XML Layout
- MVVM Architecture
- Repository Pattern
- Clean Architecture
- Material Design 3
- Firebase Authentication configured
- Cloud Firestore configured
- SHA-1 already added
- google-services.json already configured
- Google Sign-In already enabled
- Login UI already completed
- Register UI already completed

==================================================

OBJECTIVE

Implement ONLY the backend logic for Login and Register.

The UI has already been completed.

Do NOT redesign any screen.

Do NOT change XML layouts.

Do NOT modify unrelated modules.

==================================================

DO NOT MODIFY

Google Maps

Weather

TideCheck

Fishing Recommendation

Community

Favorites

Profile

Settings

Navigation

Bottom Navigation

Splash Screen Design

==================================================

IMPLEMENT

1.

Email Login

2.

Email Registration

3.

Firebase Email Verification

4.

Forgot Password

5.

Session Manager

6.

Auto Login

7.

Logout

8.

Google Sign-In

==================================================

AFTER SUCCESSFUL REGISTRATION

Automatically create a Firestore document.

Collection

users

Document ID

Firebase UID

Fields

uid

fullName

email

photoUrl

phoneNumber

emailVerified

createdAt

updatedAt

role = "user"

favoriteCount = 0

totalPosts = 0

profileCompleted = false

==================================================

AFTER LOGIN

Check

Email Verified

If email is NOT verified

Display dialog

"Please verify your email before logging in."

Provide button

Resend Verification Email

Do NOT continue to HomeActivity.

==================================================

AFTER GOOGLE LOGIN

If Firestore document does not exist

Create it automatically.

If already exists

Update

updatedAt

==================================================

ERROR HANDLING

Handle

Invalid Email

User Not Found

Wrong Password

Weak Password

Email Already Exists

Network Error

Firebase Exception

Google Sign In Failed

Email Not Verified

==================================================

ARCHITECTURE

Activity

↓

ViewModel

↓

Repository

↓

FirebaseManager

↓

Firebase Authentication

↓

Cloud Firestore

Never access Firebase directly from Activity.

Never access Firebase directly from ViewModel.

Only Repository may communicate with FirebaseManager.

==================================================

SESSION

If user already logged in

Automatically skip LoginActivity

↓

Go directly to HomeActivity

==================================================

LOGOUT

Logout from

Firebase Authentication

Google Sign-In

Clear Session

Redirect

LoginActivity

==================================================

SECURITY

Never store password locally.

Never save credentials inside SharedPreferences.

Only save login session.

==================================================

GOOGLE SIGN-IN

Reuse existing Firebase configuration.

Do NOT regenerate google-services.json.

Do NOT change package name.

Do NOT modify SHA-1 configuration.

==================================================

KEEP

Current UI

Current XML

Current Theme

Current Colors

Current Animations

Current Navigation

==================================================

OUTPUT

First analyze the existing authentication module.

Identify incompatible classes.

Modify only necessary files.

Generate production-ready Java code.

Show every modified file separately.

After finishing Authentication,

STOP.

Wait for my confirmation before implementing Firestore Fishing Points.