import type { Metadata } from "next";
import LoginForm from "@/components/LoginForm";

export const metadata: Metadata = {
  title: "Log in",
  description:
    "Sign in to Benji Social with your mobile number and a one-time SMS code.",
};

export default function LoginPage() {
  return (
    <section className="flex min-h-[calc(100svh-4rem)] flex-col items-center justify-center px-4 py-16">
      <LoginForm />
    </section>
  );
}
