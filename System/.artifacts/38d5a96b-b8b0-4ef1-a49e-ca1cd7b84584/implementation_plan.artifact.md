# DELITRACK Role-Based Security & Portal Separation Implementation Plan

Implement strict role-based routing, screen separation, and security guards to prevent Customers and Riders from accessing Admin screens or administrative features.

## User Review Required

> [!IMPORTANT]
> - **Login & Registration Routing**: Customers, Riders, and Admins will automatically land on their respective dedicated portals upon login/registration.
> - **Security Guards**: Direct activity launches (e.g. attempting to launch `AssignRiderVehicleActivity` from a Customer account) will be blocked with an "Access Denied" guard and redirected to the appropriate portal.

## Proposed Changes

### Authentication & Role-Based Navigation Routing

#### [MODIFY] [AdminLoginActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/AdminLoginActivity.kt)
- Update login completion logic to inspect user `role` from Firestore `users`:
  - `Customer` ➔ Launch `DashboardActivity` (Customer Portal).
  - `Rider` ➔ Launch `RiderDashboardActivity` (Rider Portal).
  - `Admin` ➔ Launch `AdminDashboardActivity` (Admin Portal).

#### [MODIFY] [CreateAccountActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/CreateAccountActivity.kt)
- Update post-registration navigation:
  - `Customer` ➔ Launch `DashboardActivity`.
  - `Rider` ➔ Launch `RiderDashboardActivity`.

### Admin Portal & Security Isolation

#### [NEW] [AdminDashboardActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/AdminDashboardActivity.kt)
#### [NEW] [activity_admin_dashboard.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/layout/activity_admin_dashboard.xml)
#### [NEW] [admin_nav_menu.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/menu/admin_nav_menu.xml)
- Dedicated Admin Portal container with bottom navigation for:
  1. **Order Dispatch**: Manage customer delivery requests & assign riders/vehicles.
  2. **Fleet & Riders**: Integrated [`AssignRiderVehicleActivity.kt`](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/AssignRiderVehicleActivity.kt) (Rider & Vehicle management).
  3. **GIS Route Map**: [`MapFragment.kt`](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/MapFragment.kt) (Nasugbu-Lian GIS route overview).
  4. **Analytics & Reports**: [`ReportsFragment.kt`](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/ReportsFragment.kt).

#### [MODIFY] [AssignRiderVehicleActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/AssignRiderVehicleActivity.kt)
- Add Admin Security Guard: verify current user is Admin. If a non-admin attempts to access this screen, block access and finish activity.

### Rider Portal & Job Controls

#### [NEW] [RiderDashboardActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/RiderDashboardActivity.kt)
#### [NEW] [activity_rider_dashboard.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/layout/activity_rider_dashboard.xml)
#### [NEW] [rider_nav_menu.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/menu/rider_nav_menu.xml)
- Dedicated Rider Portal container with bottom navigation for:
  1. **My Assigned Deliveries**: Filtered list showing only deliveries assigned to the logged-in rider with status update buttons ("In Transit", "Delivered", "Report Problem").
  2. **Nasugbu-Lian Route**: View delivery route on map.
  3. **Profile & Logout**: Manage rider profile and sign out.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/AndroidManifest.xml)
- Register `AdminDashboardActivity` and `RiderDashboardActivity`.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to verify clean compilation of all new portals and activities.

### Manual Verification
- Log in as Customer ➔ Verify landing on Customer Dashboard (`DashboardActivity`). Attempt to navigate to Admin screens ➔ Verify Access Denied guard.
- Log in as Rider ➔ Verify landing on Rider Portal (`RiderDashboardActivity`) displaying assigned jobs.
- Log in as Admin ➔ Verify landing on Admin Portal (`AdminDashboardActivity`) with full fleet, dispatch, GIS map, and report controls.
