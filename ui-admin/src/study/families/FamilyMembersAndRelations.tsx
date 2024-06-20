import React from 'react'
import { Family } from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { FamilyRelations } from 'study/families/FamilyRelations'
import { FamilyMembers } from 'study/families/FamilyMembers'

/**
 *
 */
export const FamilyMembersAndRelations = (
  { family, studyEnvContext }:{
    family: Family, studyEnvContext: StudyEnvContextT
  }
) => {
  return <div>
    <h4>Members</h4>
    <FamilyMembers family={family} studyEnvContext={studyEnvContext}/>
    <div className="my-4"/>
    <h4>Relations</h4>
    <FamilyRelations family={family} studyEnvContext={studyEnvContext}/>
  </div>
}
