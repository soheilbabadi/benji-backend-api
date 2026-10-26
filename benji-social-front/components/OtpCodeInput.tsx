"use client";

import { useEffect, useRef, useState } from "react";

const CODE_LENGTH = 6;

interface OtpCodeInputProps {
  onComplete: (code: string) => void;
  disabled?: boolean;
  autoFocus?: boolean;
}

/**
 * Six-box one-time-code input with auto-advance, backspace navigation and
 * paste support. Calls onComplete as soon as all digits are filled.
 */
export default function OtpCodeInput({
  onComplete,
  disabled = false,
  autoFocus = true,
}: OtpCodeInputProps) {
  const [digits, setDigits] = useState<string[]>(
    Array(CODE_LENGTH).fill(""),
  );
  const refs = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    if (autoFocus) refs.current[0]?.focus();
  }, [autoFocus]);

  function setDigit(index: number, value: string) {
    const next = [...digits];
    next[index] = value;
    setDigits(next);

    if (value && index < CODE_LENGTH - 1) {
      refs.current[index + 1]?.focus();
    }
    if (next.every((d) => d !== "")) {
      onComplete(next.join(""));
    }
  }

  function handleKeyDown(
    index: number,
    e: React.KeyboardEvent<HTMLInputElement>,
  ) {
    if (e.key === "Backspace" && !digits[index] && index > 0) {
      refs.current[index - 1]?.focus();
      setDigit(index - 1, "");
    }
    if (e.key === "ArrowLeft" && index > 0) {
      refs.current[index - 1]?.focus();
    }
    if (e.key === "ArrowRight" && index < CODE_LENGTH - 1) {
      refs.current[index + 1]?.focus();
    }
  }

  function handlePaste(e: React.ClipboardEvent<HTMLInputElement>) {
    e.preventDefault();
    const pasted = e.clipboardData
      .getData("text")
      .replace(/\D/g, "")
      .slice(0, CODE_LENGTH);
    if (!pasted) return;
    const next = Array(CODE_LENGTH).fill("");
    for (let i = 0; i < pasted.length; i++) next[i] = pasted[i];
    setDigits(next);
    refs.current[Math.min(pasted.length, CODE_LENGTH - 1)]?.focus();
    if (next.every((d) => d !== "")) onComplete(next.join(""));
  }

  return (
    <div className="flex justify-center gap-2" dir="ltr">
      {digits.map((digit, i) => (
        <input
          key={i}
          ref={(el) => {
            refs.current[i] = el;
          }}
          inputMode="numeric"
          autoComplete={i === 0 ? "one-time-code" : "off"}
          maxLength={1}
          disabled={disabled}
          value={digit}
          onChange={(e) => {
            const v = e.target.value.replace(/\D/g, "").slice(-1);
            setDigit(i, v);
          }}
          onKeyDown={(e) => handleKeyDown(i, e)}
          onPaste={handlePaste}
          aria-label={`Digit ${i + 1} of ${CODE_LENGTH}`}
          className="h-12 w-10 rounded-xl border border-black/[.15] bg-white text-center text-lg font-semibold outline-none transition focus:border-foreground focus:ring-2 focus:ring-foreground/20 disabled:opacity-50 dark:border-white/[.2] dark:bg-transparent"
        />
      ))}
    </div>
  );
}
