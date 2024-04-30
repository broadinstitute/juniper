import Api, { Enrollee, Study } from 'api/api'
import { HubUpdate } from 'hub/hubUpdates'
import { alertDefaults, AlertLevel } from '@juniper/ui-core'

/** whether the list of enrollees contains an enrollee matching the study */
export function userHasJoinedStudy(study: Study, enrollees: Enrollee[]) {
  return !!enrollees.find(enrollee => enrollee.studyEnvironmentId === study.studyEnvironments[0].id)
}

/** enrolls the user and displays a welcome banner on the dashboard */
export async function enrollCurrentUserInStudy(studyShortcode: string, studyName: string,
  preEnrollResponseId: string | null, refreshLogin: () => Promise<void>) {
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
  refreshLogin: () => Promise<void>
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
