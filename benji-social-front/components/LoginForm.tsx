"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { requestLoginOtp, loginWithOtp } from "@/services/loginService";
import OtpCodeInput from "@/components/OtpCodeInput";

type Step = "mobile" | "code";

/**
 * Login form: two-step mobile-number + OTP flow against the Spring backend
 * (POST /api/auth/otp then POST /api/auth/otp/verify). On success the session
 * tokens are stored by the service and the user is redirected home.
 */
export default function LoginForm() {
  const router = useRouter();
  const [step, setStep] = useState<Step>("mobile");
  const [mobile, setMobile] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleSendCode(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setInfo(null);

    const trimmed = mobile.trim();
    if (!/^\+?[\d\s-]{8,17}$/.test(trimmed)) {
      setError("Enter a valid mobile number, e.g. +989123456789.");
      return;
    }

    setBusy(true);
    try {
      await requestLoginOtp(trimmed);
      setStep("code");
      setInfo("A code has been sent to your phone by SMS.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  async function handleVerify(code: string) {
    setError(null);
    setInfo(null);
    setBusy(true);
    try {
      await loginWithOtp(mobile.trim(), code);
      router.push("/");
      router.refresh();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Verification failed. Try again.",
      );
      setBusy(false);
    }
  }

  return (
    <div className="w-full max-w-md rounded-2xl border border-black/[.08] bg-white p-8 shadow-sm dark:border-white/[.12] dark:bg-zinc-950">
      <h1 className="text-2xl font-bold tracking-tight">Welcome back</h1>
      <p className="mt-2 text-sm text-zinc-500 dark:text-zinc-400">
        {step === "mobile"
          ? "Sign in with your mobile number — we'll text you a one-time code."
          : `Enter the 6-digit code we sent to ${mobile.trim()}.`}
      </p>

      {step === "mobile" ? (
        <form onSubmit={handleSendCode} className="mt-6 space-y-4" noValidate>
          <div>
            <label
              htmlFor="login-mobile"
              className="block text-sm font-medium"
            >
              Mobile number
            </label>
            <input
              id="login-mobile"
              type="tel"
              autoComplete="tel"
              placeholder="+98 912 345 6789"
              value={mobile}
              onChange={(e) => setMobile(e.target.value)}
              className="mt-1 w-full rounded-xl border border-black/[.15] bg-transparent px-4 py-2.5 text-sm outline-none transition focus:border-foreground focus:ring-2 focus:ring-foreground/20 dark:border-white/[.2]"
            />
          </div>

          {error && (
            <p role="alert" className="text-sm text-red-600 dark:text-red-400">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={busy}
            className="w-full rounded-xl bg-foreground px-4 py-2.5 text-sm font-medium text-background transition hover:bg-[#383838] disabled:opacity-60 dark:hover:bg-[#ccc]"
          >
            {busy ? "Sending code…" : "Send code"}
          </button>
        </form>
      ) : (
        <div className="mt-6 space-y-4">
          <OtpCodeInput onComplete={handleVerify} disabled={busy} />

          {info && !error && (
            <p className="text-center text-sm text-emerald-600 dark:text-emerald-400">
              {info}
            </p>
          )}
          {error && (
            <p
              role="alert"
              className="text-center text-sm text-red-600 dark:text-red-400"
            >
              {error}
            </p>
          )}
          {busy && (
            <p className="text-center text-sm text-zinc-500">Verifying…</p>
          )}

          <div className="flex items-center justify-between text-sm">
            <button
              type="button"
              onClick={() => {
                setStep("mobile");
                setError(null);
                setInfo(null);
                setBusy(false);
              }}
              className="text-zinc-500 underline-offset-4 hover:underline"
            >
              Change number
            </button>
            <button
              type="button"
              onClick={handleSendCode}
              disabled={busy}
              className="text-zinc-500 underline-offset-4 hover:underline disabled:opacity-50"
            >
              Resend code
            </button>
          </div>
        </div>
      )}

      <p className="mt-6 text-center text-sm text-zinc-500 dark:text-zinc-400">
        New here?{" "}
        <Link
          href="/register"
          className="font-medium text-foreground underline underline-offset-4"
        >
          Create an account
        </Link>
      </p>
    </div>
  );
}
