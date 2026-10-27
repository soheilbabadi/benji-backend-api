import type { Metadata } from 'next'
import MobileEntryForm from '@/components/auth/mobile-entry-form'
import AuthGate from '@/components/auth/auth-gate'

export const metadata: Metadata = {
  title: 'Create account · Benji',
  description: 'Join Benji with just your mobile number — verified by SMS code.',
}

interface RegisterPageProps {
  searchParams: Promise<{ next?: string }>
}

export default function RegisterPage({ searchParams }: RegisterPageProps) {
  return (
    <AuthGate access="public">
      {/* Same as /login: ?next= is resolved client-side inside the shared form
          so this route stays prerenderable under cacheComponents. */}
      <MobileEntryForm mode="register" searchParams={searchParams} />
    </AuthGate>
  )
}
