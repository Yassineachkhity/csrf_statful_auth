# CSRF Demo with Spring Boot

## 1. Objective

This project is a simple demonstration of a **CSRF attack** in a Spring Boot application using **stateful authentication**.

The application uses:

- Spring Boot
- Spring Security
- HTTP Session
- `JSESSIONID` cookie
- A vulnerable `POST` endpoint
- A separate malicious HTML page

The goal is to understand why CSRF attacks are possible when authentication is based on cookies.

---

## 2. Stateful Authentication

After a successful login, Spring Security creates a session for the user.

The browser receives a cookie similar to:

```text
JSESSIONID=ABC123...
```

The browser automatically sends this cookie with future requests to the Spring Boot application.

Example:

```http
Cookie: JSESSIONID=ABC123
```

Because of this cookie, the server knows which user is authenticated.

---

## 3. Vulnerable Operation

The application contains a POST endpoint:

```text
POST /account/change-email
```

Example request:

```text
email=hacker@example.com
```

This endpoint changes the email of the currently authenticated user.

In the vulnerable version of the application, CSRF protection is disabled:

```java
.csrf(csrf -> csrf.disable());
```

This means that Spring only checks if the user has a valid authenticated session.

---

## 4. The Attack

The attacker creates a separate website containing a hidden form.

Example:

```html
<form
    id="csrfForm"
    action="http://localhost:8080/account/change-email"
    method="POST">

    <input
        type="hidden"
        name="email"
        value="hacker@example.com">

</form>

<script>
    document.getElementById("csrfForm").submit();
</script>
```

The attacker website can run on:

```text
http://localhost:9000
```

while the Spring Boot application runs on:

```text
http://localhost:8080
```

---

## 5. Attack Scenario

The user first logs in to:

```text
http://localhost:8080
```

with:

```text
username: yassine
password: password
```

Spring creates a session:

```text
JSESSIONID=XYZ123
```

The user then visits the attacker website:

```text
http://localhost:9000
```

and clicks on a malicious link.

The malicious page automatically sends:

```http
POST http://localhost:8080/account/change-email
```

with:

```text
email=hacker@example.com
```

The important point is that the browser automatically includes the user's session cookie:

```text
JSESSIONID=XYZ123
```

Therefore, Spring sees the request as authenticated.

The server does not know that the user did not intentionally submit the request.

The email is changed.

---

## 6. Why CSRF Works

CSRF works because the browser automatically sends authentication cookies.

The attacker does not need to know the value of:

```text
JSESSIONID
```

The attacker only needs to make the victim's browser send a request to the vulnerable application.

The flow is:

```text
User logs in
     |
     v
JSESSIONID stored in browser
     |
     v
User visits attacker website
     |
     v
Attacker page sends POST request
     |
     v
Browser automatically sends JSESSIONID
     |
     v
Server considers request authenticated
     |
     v
Operation executed
```

---

## 7. CSRF Protection

Spring Security enables CSRF protection by default.

If we remove:

```java
.csrf(csrf -> csrf.disable());
```

Spring requires a valid CSRF token for POST requests.

A legitimate form contains a token similar to:

```html
<input
    type="hidden"
    name="_csrf"
    value="random-secret-token">
```

The server verifies that the token sent by the client matches the token associated with the user's session.

The attacker does not know this token.

Therefore, the malicious request is rejected.

Typically Spring responds with:

```text
HTTP 403 Forbidden
```

---

## 8. Authentication vs CSRF Protection

Authentication answers:

```text
Who is making the request?
```

For example:

```text
JSESSIONID -> Yassine
```

CSRF protection answers:

```text
Did this request really come from the legitimate application?
```

A valid session alone is therefore not sufficient to protect sensitive POST operations.

---

## 9. Summary

Without CSRF protection:

```text
Valid JSESSIONID
        +
Malicious POST request
        =
Operation accepted
```

With CSRF protection:

```text
Valid JSESSIONID
        +
Missing / invalid CSRF token
        =
403 Forbidden
```

