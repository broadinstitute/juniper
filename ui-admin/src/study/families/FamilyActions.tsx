import React from 'react'
import { Family } from '@juniper/ui-core'
import {
  participantListPath,
  StudyEnvContextT
} from 'study/StudyEnvironmentRouter'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import JustifyChangesModal from 'study/participants/JustifyChangesModal'
import { useNavigate } from 'react-router-dom'

/**
 * Renders editable page with all family members and relations.
 */
export const FamilyActions = (
  { family, studyEnvContext }: {
    family: Family, studyEnvContext: StudyEnvContextT
  }
) => {
  const [isDeleting, setIsDeleting] = React.useState<boolean>(false)
  const navigate = useNavigate()

  const deleteFamily = async (justification: string) => {
    try {
      await Api.deleteFamily(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        family.shortcode,
        justification)
      setIsDeleting(false)
      Store.addNotification(successNotification('Family deleted.'))
      navigate(`${participantListPath(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName
      )}?groupByFamily=true`)
    } catch (e) {
      Store.addNotification(failureNotification('Could not delete family.'))
    }
  }

  return <div>
    <h4>Delete Family</h4>
    <p>
      Note: this only deletes the family relationships. Participants will still be able to participate in the study.
    </p>
    <button className="btn btn-danger" onClick={() => setIsDeleting(true)}>Delete Family</button>
    {isDeleting && <JustifyChangesModal
      saveWithJustification={deleteFamily}
      onDismiss={() => setIsDeleting(false)}
      bodyText={<p>Are you sure you want to delete {family.shortcode}?</p>}
    />}
  </div>
}
