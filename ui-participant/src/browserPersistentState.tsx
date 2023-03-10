import { useSessionStorage } from 'util/storageUtils'

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user isn't signed in yet (since they don't have an account), so local storage is the best way to keep this. */
export const usePreRegResponseId = () => useSessionStorage('preRegResponseId')

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user might not be signed in yet (since they don't have an account),
 * so local storage is the best way to keep this. */
export const usePreEnrollResponseId = () => useSessionStorage('preEnrollResponseId')

/** store the study being enrolled in so that we remember it when coming back from B2C sign-up. */
export const useReturnToStudy = () => useSessionStorage('returnToStudy')
