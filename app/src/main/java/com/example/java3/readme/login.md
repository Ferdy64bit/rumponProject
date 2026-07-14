# ROLE

You are a Senior Android Native Java Engineer specializing in Firebase Authentication and Clean Architecture.

PROJECT

Fishing Point Tanjung Anom

Platform

Android Native

Language

Java

Architecture

MVVM

Repository Pattern

Firebase

Material Design 3

------------------------------------------------------------

OBJECTIVE

Implement ONLY Firebase Authentication backend for the existing Login and Register screens.

The UI is already completed.

Do NOT redesign any screen.

Do NOT change XML layout.

------------------------------------------------------------

DO NOT MODIFY

Google Maps

Community

Weather

TideCheck

Recommendation Engine

Favorites

Profile

Settings

Navigation

------------------------------------------------------------

IMPLEMENT

Firebase Authentication

Email Login

Email Registration

Email Verification

Forgot Password

Session Manager

Auto Login

Logout

------------------------------------------------------------

AFTER REGISTRATION

Automatically create a Firestore document in

users/{uid}

Fields

uid

fullName

email

photoUrl

phone

createdAt

emailVerified

favoriteCount

totalPosts

------------------------------------------------------------

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

Never call Firebase directly from Activity.

------------------------------------------------------------

ERROR HANDLING

Display proper messages for

Invalid Email

Weak Password

User Already Exists

Wrong Password

No Internet

Firebase Exception

------------------------------------------------------------

OUTPUT

Generate

FirebaseManager

AuthRepository

LoginViewModel

RegisterViewModel

SessionManager

Firestore User Creation

Verification Checklist

STOP.

Wait for confirmation before implementing Google Login.