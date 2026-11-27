import type { Metadata } from 'next'
import { OtpVerifyScreen } from '@/components/auth/otp-verify-form'

export const metadata: Metadata = {
  title: 'Verify code · Benji',
  description: 'Enter the one-time code we sent you by SMS.',
}

// No searchParams here on purpose: the pending mobile number travels through
// sessionStorage (saved by MobileEntryForm), which keeps this route fully
// prerenderable under cacheComponents.
export default function VerifyOtpPage() {
  return <OtpVerifyScreen />
}
