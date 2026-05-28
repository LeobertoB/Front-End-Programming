import { AuthForm } from '../components/AuthForm';

export function LoginPage() {
  return (
    <section className="auth-page">
      <div>
        <p className="eyebrow">Login</p>
        <h1>Access your account</h1>
      </div>
      <AuthForm mode="login" />
    </section>
  );
}
