import { useCallback } from 'react'

import { useLocalStorage, useSessionStorage } from 'util/storageUtils'

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user isn't signed in yet (since they don't have an account), so local storage is the best way to keep this. */
export const usePreRegResponseId = () => useSessionStorage('preRegResponseId')

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user might not be signed in yet (since they don't have an account),
 * so local storage is the best way to keep this. */
export const usePreEnrollResponseId = () => useSessionStorage('preEnrollResponseId')

/** store the study being enrolled in so that we remember it when coming back from B2C sign-up. */
export const useReturnToStudy = () => useSessionStorage('returnToStudy')

/** store whether we are processing an invitation, and what type it is.
 * For now, 'link' is the only type contemplated. */
export const useInvitationType = () => useSessionStorage('invitationType')

/** store the participants preferred language so that we remember it when coming back from B2C sign-up. */
export const useReturnToLanguage = () => useSessionStorage('returnToLanguage')

/** store whether the user has successfully provided a password for a password protected study */
export const useHasProvidedStudyPassword = (studyShortcode: string): [boolean, () => void] => {
  const [value, setValue] = useSessionStorage(`provided-study-password/${studyShortcode}`)
  const setHasProvidedStudyPassword = useCallback(() => setValue('true'), [])
  return [value === 'true', setHasProvidedStudyPassword]
}

export const useCookiesAcknowledged = (): [boolean, () => void] => {
  const [value, setValue] = useLocalStorage('cookiesAcknowledged')
  return [!!value, () => setValue('true')]
}
