import type { Metadata } from "next";
import RegisterForm from "@/components/RegisterForm";

export const metadata: Metadata = {
  title: "Sign up",
  description:
    "Create your Benji Social account with your mobile number — verified by a one-time SMS code.",
};

export default function RegisterPage() {
  return (
    <section className="flex min-h-[calc(100svh-4rem)] flex-col items-center justify-center px-4 py-16">
      <RegisterForm />
    </section>
  );
}
