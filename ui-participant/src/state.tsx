import { useLocalStorage } from 'util/storageUtils'

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user isn't signed in yet (since they don't have an account), so local storage is the best way to keep this. */
const PREREG_ID_STORAGE_KEY = 'preRegResponseId'

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user might not be signed in yet (since they don't have an account),
 * so local storage is the best way to keep this. */
const PRE_ENROLL_ID_KEY = 'preEnrollResponseId'

const RETURN_TO_PORTAL_KEY = 'returnToPortal'
const RETURN_TO_STUDY_KEY = 'returnToStudy'

export const usePreRegResponseId = () => useLocalStorage(PREREG_ID_STORAGE_KEY)
export const usePreEnrollResponseId = () => useLocalStorage(PRE_ENROLL_ID_KEY)
export const useReturnToPortal = () => useLocalStorage(RETURN_TO_PORTAL_KEY)
export const useReturnToStudy = () => useLocalStorage(RETURN_TO_STUDY_KEY)
