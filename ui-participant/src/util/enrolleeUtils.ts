import Api, { Enrollee, PortalParticipantUser, Study } from 'api/api'
import { HubUpdate } from 'hub/hubUpdates'
import { alertDefaults, AlertLevel, useI18n } from '@juniper/ui-core'
import { useUser } from '../providers/UserProvider'
import { isEmpty, isNil } from 'lodash'

/** whether the list of enrollees contains an enrollee matching the study */
export function userHasJoinedStudy(study: Study, enrollees: Enrollee[]) {
  return !!enrollees.find(enrollee => enrollee.studyEnvironmentId === study.studyEnvironments[0].id)
}

/** enrolls the user and displays a welcome banner on the dashboard */
export async function enrollCurrentUserInStudy(studyShortcode: string, studyName: string,
  preEnrollResponseId: string | null, refreshLogin: () => void) {
  await Api.createEnrollee({
    studyShortcode,
    preEnrollResponseId
  })
  const hubUpdate: HubUpdate = {
    message: {
      title: `Welcome to ${studyName}`,
      detail: alertDefaults['WELCOME'].detail,
      type: alertDefaults['WELCOME'].alertType as AlertLevel
    }
  }
  await refreshLogin()
  return hubUpdate
}

/**
 * Enrolls a proxy user in a study; if the proxyPpUserId is provided, then it will be attached
 * to an existing user, otherwise a new user will be created.
 */
export async function enrollProxyUserInStudy(
  studyShortcode: string,
  studyName: string,
  preEnrollResponseId: string | null,
  governedPpUserId: string | null,
  refreshLogin: () => void
) {
  await Api.createGovernedEnrollee({
    studyShortcode,
    preEnrollResponseId,
    governedPpUserId
  })
  const hubUpdate: HubUpdate = {
    message: {
      title: `Welcome to ${studyName}`,
      detail: alertDefaults['WELCOME'].detail,
      type: alertDefaults['WELCOME'].alertType as AlertLevel
    }
  }
  await refreshLogin()
  return hubUpdate
}

/**
 *
 */
export const useName = (ppUser: PortalParticipantUser | undefined) => {
  const { i18n } = useI18n()
  const { enrollees, user, relations } = useUser()

  if (!ppUser) {
    return ''
  }
  const profile = enrollees
    .find(enrollee => enrollee.profileId === ppUser.profileId && !isNil(enrollee.profile))?.profile
  const hasProxies = relations.some(relation => relation.relationshipType === 'PROXY')
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
