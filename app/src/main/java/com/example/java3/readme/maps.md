# ROLE

You are a Senior Android Software Architect, Senior Android Native Java Engineer, and Google Maps SDK Expert.

You are working on an existing Android Studio project called:

Fishing Point Tanjung Anom

The application is already built using Java, XML, MVVM, Clean Architecture, Repository Pattern, Firebase Authentication, Firestore, Material Design 3, and Bottom Navigation.

The previous MapLibre implementation must be completely removed and replaced with Google Maps SDK for Android.

------------------------------------------------------------

OBJECTIVE

Re-integrate Google Maps into the existing project.

The goal of this phase is ONLY:

Display Google Maps successfully inside the existing Map Fragment.

Nothing else.

------------------------------------------------------------

IMPORTANT

Do NOT regenerate the project.

Do NOT redesign the application.

Do NOT modify Firebase.

Do NOT modify Firestore.

Do NOT modify Authentication.

Do NOT modify Weather.

Do NOT modify TideCheck.

Do NOT modify Recommendation Engine.

Do NOT modify Community.

Do NOT modify Profile.

Do NOT modify Favorites.

Do NOT modify Settings.

Do NOT modify Navigation.

Keep the current MVVM architecture.

Modify ONLY the Map module.

------------------------------------------------------------

GOOGLE MAPS CONFIGURATION

Use the Google Maps API key that is already configured in this project.

Do not replace it.

Do not generate placeholder API keys.

Do not change the existing API key configuration in the project.

Verify that AndroidManifest.xml correctly references the configured API key.

------------------------------------------------------------

REMOVE

Remove every MapLibre dependency.

Remove MapLibre imports.

Remove MapLibre managers.

Remove OfflineMapManager.

Remove MarkerManager.

Remove MapLocationManager.

Remove every unused MapLibre resource.

Remove every MapLibre permission that is no longer needed.

------------------------------------------------------------

INSTALL

Use

Google Maps SDK for Android

Google Play Services Location

FusedLocationProviderClient

Material Components

------------------------------------------------------------

DEPENDENCIES

Verify Gradle configuration.

Verify Manifest configuration.

Verify permissions.

Verify Google Play Services version compatibility.

------------------------------------------------------------

MAP SCREEN

Keep the existing Fragment.

Do not redesign the UI.

Only replace the Map implementation.

Use

SupportMapFragment

or

MapView

depending on the current architecture.

------------------------------------------------------------

FIRST IMPLEMENTATION

ONLY

Display Google Maps.

Nothing else.

Do NOT implement

GPS

Markers

Firestore

Polyline

Directions

Weather

Recommendation

Current Location

Camera Animation

Marker Cluster

Navigation

------------------------------------------------------------

DEFAULT CAMERA

Move camera only once.

Latitude

-6.041980

Longitude

106.501318

Zoom

15f

------------------------------------------------------------

MAP SETTINGS

Enable

Zoom Controls

Zoom Gestures

Compass

Rotate Gestures

Scroll Gestures

Tilt Gestures

Disable every advanced feature.

------------------------------------------------------------

ARCHITECTURE

Keep

MVVM

Repository Pattern

ViewBinding

Single Activity Architecture

Bottom Navigation

Do not change package names.

Do not move files unnecessarily.

------------------------------------------------------------

VIEWBINDING

Never break ViewBinding.

Never rename existing XML IDs.

------------------------------------------------------------

OUTPUT

Provide

1.

Architecture Analysis

2.

Files Modified

3.

Gradle Changes

4.

Manifest Changes

5.

Java Code

6.

XML Code

7.

Verification Checklist

------------------------------------------------------------

SUCCESS CRITERIA

When the user presses the Maps menu

The Google Map must appear successfully.

The application must not crash.

The user must be able to

Zoom

Drag

Rotate

Nothing else.

STOP.

Wait for confirmation before implementing GPS.