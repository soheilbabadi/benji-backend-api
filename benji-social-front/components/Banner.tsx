import Link from "next/link";

export default function Banner() {
  return (
    <section className="relative isolate overflow-hidden">
      {/* Decorative background */}
      <div
        aria-hidden
        className="absolute inset-0 -z-10 bg-gradient-to-b from-zinc-100 via-white to-white dark:from-zinc-900 dark:via-black dark:to-black"
      />
      <div
        aria-hidden
        className="absolute left-1/2 top-0 -z-10 h-[400px] w-[700px] -translate-x-1/2 rounded-full bg-indigo-400/20 blur-3xl dark:bg-indigo-500/10"
      />

      <div className="mx-auto flex max-w-6xl flex-col items-center px-4 py-24 text-center sm:px-6 sm:py-32">
        <span className="mb-6 inline-flex items-center gap-2 rounded-full border border-black/[.08] px-4 py-1.5 text-xs font-medium text-zinc-600 dark:border-white/[.14] dark:text-zinc-400">
          <span className="h-2 w-2 rounded-full bg-emerald-500" />
          Now available in your neighborhood
        </span>

        <h1 className="max-w-3xl text-4xl font-bold leading-tight tracking-tight text-black sm:text-6xl dark:text-zinc-50">
          Bring your community{" "}
          <span className="bg-gradient-to-r from-indigo-500 to-violet-500 bg-clip-text text-transparent">
            closer together
          </span>
        </h1>

        <p className="mt-6 max-w-xl text-lg leading-8 text-zinc-600 dark:text-zinc-400">
          Benji Social connects neighbors through local events, shared stories,
          and a lost &amp; found that actually works. Join thousands of people
          making their neighborhood feel like home.
        </p>

        <div className="mt-10 flex w-full flex-col gap-4 text-base font-medium sm:w-auto sm:flex-row">
          <Link
            href="/register"
            className="flex h-12 w-full items-center justify-center rounded-full bg-foreground px-8 text-background transition-colors hover:bg-[#383838] sm:w-auto dark:hover:bg-[#ccc]"
          >
            Get started — it&apos;s free
          </Link>
          <Link
            href="/about"
            className="flex h-12 w-full items-center justify-center rounded-full border border-solid border-black/[.12] px-8 transition-colors hover:border-transparent hover:bg-black/[.04] sm:w-auto dark:border-white/[.2] dark:hover:bg-white/[.06]"
          >
            Learn more
          </Link>
        </div>

        {/* Stats */}
        <dl className="mt-16 grid w-full max-w-2xl grid-cols-1 gap-6 border-t border-black/[.06] pt-10 text-center sm:grid-cols-3 dark:border-white/[.1]">
          {[
            { value: "12k+", label: "Active members" },
            { value: "340", label: "Local events" },
            { value: "1.8k", label: "Items reunited" },
          ].map((stat) => (
            <div key={stat.label}>
              <dt className="order-2 block text-sm text-zinc-500">
                {stat.label}
              </dt>
              <dd className="text-3xl font-semibold tracking-tight text-black dark:text-zinc-50">
                {stat.value}
              </dd>
            </div>
          ))}
        </dl>
      </div>
    </section>
  );
}
