import type { Metadata } from 'next'
import MobileEntryForm from '@/components/auth/mobile-entry-form'
import AuthGate from '@/components/auth/auth-gate'

export const metadata: Metadata = {
  title: 'Sign in · Benji',
  description: 'Sign in to Benji with a one-time code sent by SMS.',
}

interface LoginPageProps {
  searchParams: Promise<{ next?: string }>
}

export default function LoginPage({ searchParams }: LoginPageProps) {
  return (
    <AuthGate access="public">
      {/* Intended post-login destination (?next=, set by AuthGate) is read and
          validated client-side inside the form — keeps this route fully
          prerenderable under cacheComponents. */}
      <MobileEntryForm mode="login" searchParams={searchParams} />
    </AuthGate>
  )
}
