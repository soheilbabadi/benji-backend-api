"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { requestOtp, verifyOtp } from "@/services/registerService";

import OtpCodeInput from "@/components/OtpCodeInput";

type Step = "mobile" | "code";

/**
 * Registration form: asks for a mobile number, sends an OTP via SMS
 * (POST /api/auth/otp), then verifies it (POST /api/auth/otp/verify).
 * The backend creates the account automatically on first verification.
 */
export default function RegisterForm() {
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
      await requestOtp(trimmed);
      setStep("code");
      setInfo("We texted you a 6-digit code to finish creating your account.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  async function handleVerify(code: string) {
    setError(null);
    setBusy(true);
    try {
      await verifyOtp(mobile.trim(), code); // tokens persisted to localStorage by the service
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
      <h1 className="text-2xl font-bold tracking-tight">Create your account</h1>
      <p className="mt-2 text-sm text-zinc-500 dark:text-zinc-400">
        {step === "mobile"
          ? "Join your neighborhood with just your mobile number — no password needed."
          : `Confirm the code we sent to ${mobile.trim()} to activate your account.`}
      </p>

      {step === "mobile" ? (
        <form onSubmit={handleSendCode} className="mt-6 space-y-4" noValidate>
          <div>
            <label
              htmlFor="register-mobile"
              className="block text-sm font-medium"
            >
              Mobile number
            </label>
            <input
              id="register-mobile"
              type="tel"
              autoComplete="tel"
              placeholder="+98 912 345 6789"
              value={mobile}
              onChange={(e) => setMobile(e.target.value)}
              className="mt-1 w-full rounded-xl border border-black/[.15] bg-transparent px-4 py-2.5 text-sm outline-none transition focus:border-foreground focus:ring-2 focus:ring-foreground/20 dark:border-white/[.2]"
            />
            <p className="mt-2 text-xs text-zinc-400 dark:text-zinc-500">
              Include your country code (e.g. +98) for best results.
            </p>
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
        Already have an account?{" "}
        <Link
          href="/login"
          className="font-medium text-foreground underline underline-offset-4"
        >
          Log in
        </Link>
      </p>
    </div>
  );
}
