import React from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { Family } from '@juniper/ui-core'

/**
 *
 */
export const FamilyOverview = (
  {
    family, studyEnvContext, onUpdate
  }: {
    family: Family,
    studyEnvContext: StudyEnvContextT,
    onUpdate: () => void
  }) => {
  return <div>
    <h1>Family Overview</h1>
    <p>Family: {family.id}</p>
  </div>
}
