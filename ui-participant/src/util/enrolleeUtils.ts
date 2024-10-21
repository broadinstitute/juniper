import Api, {
  PortalParticipantUser,
  Study
} from 'api/api'
import {
  Enrollee, HubResponse,
  useI18n
} from '@juniper/ui-core'
import { useUser } from '../providers/UserProvider'
import {
  isEmpty,
  isNil
} from 'lodash'

/** whether the list of enrollees contains an enrollee matching the study */
export function userHasJoinedStudy(study: Study, enrollees: Enrollee[]) {
  return !!enrollees.find(enrollee => enrollee.studyEnvironmentId === study.studyEnvironments[0].id)
}

/** enrolls the user and displays a welcome banner on the dashboard */
export async function enrollCurrentUserInStudy(
  studyShortcode: string,
  preEnrollResponseId: string | null,
  refreshLogin: () => Promise<void>
): Promise<HubResponse> {
  const hubResponse = await Api.createEnrollee({
    studyShortcode,
    preEnrollResponseId
  })
  await refreshLogin()
  return hubResponse
}

/**
 * Enrolls a proxy user in a study; if the proxyPpUserId is provided, then it will be attached
 * to an existing user, otherwise a new user will be created.
 */
export async function enrollProxyUserInStudy(
  studyShortcode: string,
  preEnrollResponseId: string | null,
  governedPpUserId: string | null,
  refreshLogin: () => Promise<void>
): Promise<HubResponse> {
  const hubResponse = await Api.createGovernedEnrollee({
    studyShortcode,
    preEnrollResponseId,
    governedPpUserId
  })
  await refreshLogin()
  return hubResponse
}

/**
 * Provides the name of the user (or 'You' / 'Your dependent' if no name provided).
 * If undefined, returns an empty string.
 */
export const useName = (ppUser: PortalParticipantUser | undefined) => {
  const { i18n } = useI18n()
  const { enrollees, user, proxyRelations } = useUser()

  if (!ppUser) {
    return ''
  }
  const profile = enrollees
    .find(enrollee => enrollee.profileId === ppUser.profileId && !isNil(enrollee.profile))?.profile
  const hasProxies = proxyRelations.some(relation => relation.relationshipType === 'PROXY')
  const isMainUser = ppUser.participantUserId === user?.id

  const givenName = profile?.givenName || ''
  const familyName = profile?.familyName || ''

  if (profile && !isEmpty(`${givenName}${familyName}`.trim())) {
    return `${givenName} ${familyName}${hasProxies && isMainUser ? ` ${i18n('youInParens')}` : ''}`
  } else {
    if (isMainUser) {
      return i18n('you')
    } else {
      return i18n('yourDependent')
    }
  }
}
