import React, { useState, useEffect } from 'react'
import Api, { EnrolleeSearchResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { Link } from 'react-router-dom'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchResult[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    Api.getEnrollees(portal.shortcode, study.shortcode, currentEnv.environmentName).then(result => {
      setParticipantList(result)
      setIsLoading(false)
    }).catch(() => {
      Store.addNotification(failureNotification(`Error loading participants`))
    })
  }, [])
  return <div className="ParticipantList container pt-2">
    <div className="row">
      <div className="col-12">
        <h5>Participants</h5>
        <LoadingSpinner isLoading={isLoading}>
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Shortcode</th>
                <th>Family name</th>
                <th>Given name</th>
                <th>Consented</th>
                <th>Forms complete</th>
              </tr>
            </thead>
            <tbody>
              { participantList.map(participant => {
                return <tr>
                  <td><Link to={`${currentEnvPath}/participants/${participant.enrollee.shortcode}`}>
                    {participant.enrollee.shortcode}
                  </Link></td>
                  <td>{participant.profile.givenName}</td>
                  <td>{participant.profile.familyName}</td>
                  <td>{participant.enrollee.consented ? 'Yes' : 'No'}</td>
                  <td>No</td>
                </tr>
              })}
            </tbody>
          </table>
        </LoadingSpinner>
      </div>
    </div>
  </div>
}

export default ParticipantList
