import Api, { Enrollee, Study } from 'api/api'
import { HubUpdate } from 'hub/hubUpdates'
import { alertDefaults, AlertLevel } from '@juniper/ui-core'

/** whether the list of enrollees contains an enrollee matching the study */
export function userHasJoinedStudy(study: Study, enrollees: Enrollee[]) {
  return !!enrollees.find(enrollee => enrollee.studyEnvironmentId === study.studyEnvironments[0].id)
}

/** enrolls the user and displays a welcome banner on the dashboard */
export async function enrollCurrentUserInStudy(studyShortcode: string, studyName: string,
  preEnrollResponseId: string | null, updateEnrollee: (enrollee: Enrollee) => void) {
  const response = await Api.createEnrollee({
    studyShortcode,
    preEnrollResponseId
  })
  const hubUpdate: HubUpdate = {
    message: {
      title: `Welcome to ${studyName}`,
      detail: alertDefaults['WELCOME'].detail,
      type: alertDefaults['WELCOME'].type as AlertLevel
    }
  }
  await updateEnrollee(response.enrollee)
  return hubUpdate
}
