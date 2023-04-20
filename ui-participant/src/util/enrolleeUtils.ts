import { Enrollee, PortalStudy } from '../api/api'

/** whether the list of enrollees contains an enrollee matching the study */
export function userHasJoinedPortalStudy(portalStudy: PortalStudy, enrollees: Enrollee[]) {
  return !!enrollees.find(enrollee => enrollee.studyEnvironmentId === portalStudy.study.studyEnvironments[0].id)
}
