# RoutGo Customer Login and Registration Implementation Plan

Improve and update the existing authentication activities (`AdminLoginActivity.kt` / login screen, `CreateAccountActivity.kt` / registration screen, and their layout XMLs) to meet all RoutGo customer login and registration requirements.

## User Review Required

> [!IMPORTANT]
> - Ensure Registration screen includes **Confirm Password** field with validation matching the password.
> - Automatically assign `role: "Customer"` in Firestore `users` collection upon registration and prevent any admin registration.
> - Redirect successful customers to the Customer Dashboard (`DashboardActivity`).

## Proposed Changes

### Login Screen
#### [MODIFY] [activity_admin_login.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/layout/activity_admin_login.xml)
- Update title/branding to "RoutGo Customer Login" / logo.
- Ensure Email input field, Password input field, show/hide password toggle, login button, forgot password link, and create account link are fully functional and cleanly styled.

#### [MODIFY] [AdminLoginActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/AdminLoginActivity.kt)
- Validate email and password inputs.
- Authenticate with Firebase Auth and verify user role or redirect successfully to `DashboardActivity`.
- Prevent customer accounts from accessing admin-only functions.

### Registration Screen
#### [MODIFY] [activity_create_account.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/layout/activity_create_account.xml)
- Add **Confirm Password** input field (`confirmPasswordField`) and toggle show/hide.
- Ensure Full Name, Email Address, Contact Number, Password, Confirm Password, Register button, and Back to Login link (`signInText`) are present and styled cleanly.

#### [MODIFY] [CreateAccountActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/CreateAccountActivity.kt)
- Validate all required fields including password matching confirm password.
- Register user in Firebase Auth.
- Automatically assign `role: "Customer"` in Firestore `users` collection.
- Prevent any attempt to register as ADMIN.
- Redirect on success to Dashboard.

## Verification Plan

### Automated Tests
- Code inspection and verification of inputs and validations.

### Manual Verification
- Test login with customer credentials and default admin.
- Test registration with new customer account (verifying confirm password validation and automatic customer role assignment in Firestore).
