# Frontend Programming

# Project Overview

You are required to develop a React application that demonstrates your understanding of modern React concepts, including:

- State management
- Routing
- Asynchronous operations
- API integration

The project should be fully functional and well-structured, showcasing good coding practices.

This practical project accounts for 50% of the final grade; the remaining 50% will be assessed through an oral examination.

---

# General Requirements

## Project Theme

You are free to choose the theme of your application.

Examples include:

- E-commerce store
- Task manager
- Social media dashboard
- Movie database
- Any other suitable application

---

## Components

- Break the UI into reusable and well-structured components.
- Use conditional rendering where possible.
- Use component composition where possible.

---

## State Management

Use:

- `useState`
- `useEffect`

for managing component state and side effects.

Use:

- Redux for global state management
- Thunk for asynchronous actions

---

## Pages and Routing

The application should include at least six distinct pages, covering areas such as:

- Homepage
- Dashboard for administrators or content creators
- One or more listing pages with:
  - Filters
  - Paginated content
- At least one detail page showing information about a specific item

Examples:

```text
/product/42
/post/123
```

Use React Router for navigation, including dynamic routing for detail pages.

---

## Users

The application must simulate the presence of users by implementing a login system.

- Fake authentication is acceptable.
- The application must support at least two user roles.

Examples:

- Regular user
- Admin

Different roles must have different views and permissions.

Examples:

- Admins may access management features
- Admins may access additional content sections

---

## API Usage

The application must consume external APIs for dynamic content.

Examples:

- OpenWeather
- TMDB
- JSONPlaceholder

You may also use:

- Custom APIs with Json Server
- Or both external and custom APIs together

---

## Forms

Include at least four controlled forms with validation.

These forms should mimic real-world functionality and be closely tied to the application's features.

Examples:

### Marketplace App

An admin user should be able to:

- Create products
- Update products
- Delete products

through forms.

### Social Media App

A user should be able to:

- Create posts
- Edit posts
- Delete posts

using forms that handle user input.

### Validation Requirements

Each form must:

- Be fully functional
- Validate required fields
- Validate proper formats
- Handle errors properly

---

## Submission Requirements

The project must be:

- Hosted on GitHub
- Or submitted via `.zip` file on LMS

The submission must include everything needed to run the application and a clear `README.md` explaining:

- Project overview
- Running instructions
- Features
- Technologies used

---

# Optional Features

- Use CSS, Bootstrap, Tailwind, or another styling library
- Use additional libraries to improve UX/UI
- Optimize performance where applicable

---

# Final Notes

- All general requirements are mandatory.
- Failure to meet requirements will result in penalties in the final evaluation.
- Optional features are not required but can contribute to a higher score if implemented effectively.

This assignment is designed to give creative freedom while ensuring proficiency in key React concepts.

Choose a project idea that interests you and make it your own while following the guidelines.

Good luck and happy coding! 🚀

---

# Backend Programming

# Project Overview

You are required to build a complete backend application using Spring and PostgreSQL, demonstrating your ability to design and implement robust server-side features, including:

- Request handling
- Data persistence
- Validation
- Authentication
- Business logic structuring
- Seamless interaction with:
  - The underlying database
  - External services when needed

The project should be fully functional and well-structured, showcasing good coding practices.

This practical project accounts for 50% of the final grade; the remaining 50% will be assessed through an oral examination.

---

# General Requirements

## Project Theme

You are free to choose the theme of your application.

Examples include:

- E-commerce store
- Task manager
- Social media dashboard
- Movie database
- Any other suitable application

---

## Entities

The application must include a domain model with:

- At least eight tables
- Coherent and meaningful relationships
- At least one inheritance structure that justifies a hierarchy within the domain

---

## User Requirements

The application must include a complete user management system.

Each user must have:

- Email
- Password
- Profile image

The profile image must be updatable after registration.

Users must also include common personal details relevant to the domain, such as:

- Name
- Surname
- Registration date
- Additional domain-specific information

The profile should feel realistic and fully functional.

---

## REST APIs

The system must expose REST APIs that follow consistent principles for:

- Request handling
- Responses
- Error management

The APIs must ensure predictable and reliable interactions.

---

## Auth

The application must implement:

- Authentication
- Authorization

based on JWT.

The user model must include at least three distinct roles, each with its own permissions and access rules.

---

## Queries

Queries must support:

- Filtering
- Sorting
- Aggregations
- Multiple combined conditions

You may use:

- JPA query methods
- JPQL
- Native SQL

Queries should support real use cases within the application.

---

## Error Handling

The project must:

- Validate all incoming data
- Handle errors through structured and meaningful responses
- Behave reliably in expected and unexpected situations
- Present consistent patterns across the application

---

## 3rd Party APIs

The backend must interact with at least two third-party APIs.

The retrieved information must:

- Be incorporated meaningfully into the system
- Contribute to:
  - Internal application logic
  - Exposed functionality

---

## Supporting Material

The project must be hosted on GitHub including:

- Everything needed to run the application
- A clear `README.md`

The README must explain:

- Project overview
- Running instructions
- Environment variables
- Features
- Other relevant information

---

## Postman Collection

Students must include a Postman collection in JSON format containing all requests needed to test every implemented feature.

Each request must include:

- Example payloads
- Parameters
- Headers
- All details required for immediate use

Important:

- Any functionality not represented in the Postman collection will not be evaluated during grading.

---

# Final Notes

- All general requirements are mandatory.
- Failure to meet requirements will result in penalties in the final evaluation.
- Penalties may apply if security principles or best practices taught during the course are not followed.

Optional features may be implemented for extra points, including:

- Additional third-party API integrations
- GraphQL sections
- Complex or optimized queries
- Other meaningful extensions

Good luck and happy coding! 🚀