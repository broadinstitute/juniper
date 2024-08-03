import React from 'react'
import { Family } from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { FamilyRelations } from 'study/families/FamilyRelations'
import { FamilyMembers } from 'study/families/FamilyMembers'
import { ProbandEditor } from 'study/families/ProbandEditor'

/**
 * Renders editable page with all family members and relations.
 */
export const FamilyMembersAndRelations = (
  { family, studyEnvContext, reloadFamily }: {
    family: Family, studyEnvContext: StudyEnvContextT, reloadFamily: () => void
  }
) => {
  return <div>
    <ProbandEditor family={family} studyEnvContext={studyEnvContext} reloadFamily={reloadFamily}/>
    <div className="my-4"/>
    <FamilyRelations family={family} studyEnvContext={studyEnvContext} reloadFamily={reloadFamily}/>
    <div className="my-4"/>
    <FamilyMembers family={family} studyEnvContext={studyEnvContext} reloadFamily={reloadFamily}/>
  </div>
}
