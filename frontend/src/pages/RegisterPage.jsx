import { AuthForm } from '../components/AuthForm';

export function RegisterPage() {
  return (
    <section className="auth-page">
      <div>
        <p className="eyebrow">Register</p>
        <h1>Create your EventHub account</h1>
      </div>
      <AuthForm mode="register" />
    </section>
  );
}
