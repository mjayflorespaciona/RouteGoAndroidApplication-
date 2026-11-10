# RoutGo Customer Login and Registration Implementation Plan

Improve and update `AdminLoginActivity.kt`, `CreateAccountActivity.kt`, `activity_admin_login.xml`, and `activity_create_account.xml` to meet all RoutGo customer login and registration requirements.

## User Review Required

> [!IMPORTANT]
> - Registration screen will be updated to include the **Confirm Password** field (`confirmPasswordField`) with password matching validation.
> - Newly registered accounts will automatically receive `role: "Customer"` in Firestore, strictly prohibiting admin registration.
> - Authenticated customers will be redirected to the Customer Dashboard (`DashboardActivity`).

## Proposed Changes

### Login Screen UI & Logic

#### [MODIFY] [activity_admin_login.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/layout/activity_admin_login.xml)
- Ensure RoutGo logo and application title are clearly visible.
- Provide Email input field (`loginUserField`), Password input field (`loginPassField`), show/hide password toggle (`togglePasswordBtn`), Sign In button (`signInBtn`), Forgot Password link (`forgotPasswordText`), and Create Account link (`signUpPrompt`).

#### [MODIFY] [AdminLoginActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/AdminLoginActivity.kt)
- Validate email and password inputs before submission.
- Authenticate with Firebase Auth and check user role in Firestore.
- Redirect successful customers to `DashboardActivity` while enforcing role-based access control.

### Registration Screen UI & Logic

#### [MODIFY] [activity_create_account.xml](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/res/layout/activity_create_account.xml)
- Add **Confirm Password** field (`confirmPasswordField`) and show/hide password toggle (`toggleConfirmPasswordBtn`).
- Maintain Full Name (`fullNameField`), Email Address (`emailField`), Contact Number (`phoneField`), Password (`passwordField`), Register button (`createAccountBtn`), and Back to Login link (`signInText`).

#### [MODIFY] [CreateAccountActivity.kt](file:///C:/Users/Aron/Desktop/SystemRoute/System/app/src/main/java/com/example/delitrack/frontEnd/CreateAccountActivity.kt)
- Add comprehensive validation for required fields, ensuring password matches confirm password.
- Create user in Firebase Auth and save user profile in Firestore `users` collection with `role: "Customer"`.
- Strictly prohibit registering as Admin.
- Redirect successful registration directly to `DashboardActivity`.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to verify clean compilation.

### Manual Verification
- Test customer registration with valid fields and password matching.
- Test login flow with customer credentials and verify redirection to Customer Dashboard.
