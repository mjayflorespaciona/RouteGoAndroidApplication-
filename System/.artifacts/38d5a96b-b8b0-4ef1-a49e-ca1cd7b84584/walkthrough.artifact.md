# RoutGo Customer Login and Registration Walkthrough

Successfully created and improved the RoutGo Customer Login and Registration screens meeting all design and functional requirements.

## Changes Made

### Login Screen
- **[activity_admin_login.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/layout/activity_admin_login.xml)**: Configured UI featuring RoutGo logo/branding, Email input field, Password input field, show/hide password toggle, login button, forgot password link, and create account prompt.
- **[AdminLoginActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/AdminLoginActivity.kt)**: Implemented input validation, Firebase authentication credential check, password reset handling, role checking, and seamless redirection to the Customer Dashboard.

### Registration Screen
- **[activity_create_account.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/layout/activity_create_account.xml)**: Added the **Confirm Password** field (`confirmPasswordField`) and show/hide toggle alongside Full Name, Email Address, Contact Number, Password, Register button, and Back to Login link.
- **[CreateAccountActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/CreateAccountActivity.kt)**: Added validation for required fields, password match verification (`password == confirmPassword`), minimum password length check (6 chars), automatic assignment of `role: "Customer"` in Firestore `users`, prevention of admin registration, and successful redirection to the Customer Dashboard.

## Verification Results

### Automated Build Verification
- Ran Gradle build (`app:assembleDebug`) successfully with **zero errors**.
